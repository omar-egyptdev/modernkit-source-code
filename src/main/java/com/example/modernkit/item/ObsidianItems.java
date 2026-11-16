package com.example.modernkit.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ObsidianItems {
    private ObsidianItems() {}

    public static final Item OBSIDIAN_SWORD = new SwordItem(
            ObsidianToolMaterial.SWORD,
            ObsidianToolMaterial.SWORD_ATTACK_BONUS,
            ObsidianToolMaterial.SWORD_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.COMBAT));

    public static final Item OBSIDIAN_PICKAXE = new PickaxeItem(
            ObsidianToolMaterial.PICKAXE,
            ObsidianToolMaterial.PICKAXE_ATTACK_BONUS,
            ObsidianToolMaterial.PICKAXE_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.TOOLS)) {};

    public static final Item OBSIDIAN_AXE = new AxeItem(
            ObsidianToolMaterial.AXE,
            ObsidianToolMaterial.AXE_ATTACK_BONUS,
            ObsidianToolMaterial.AXE_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.TOOLS)) {};

    public static final Item OBSIDIAN_SHOVEL = new ShovelItem(
            ObsidianToolMaterial.SHOVEL,
            ObsidianToolMaterial.SHOVEL_ATTACK_BONUS,
            ObsidianToolMaterial.SHOVEL_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.TOOLS));

    public static final Item OBSIDIAN_HELMET = armor(EquipmentSlot.HEAD);
    public static final Item OBSIDIAN_CHESTPLATE = armor(EquipmentSlot.CHEST);
    public static final Item OBSIDIAN_LEGGINGS = armor(EquipmentSlot.LEGS);
    public static final Item OBSIDIAN_BOOTS = armor(EquipmentSlot.FEET);

    private static Item armor(EquipmentSlot slot) {
        return new ArmorItem(ObsidianArmorMaterial.INSTANCE, slot,
                new Item.Settings().group(ItemGroup.COMBAT));
    }

    public static void register() {
        register("obsidian_sword", OBSIDIAN_SWORD);
        register("obsidian_pickaxe", OBSIDIAN_PICKAXE);
        register("obsidian_axe", OBSIDIAN_AXE);
        register("obsidian_shovel", OBSIDIAN_SHOVEL);
        register("obsidian_helmet", OBSIDIAN_HELMET);
        register("obsidian_chestplate", OBSIDIAN_CHESTPLATE);
        register("obsidian_leggings", OBSIDIAN_LEGGINGS);
        register("obsidian_boots", OBSIDIAN_BOOTS);
    }

    private static void register(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier("minecraft", name), item);
    }
}
