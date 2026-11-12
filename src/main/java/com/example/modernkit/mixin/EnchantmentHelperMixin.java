package com.example.modernkit.mixin;

import com.example.modernkit.item.MaceItem;
import com.example.modernkit.item.SpearItem;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Lets the enchanting table offer enchantments to maces and spears.
 * Vanilla only checks the item's enchantment target (swords etc.), so we add the rest here
 * using the same power ranges and rules as vanilla.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(method = "getPossibleEntries", at = @At("RETURN"))
    private static void modernkit$addEntries(int power, ItemStack stack, boolean treasureAllowed,
                                             CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (!(stack.getItem() instanceof MaceItem) && !(stack.getItem() instanceof SpearItem)) {
            return;
        }

        List<EnchantmentLevelEntry> list = cir.getReturnValue();

        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.isTreasure() && !treasureAllowed) {
                continue;
            }
            if (!enchantment.isAvailableForRandomSelection() || !enchantment.isAcceptableItem(stack)) {
                continue;
            }

            boolean present = false;
            for (EnchantmentLevelEntry entry : list) {
                if (entry.enchantment == enchantment) {
                    present = true;
                    break;
                }
            }
            if (present) {
                continue;
            }

            for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); --level) {
                if (power >= enchantment.getMinPower(level) && power <= enchantment.getMaxPower(level)) {
                    list.add(new EnchantmentLevelEntry(enchantment, level));
                    break;
                }
            }
        }
    }
}
