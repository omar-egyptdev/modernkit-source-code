package com.example.modernkit.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

/**
 * Swift Sneak: leggings only (including ModernKit leggings), levels I-III.
 * Treasure enchantment, loot-only like in newer versions. The speed boost is in SwiftSneakMixin.
 */
public class SwiftSneakEnchantment extends Enchantment {

    public SwiftSneakEnchantment() {
        super(Enchantment.Rarity.VERY_RARE, EnchantmentTarget.ARMOR_LEGS, new EquipmentSlot[] { EquipmentSlot.LEGS });
    }

    @Override public int getMaxLevel() { return 3; }
    @Override public int getMinPower(int level) { return 25 + (level - 1) * 25; }
    @Override public int getMaxPower(int level) { return 75 + (level - 1) * 25; }

    @Override public boolean isTreasure() { return true; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return false; }
    @Override public boolean isAvailableForRandomSelection() { return false; }
}
