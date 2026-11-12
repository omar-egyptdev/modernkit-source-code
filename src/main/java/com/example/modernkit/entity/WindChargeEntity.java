package com.example.modernkit.entity;

import com.example.modernkit.item.ModItems;
import com.example.modernkit.sound.ModSounds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WindChargeEntity extends ThrownItemEntity {

    private static final double BURST_RADIUS = 2.4D;

    public WindChargeEntity(
            EntityType<? extends WindChargeEntity> type,
            World world
    ) {
        super(type, world);
    }

    public WindChargeEntity(World world, LivingEntity owner) {
        super(ModEntities.WIND_CHARGE, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.WIND_CHARGE;
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (world.isClient) {
            return true;
        }

        Entity deflector = source.getAttacker();
        if (deflector == null) {
            return false;
        }

        Vec3d direction = deflector.getRotationVec(1.0F);
        Entity sourceEntity = source.getSource();
        if (sourceEntity instanceof ProjectileEntity) {
            Vec3d projectileVelocity = sourceEntity.getVelocity();
            if (projectileVelocity.lengthSquared() > 1.0E-6D) {
                direction = projectileVelocity.normalize();
            }
        }

        if (direction.lengthSquared() < 1.0E-6D) {
            return false;
        }

        setOwner(deflector);
        setVelocity(direction.normalize().multiply(1.5D));
        velocityModified = true;
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        if (!world.isClient) {
            Entity target = hit.getEntity();
            target.damage(
                    DamageSource.thrownProjectile(this, getOwner()),
                    1.0F
            );
            burst(target.getX(), target.getBodyY(0.5D), target.getZ());
        }

        remove();
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        if (!world.isClient) {
            Vec3d impact = hit.getPos();
            burst(impact.x, impact.y, impact.z);
        }

        remove();
    }

    private void burst(double x, double y, double z) {
        WindExplosion.burst(
                world,
                x,
                y,
                z,
                BURST_RADIUS,
                1.4D,
                getOwner(),
                null,
                ModSounds.WIND_CHARGE_BURST,
                0.8F,
                0.9F + random.nextFloat() * 0.2F
        );
    }
}

