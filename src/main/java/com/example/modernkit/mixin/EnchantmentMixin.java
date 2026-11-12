package com.example.modernkit.mixin;

import com.example.modernkit.item.MaceItem;
import com.example.modernkit.item.SpearItem;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes vanilla weapon enchantments available to ModernKit weapons.
 * Spears: Sharpness, Smite, Bane, Knockback, Fire Aspect, Looting (not Sweeping Edge).
 * Mace: Smite, Bane of Arthropods, Fire Aspect (Mending, Unbreaking and Vanishing already work).
 */
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    private void modernkit$allowModernEnchantments(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null) {
            return;
        }

        Enchantment self = (Enchantment) (Object) this;

        if (stack.getItem() instanceof SpearItem) {
            boolean supported =
                    self == Enchantments.SHARPNESS
                    || self == Enchantments.SMITE
                    || self == Enchantments.BANE_OF_ARTHROPODS
                    || self == Enchantments.KNOCKBACK
                    || self == Enchantments.FIRE_ASPECT
                    || self == Enchantments.LOOTING;
            if (supported) {
                cir.setReturnValue(true);
            }
        } else if (stack.getItem() instanceof MaceItem) {
            boolean supported =
                    self == Enchantments.SMITE
                    || self == Enchantments.BANE_OF_ARTHROPODS
                    || self == Enchantments.FIRE_ASPECT;
            if (supported) {
                cir.setReturnValue(true);
            }
        }
    }
}
