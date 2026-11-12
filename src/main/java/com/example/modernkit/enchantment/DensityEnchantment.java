package com.example.modernkit.enchantment;

import com.example.modernkit.item.MaceItem;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Density: mace only, levels I-V. Extends DamageEnchantment so it is automatically
 * exclusive with Smite, Bane of Arthropods, Sharpness and Breach, like in newer versions.
 * The extra smash damage is applied in SmashAttack.
 */
public class DensityEnchantment extends DamageEnchantment {

    public DensityEnchantment() {
        super(Enchantment.Rarity.UNCOMMON, 3, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override public int getMaxLevel() { return 5; }
    @Override public int getMinPower(int level) { return 5 + (level - 1) * 8; }
    @Override public int getMaxPower(int level) { return 25 + (level - 1) * 8; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof MaceItem;
    }
}
