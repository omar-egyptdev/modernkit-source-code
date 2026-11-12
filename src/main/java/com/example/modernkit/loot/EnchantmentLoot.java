package com.example.modernkit.loot;

import com.example.modernkit.ModernKitMod;

import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;

import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

/**
 * Chest loot for the treasure enchantments that 1.16.5 cannot generate:
 * - Wind Burst: vanilla 1.21.4 drops a level I book from ominous vaults
 *   (5.5%); here it is placed in the closest 1.16.5 endgame treasure chests.
 * - Swift Sneak: vanilla drops a random-level book from ancient city chests
 *   (23.7%); ancient cities do not exist in 1.16.5, so the closest analogs
 *   get smaller chances.
 */
public final class EnchantmentLoot {

    private EnchantmentLoot() {
    }

    public static void register() {

        LootTableLoadingCallback.EVENT.register(
                (resourceManager, manager, id, supplier, setter) -> {

                    // --------------------------------------
                    // WIND BURST I (loot-only in vanilla)
                    // --------------------------------------

                    if (matches(id, "minecraft:chests/bastion_treasure")) {
                        supplier.pool(singleBookPool("minecraft:wind_burst", 1, 0.055F));
                    }

                    if (matches(id, "minecraft:chests/end_city_treasure")) {
                        supplier.pool(singleBookPool("minecraft:wind_burst", 1, 0.06F));
                    }

                    // --------------------------------------
                    // SWIFT SNEAK I-III (loot-only in vanilla)
                    // --------------------------------------

                    if (matches(id, "minecraft:chests/stronghold_library")) {
                        supplier.pool(swiftSneakPool(0.10F));
                    }

                    if (matches(id, "minecraft:chests/simple_dungeon")) {
                        supplier.pool(swiftSneakPool(0.08F));
                    }
                }
        );

        ModernKitMod.LOGGER.info(
                "ModernKit Wind Burst / Swift Sneak structure loot registered."
        );
    }

    private static boolean matches(
            Identifier actual,
            String expected
    ) {
        return actual.equals(new Identifier(expected));
    }

    /**
     * Builds the StoredEnchantments tag for an enchanted book.
     */
    private static NbtCompound bookTag(String enchantmentId, int level) {

        NbtCompound enchantment = new NbtCompound();

        enchantment.putString(
                "id",
                enchantmentId
        );

        enchantment.putShort(
                "lvl",
                (short) level
        );

        NbtList storedEnchantments = new NbtList();

        storedEnchantments.add(enchantment);

        NbtCompound bookTag = new NbtCompound();

        bookTag.put(
                "StoredEnchantments",
                storedEnchantments
        );

        return bookTag;
    }

    /**
     * Adds one extra optional enchanted-book pool to an existing table.
     */
    private static LootPool.Builder singleBookPool(
            String enchantmentId,
            int level,
            float chance
    ) {

        return LootPool.builder()
                .with(
                        ItemEntry.builder(Items.ENCHANTED_BOOK)
                                .apply(
                                        SetNbtLootFunction.builder(
                                                bookTag(enchantmentId, level)
                                        )
                                )
                )
                .conditionally(
                        RandomChanceLootCondition.builder(chance)
                );
    }

    /**
     * Adds a randomly weighted Swift Sneak I, II or III book
     * (vanilla ancient cities offer any level).
     */
    private static LootPool.Builder swiftSneakPool(float chance) {

        LootPool.Builder pool = LootPool.builder()
                .conditionally(
                        RandomChanceLootCondition.builder(chance)
                );

        pool.with(
                ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(1)
                        .apply(
                                SetNbtLootFunction.builder(
                                        bookTag("minecraft:swift_sneak", 1)
                                )
                        )
        );

        pool.with(
                ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(1)
                        .apply(
                                SetNbtLootFunction.builder(
                                        bookTag("minecraft:swift_sneak", 2)
                                )
                        )
        );

        pool.with(
                ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(1)
                        .apply(
                                SetNbtLootFunction.builder(
                                        bookTag("minecraft:swift_sneak", 3)
                                )
                        )
        );

        return pool;
    }
}
