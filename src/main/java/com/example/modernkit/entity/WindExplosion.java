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
import net.minecraft.world.World;

import java.util.List;

/**
 * Shared wind-burst explosion behavior, used by both the Wind Charge projectile
 * and the Wind Burst mace enchantment. Like in newer versions, the burst is a
 * knockback-only explosion that also presses buttons, flips levers, opens
 * wooden doors/trapdoors/fence gates, rings bells and breaks chorus flowers,
 * while respecting Blast Protection and knockback resistance.
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
        serverWorld.spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                24,
                0.45D,
                0.35D,
                0.45D,
                0.12D
        );
        serverWorld.spawnParticles(
                ParticleTypes.POOF,
                x,
                y,
                z,
                8,
                0.25D,
                0.25D,
                0.25D,
                0.04D
        );

        interactWithBlocks(world, x, y, z, radius, owner);
        knockbackNearby(world, x, y, z, radius, knockbackBase, excluded);
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

            double dx = entity.getX() - x;
            double dy = entity.getBodyY(0.5D) - y;
            double dz = entity.getZ() - z;
            double distanceSquared = dx * dx + dy * dy + dz * dz;

            if (distanceSquared >= radius * radius) {
                continue;
            }

            double distance = Math.sqrt(distanceSquared);
            double strength = knockbackBase * (1.0D - distance / radius);
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

            double horizontalLength = Math.sqrt(dx * dx + dz * dz);
            double horizontalScale =
                    horizontalLength < 1.0E-6D
                            ? 0.0D
                            : strength / horizontalLength;
            double vertical = 0.45D + strength * 0.65D;

            entity.setVelocity(
                    entity.getVelocity().add(
                            dx * horizontalScale,
                            vertical,
                            dz * horizontalScale
                    )
            );
            entity.velocityModified = true;

            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.networkHandler.sendPacket(
                        new EntityVelocityUpdateS2CPacket(player)
                );
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
