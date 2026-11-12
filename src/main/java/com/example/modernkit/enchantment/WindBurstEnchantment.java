package com.example.modernkit.enchantment;

import com.example.modernkit.item.MaceItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Wind Burst: mace only, levels I-III. Treasure enchantment: never at the enchanting table,
 * in librarian trades or in random loot (same approach vanilla uses for Soul Speed).
 * It is added to specific chests by EnchantmentLoot.
 */
public class WindBurstEnchantment extends Enchantment {

    public WindBurstEnchantment() {
        super(Enchantment.Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override public int getMaxLevel() { return 3; }
    @Override public int getMinPower(int level) { return 15 + (level - 1) * 9; }
    @Override public int getMaxPower(int level) { return 65 + (level - 1) * 9; }

    @Override public boolean isTreasure() { return true; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return false; }
    @Override public boolean isAvailableForRandomSelection() { return false; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof MaceItem;
    }
}
