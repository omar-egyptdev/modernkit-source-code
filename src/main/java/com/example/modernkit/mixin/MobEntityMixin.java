package com.example.modernkit.mixin;

import com.example.modernkit.combat.SpearMobCombat;
import com.example.modernkit.item.MaceItem;
import com.example.modernkit.item.SmashAttack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds server-side spear combat to supported mobs.
 *
 * Prevents the ordinary melee attack from bypassing the
 * custom spear charge controller.
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void modernkit$tickSpearMobCombat(CallbackInfo ci) {

        SpearMobCombat.tick(
                (MobEntity) (Object) this
        );
    }

    @Inject(
            method = "tryAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modernkit$interceptSpearMobAttack(
            Entity target,
            CallbackInfoReturnable<Boolean> cir
    ) {

        MobEntity mob = (MobEntity) (Object) this;

        if (SpearMobCombat.shouldOverrideVanillaAttack(mob)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Wind Burst works for any living creature attacking with a mace, not
     * just players - like vanilla, where a fox pouncing with a Wind Burst
     * mace activates it. Fires after a successful mob melee attack when the
     * mob is falling at least 1.5 blocks (the smash attack threshold).
     */
    @Inject(
            method = "tryAttack",
            at = @At("TAIL")
    )
    private void modernkit$windBurstMobAttack(
            Entity target,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue() || target == null) {
            return;
        }

        MobEntity mob = (MobEntity) (Object) this;
        if (mob.world.isClient) {
            return;
        }

        ItemStack weapon = mob.getMainHandStack();
        if (!(weapon.getItem() instanceof MaceItem)) {
            return;
        }

        if (mob.fallDistance <= SmashAttack.MIN_FALL || mob.isFallFlying()) {
            return;
        }

        int windBurst = SmashAttack.windBurstLevel(mob);
        if (windBurst > 0) {
            SmashAttack.windBurstLaunch(mob, target, windBurst);
        }
    }
}
