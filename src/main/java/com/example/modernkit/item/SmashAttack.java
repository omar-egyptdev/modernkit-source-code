package com.example.modernkit.item;

import com.example.modernkit.enchantment.ModEnchantments;
import com.example.modernkit.entity.WindExplosion;
import com.example.modernkit.sound.ModSounds;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import java.util.List;

public final class SmashAttack {
    public static final float MIN_FALL = 1.5F;
    public static final double RADIUS = 3.5D;
    public static final float KNOCKBACK_SCALE = 0.3F;

    /**
     * Density (vanilla 1.21): adds 0.5 damage points per block fallen per level,
     * so level V adds 2.5 points per block.
     */
    public static final float DENSITY_DAMAGE_PER_BLOCK_PER_LEVEL = 0.5F;

    /**
     * Wind Burst (vanilla 1.21.2): the smash launches the attacker back up,
     * roughly 8 blocks at level I, 16 at II and 24 at III. These launch
     * velocities reproduce those heights with Minecraft's gravity/drag
     * (v' = (v - 0.08) * 0.98 per tick).
     */
    private static final double[] WIND_BURST_LAUNCH = new double[] { 1.3D, 2.0D, 2.6D };
    private static final double WIND_BURST_LAUNCH_BASE = 1.3D;
    private static final double WIND_BURST_LAUNCH_PER_LEVEL = 0.65D;

    /**
     * Wind Burst explosion knockback multipliers (vanilla 1.21 values:
     * 1.2x / 1.75x / 2.2x; further levels use 1.15 + 0.35 * level).
     */
    private static final float[] WIND_BURST_KNOCKBACK = new float[] { 1.2F, 1.75F, 2.2F };

    /** Wind Burst explosion radius (vanilla 1.21: same as the smash radius). */
    private static final double WIND_BURST_RADIUS = 3.5D;
    /** Base explosion knockback at zero distance (same scale as the Wind Charge burst). */
    private static final double WIND_BURST_KNOCKBACK_BASE = 1.4D;

    private SmashAttack() {}

    public static boolean canSmash(PlayerEntity player) {
        return player.getMainHandStack().getItem() == ModItems.MACE
                && player.fallDistance > MIN_FALL
                && !player.isFallFlying();
    }

    public static float bonusDamage(float fall) {
        if (fall <= 3.0F) return 4.0F * fall;
        if (fall <= 8.0F) return 12.0F + 2.0F * (fall - 3.0F);
        return 22.0F + (fall - 8.0F);
    }

    /**
     * Density: extra smash damage from the mace's Density enchantment.
     * 0.5 damage points per block fallen per level (vanilla 1.21 values).
     */
    public static float densityBonus(PlayerEntity player, float fall) {
        int level = EnchantmentHelper.getLevel(ModEnchantments.DENSITY, player.getMainHandStack());
        if (level <= 0) {
            return 0.0F;
        }
        return DENSITY_DAMAGE_PER_BLOCK_PER_LEVEL * level * fall;
    }

    /**
     * Wind Burst level on the held mace, or 0.
     */
    public static int windBurstLevel(LivingEntity attacker) {
        return EnchantmentHelper.getLevel(ModEnchantments.WIND_BURST, attacker.getMainHandStack());
    }

    /**
     * Wind Burst: after a successful smash, launches the attacker back into
     * the air, roughly 8 / 16 / 24 blocks at levels I / II / III (vanilla
     * 1.21.2), and creates a real wind burst explosion at the impact point:
     * nearby entities are pushed away (scaled by the vanilla knockback
     * multipliers) and redstone objects react, exactly like a Wind Charge.
     * Blast Protection reduces the launch, like vanilla. This does NOT negate
     * the fall damage from the following landing (same as vanilla).
     */
    public static void windBurstLaunch(LivingEntity attacker, Entity target, int level) {
        ServerWorld sw = (ServerWorld) attacker.world;

        double launch;
        if (level <= 3) {
            launch = WIND_BURST_LAUNCH[level - 1];
        } else {
            launch = WIND_BURST_LAUNCH_BASE + WIND_BURST_LAUNCH_PER_LEVEL * (level - 1);
        }

        // Vanilla wind burst knockback multiplier: 1.2 / 1.75 / 2.2 (1.15 + 0.35 * level beyond)
        float multiplier;
        if (level <= 3) {
            multiplier = WIND_BURST_KNOCKBACK[level - 1];
        } else {
            multiplier = 1.15F + 0.35F * level;
        }

        double x = target.getX();
        double y = target.getBodyY(0.5D);
        double z = target.getZ();

        // The full wind burst explosion at the impact point. The attacker is
        // excluded from the generic push and gets the controlled launch below.
        WindExplosion.burst(
                attacker.world,
                x,
                y,
                z,
                WIND_BURST_RADIUS,
                WIND_BURST_KNOCKBACK_BASE * multiplier,
                attacker,
                attacker,
                ModSounds.WIND_CHARGE_BURST,
                1.0F,
                1.0F
        );

        // Controlled vertical launch for the attacker, reduced by Blast Protection.
        int blastProtection = EnchantmentHelper.getEquipmentLevel(Enchantments.BLAST_PROTECTION, attacker);
        launch *= 1.0D - Math.min(20, blastProtection * 2) / 25.0D;

        attacker.setVelocity(attacker.getVelocity().x, launch, attacker.getVelocity().z);
        if (attacker instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) attacker).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(attacker));
        }
    }

    public static void doEffects(PlayerEntity player, Entity target, float fall) {
        if (player.world.isClient) return;
        ServerWorld sw = (ServerWorld) player.world;
        double y = target.getBodyY(0.5D);

        // Particles - 1.16.5 doesn't have gust/wind burst, so we fake it
        sw.spawnParticles(ParticleTypes.EXPLOSION, target.getX(), y, target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        sw.spawnParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.2D, target.getZ(), 12, 0.6D, 0.2D, 0.6D, 0.05D);
        sw.spawnParticles(ParticleTypes.CRIT, target.getX(), y, target.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.2D);
        if (fall > 5.0F) {
            sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), y, target.getZ(), 2, 0.6D, 0.2D, 0.6D, 0.0D);
        }

        // Sounds - closest vanilla equivalents to the real mace sounds
        sw.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1.0F, 0.6F);
        sw.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.7F, 1.2F);
        if (fall > 5.0F) {
            sw.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, 0.8F, 0.7F);
        }
    }

    public static void knockbackNearby(PlayerEntity player, Entity target, float fall) {
        Box box = target.getBoundingBox().expand(RADIUS);
        List<LivingEntity> list = player.world.getEntitiesByClass(LivingEntity.class, box, e ->
                e != player
                        && e != target
                        && !player.isTeammate(e)
                        && !(e instanceof ArmorStandEntity && ((ArmorStandEntity) e).isMarker())
                        && !(e instanceof TameableEntity && ((TameableEntity) e).isOwner(player))
                        && target.squaredDistanceTo(e) <= RADIUS * RADIUS);

        for (LivingEntity e : list) {
            double dx = e.getX() - target.getX();
            double dz = e.getZ() - target.getZ();
            double dist = Math.sqrt(target.squaredDistanceTo(e));
            float strength = (float) ((RADIUS - dist) * KNOCKBACK_SCALE);
            if (fall > 5.0F) strength *= 2.0F;
            if (strength <= 0.0F) continue;

            e.takeKnockback(strength, -dx, -dz);
            e.velocityModified = true;
            if (e instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) e).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(e));
            }
        }
    }
}
