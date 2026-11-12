package com.example.modernkit.enchantment;

import com.example.modernkit.item.MaceItem;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Breach: mace only, levels I-IV, exclusive with Density/Smite/Bane/Sharpness.
 * Armor is 15% less effective per level (applied in BreachArmorMixin).
 */
public class BreachEnchantment extends DamageEnchantment {

    public BreachEnchantment() {
        super(Enchantment.Rarity.RARE, 3, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override public int getMaxLevel() { return 4; }
    @Override public int getMinPower(int level) { return 15 + (level - 1) * 9; }
    @Override public int getMaxPower(int level) { return 65 + (level - 1) * 9; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof MaceItem;
    }
}
