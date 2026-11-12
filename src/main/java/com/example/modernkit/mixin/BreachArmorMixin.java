package com.example.modernkit.mixin;

import com.example.modernkit.enchantment.ModEnchantments;
import com.example.modernkit.item.MaceItem;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Breach (mace, levels I-IV): the target's armor is 15% less effective per
 * level - vanilla 1.21 reduces the damage reduction that armor itself
 * provides by 15 percentage points per level, min 0.
 *
 * This hooks the armor phase of damage calculation (applyArmorToDamage), so:
 * - it also works against natural mob armor points (shulkers, etc.), and
 * - it does NOT affect Protection enchantments or the Resistance effect,
 *   which are applied later by applyEnchantmentsToDamage - exactly like vanilla.
 *
 * The reduction is implemented on the same scale vanilla 1.16.5 uses internally:
 * effective armor points (0-20, where 20 = 80% damage reduction), so 15
 * percentage points = 15/25 = 3.75 armor points per Breach level.
 */
@Mixin(LivingEntity.class)
public abstract class BreachArmorMixin {

    @Inject(method = "applyArmorToDamage", at = @At("RETURN"), cancellable = true)
    private void modernkit$breach(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (source.bypassesArmor() || amount <= 0.0F) {
            return;
        }
        if (!(source.getAttacker() instanceof LivingEntity)) {
            return;
        }

        ItemStack weapon = ((LivingEntity) source.getAttacker()).getMainHandStack();
        if (!(weapon.getItem() instanceof MaceItem)) {
            return;
        }

        int level = EnchantmentHelper.getLevel(ModEnchantments.BREACH, weapon);
        if (level <= 0) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;

        // Re-run vanilla's armor formula (DamageUtil.getDamageLeft), then remove
        // 15 percentage points of armor reduction per level.
        float armor = self.getArmor();
        float toughness = (float) self.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        float divisor = 2.0F + toughness / 4.0F;
        float effective = MathHelper.clamp(armor - amount / divisor, armor * 0.2F, 20.0F);
        float reduced = Math.max(0.0F, effective - level * 3.75F);

        cir.setReturnValue(amount * (1.0F - reduced / 25.0F));
    }
}
