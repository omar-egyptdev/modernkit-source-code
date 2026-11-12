package com.example.modernkit.mixin;

import com.example.modernkit.enchantment.ModEnchantments;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Swift Sneak (leggings, levels I-III): sneaking (and crawling) speed becomes
 * 30% + 15% of walking speed per level, capped at walking speed - so level III
 * sneaks at 75% of walking speed, like vanilla.
 *
 * In 1.16.5 the sneak slowdown is applied in the client keyboard input
 * (KeyboardInput.tick multiplies the input by 0.3 while sneaking), so this
 * raises that factor for players wearing Swift Sneak leggings. Sneaking is
 * always slower than walking, so server-side movement checks are unaffected.
 */
@Mixin(KeyboardInput.class)
public abstract class SwiftSneakMixin {

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.3D))
    private double modernkit$swiftSneak(double original) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return original;
        }

        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        int level = EnchantmentHelper.getLevel(ModEnchantments.SWIFT_SNEAK, leggings);
        if (level <= 0) {
            return original;
        }

        return Math.min(1.0D, original + 0.15D * level);
    }
}
