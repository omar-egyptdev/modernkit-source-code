package com.example.modernkit.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Registration for ModernKit enchantments (all under the minecraft: namespace).
 */
public final class ModEnchantments {

    public static Enchantment LUNGE;
    public static Enchantment DENSITY;
    public static Enchantment BREACH;
    public static Enchantment WIND_BURST;
    public static Enchantment SWIFT_SNEAK;

    private ModEnchantments() {
    }

    public static void register() {
        if (LUNGE != null) {
            return;
        }
        LUNGE = reg("lunge", new LungeEnchantment());
        DENSITY = reg("density", new DensityEnchantment());
        BREACH = reg("breach", new BreachEnchantment());
        WIND_BURST = reg("wind_burst", new WindBurstEnchantment());
        SWIFT_SNEAK = reg("swift_sneak", new SwiftSneakEnchantment());
    }

    private static Enchantment reg(String name, Enchantment enchantment) {
        return Registry.register(Registry.ENCHANTMENT, new Identifier("minecraft", name), enchantment);
    }
}
