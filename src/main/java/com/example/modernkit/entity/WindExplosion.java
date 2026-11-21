package com.example.modernkit.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.List;

/**
 * Shared wind-burst explosion behavior, used by both the Wind Charge projectile
 * and the Wind Burst mace enchantment. Like in newer versions, the burst is a
 * knockback-only explosion that also presses buttons, flips levers, opens
 * wooden doors/trapdoors/fence gates, rings bells and breaks chorus flowers,
 * while respecting Blast Protection and knockback resistance.
 *
 * The burst also reproduces the vanilla fall rule: players knocked into the
 * air only start accumulating fall damage once they fall below the Y level
 * of the burst that hit them (tracked in {@link WindChargeFloors}), so wind
 * charge jumps land safely. The visuals are the expanding gust rings rather
 * than a plain smoke puff.
 *
 * The knockback itself follows the vanilla explosion model (decompiled from
 * the 1.16.5 game jar): falloff from the entity's feet against the burst
 * radius, push direction toward the entity's eyes, line-of-sight exposure
 * from {@link Explosion#getExposure}, then Blast Protection and knockback
 * resistance.
 */
public final class WindExplosion {

    private WindExplosion() {
    }

    /**
     * Runs one wind burst explosion at the given position.
     *
     * @param excluded an entity that is launched separately and should not
     *                 receive the generic radial knockback (may be null)
     */
    public static void burst(
            World world,
            double x,
            double y,
            double z,
            double radius,
            double knockbackBase,
            Entity owner,
            Entity excluded,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        if (world.isClient) {
            return;
        }

        ServerWorld serverWorld = (ServerWorld) world;

        serverWorld.playSound(
                null,
                x,
                y,
                z,
                sound,
                SoundCategory.PLAYERS,
                volume,
                pitch
        );
        spawnGust(serverWorld, x, y, z);

        interactWithBlocks(world, x, y, z, radius, owner);
        knockbackNearby(world, x, y, z, radius, knockbackBase, excluded);
    }

    /**
     * Spawns the gust burst: two flat expanding cloud rings like the vanilla
     * wind burst, a rising center column, end-rod sparks around the edge and
     * a soft base puff.
     */
    private static void spawnGust(ServerWorld world, double x, double y, double z) {
        spawnRing(world, x, y, z, 0.7D, 10);
        spawnRing(world, x, y + 0.15D, z, 1.5D, 16);

        // Rising center column.
        for (int i = 0; i < 6; i++) {
            world.spawnParticles(
                    ParticleTypes.CLOUD,
                    x,
                    y + 0.1D + i * 0.35D,
                    z,
                    1,
                    0.08D,
                    0.02D,
                    0.08D,
                    0.02D
            );
        }

        // End-rod sparks around the edge.
        for (int i = 0; i < 12; i++) {
            double angle = (Math.PI * 2.0D * i) / 12.0D;
            world.spawnParticles(
                    ParticleTypes.END_ROD,
                    x + Math.cos(angle) * 0.9D,
                    y + 0.3D,
                    z + Math.sin(angle) * 0.9D,
                    1,
                    0.05D,
                    0.05D,
                    0.05D,
                    0.04D
            );
        }

        // Soft base puff.
        world.spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y + 0.2D,
                z,
                6,
                0.2D,
                0.1D,
                0.2D,
                0.01D
        );
    }

    /** Spawns one flat ring of cloud particles at explicit angular positions. */
    private static void spawnRing(
            ServerWorld world,
            double x,
            double y,
            double z,
            double radius,
            int points
    ) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            world.spawnParticles(
                    ParticleTypes.CLOUD,
                    x + Math.cos(angle) * radius,
                    y,
                    z + Math.sin(angle) * radius,
                    1,
                    0.02D,
                    0.02D,
                    0.02D,
                    0.0D
            );
        }
    }

    private static void knockbackNearby(
            World world,
            double x,
            double y,
            double z,
            double radius,
            double knockbackBase,
            Entity excluded
    ) {
        Vec3d center = new Vec3d(x, y, z);
        Box area = new Box(
                x - radius,
                y - radius,
                z - radius,
                x + radius,
                y + radius,
                z + radius
        );
        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (!entity.isPushable() || entity == excluded) {
                continue;
            }

            // Vanilla explosion rule: falloff is measured from the entity's
            // FEET against the full burst radius, so a burst at your feet is
            // a point-blank hit (old code measured from mid-body, which cut
            // the launch of a ground burst in half).
            double distance = Math.sqrt(entity.squaredDistanceTo(center));
            if (distance >= radius) {
                continue;
            }

            // Push direction runs from the burst to the entity's eyes, so a
            // ground burst under you throws you straight up instead of the
            // old flat sideways shove with a fixed vertical pop.
            double dx = entity.getX() - x;
            double dy = entity.getEyeY() - y;
            double dz = entity.getZ() - z;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < 1.0E-4D) {
                dx = 0.0D;
                dy = 1.0D;
                dz = 0.0D;
            } else {
                dx /= length;
                dy /= length;
                dz /= length;
            }

            double strength = knockbackBase * (1.0D - distance / radius);
            // Line of sight: a burst behind a wall barely pushes you.
            strength *= Explosion.getExposure(center, entity);

            int blastProtection = EnchantmentHelper.getEquipmentLevel(
                    Enchantments.BLAST_PROTECTION,
                    entity
            );
            strength *= 1.0D - Math.min(20, blastProtection * 2) / 25.0D;
            double knockbackResistance = Math.max(
                    0.0D,
                    Math.min(
                            1.0D,
                            entity.getAttributeValue(
                                    EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE
                            )
                    )
            );
            strength *= 1.0D - knockbackResistance;

            if (strength <= 0.0D) {
                continue;
            }

            entity.setVelocity(
                    entity.getVelocity().add(
                            dx * strength,
                            dy * strength,
                            dz * strength
                    )
            );
            entity.velocityModified = true;

            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.networkHandler.sendPacket(
                        new EntityVelocityUpdateS2CPacket(player)
                );
                // Vanilla wind burst rule: fall damage accumulates only once
                // the player falls below the Y level of this burst.
                WindChargeFloors.setFloor(player, y);
            }
        }
    }

    private static void interactWithBlocks(
            World world,
            double x,
            double y,
            double z,
            double radius,
            Entity owner
    ) {
        int minX = MathHelper.floor(x - radius);
        int minY = MathHelper.floor(y - radius);
        int minZ = MathHelper.floor(z - radius);
        int maxX = MathHelper.floor(x + radius);
        int maxY = MathHelper.floor(y + radius);
        int maxZ = MathHelper.floor(z + radius);

        for (int blockX = minX; blockX <= maxX; blockX++) {
            for (int blockY = minY; blockY <= maxY; blockY++) {
                for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                    double dx = blockX + 0.5D - x;
                    double dy = blockY + 0.5D - y;
                    double dz = blockZ + 0.5D - z;

                    if (dx * dx + dy * dy + dz * dz
                            <= radius * radius) {
                        interactWithBlock(
                                world,
                                new BlockPos(blockX, blockY, blockZ),
                                owner
                        );
                    }
                }
            }
        }
    }

    private static void interactWithBlock(World world, BlockPos pos, Entity owner) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof AbstractButtonBlock) {
            AbstractButtonBlock button = (AbstractButtonBlock) block;
            if (!state.get(AbstractButtonBlock.POWERED)) {
                button.powerOn(state, world, pos);
            }
            return;
        }

        if (block instanceof LeverBlock) {
            world.setBlockState(
                    pos,
                    state.with(
                            LeverBlock.POWERED,
                            !state.get(LeverBlock.POWERED)
                    ),
                    3
            );
            world.playSound(
                    null,
                    pos,
                    SoundEvents.BLOCK_LEVER_CLICK,
                    SoundCategory.BLOCKS,
                    0.5F,
                    0.6F
            );
            return;
        }

        if (block instanceof DoorBlock && block != Blocks.IRON_DOOR) {
            if (state.get(DoorBlock.HALF)
                    == DoubleBlockHalf.UPPER) {
                return;
            }

            DoorBlock door = (DoorBlock) block;
            door.setOpen(
                    world,
                    state,
                    pos,
                    !state.get(DoorBlock.OPEN)
            );
            return;
        }

        if (block instanceof TrapdoorBlock && block != Blocks.IRON_TRAPDOOR) {
            boolean open = !state.get(TrapdoorBlock.OPEN);
            world.setBlockState(pos, state.with(TrapdoorBlock.OPEN, open), 3);
            world.playSound(
                    null,
                    pos,
                    open
                            ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN
                            : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE,
                    SoundCategory.BLOCKS,
                    1.0F,
                    0.9F
            );
            return;
        }

        if (block instanceof FenceGateBlock) {
            boolean open = !state.get(FenceGateBlock.OPEN);
            world.setBlockState(pos, state.with(FenceGateBlock.OPEN, open), 3);
            world.playSound(
                    null,
                    pos,
                    open
                            ? SoundEvents.BLOCK_FENCE_GATE_OPEN
                            : SoundEvents.BLOCK_FENCE_GATE_CLOSE,
                    SoundCategory.BLOCKS,
                    1.0F,
                    0.9F
            );
            return;
        }

        if (block instanceof BellBlock) {
            ((BellBlock) block).ring(world, pos, Direction.UP);
            return;
        }

        if (block instanceof ChorusFlowerBlock) {
            world.breakBlock(pos, true, owner, 512);
        }
    }
}
