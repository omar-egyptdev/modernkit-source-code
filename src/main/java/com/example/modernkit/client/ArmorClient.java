package com.example.modernkit.client;

import com.example.modernkit.item.EmeraldItems;
import com.example.modernkit.item.RainbowItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderingRegistry;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ArmorClient implements ClientModInitializer {
    private static EmeraldArmorModel emeraldOuter;
    private static EmeraldArmorModel emeraldInner;
    private static RainbowArmorModel rainbowOuter;
    private static RainbowArmorModel rainbowInner;

    @Override
    public void onInitializeClient() {
        ArmorRenderingRegistry.registerModel(ArmorClient::emeraldModel,
                EmeraldItems.EMERALD_HELMET,
                EmeraldItems.EMERALD_CHESTPLATE,
                EmeraldItems.EMERALD_LEGGINGS,
                EmeraldItems.EMERALD_BOOTS);

        ArmorRenderingRegistry.registerModel(ArmorClient::rainbowModel,
                RainbowItems.RAINBOW_HELMET,
                RainbowItems.RAINBOW_CHESTPLATE,
                RainbowItems.RAINBOW_LEGGINGS,
                RainbowItems.RAINBOW_BOOTS);
    }

    private static BipedEntityModel<LivingEntity> emeraldModel(LivingEntity entity, ItemStack stack,
                                                               EquipmentSlot slot, BipedEntityModel<LivingEntity> defaultModel) {
        if (slot == EquipmentSlot.LEGS) {
            if (emeraldInner == null) {
                emeraldInner = new EmeraldArmorModel(0.5F, true);
            }
            return emeraldInner;
        }
        if (emeraldOuter == null) {
            emeraldOuter = new EmeraldArmorModel(1.0F, false);
        }
        return emeraldOuter;
    }

    private static BipedEntityModel<LivingEntity> rainbowModel(LivingEntity entity, ItemStack stack,
                                                               EquipmentSlot slot, BipedEntityModel<LivingEntity> defaultModel) {
        if (slot == EquipmentSlot.LEGS) {
            if (rainbowInner == null) {
                rainbowInner = new RainbowArmorModel(0.5F, true);
            }
            return rainbowInner;
        }
        if (rainbowOuter == null) {
            rainbowOuter = new RainbowArmorModel(1.0F, false);
        }
        return rainbowOuter;
    }
}
