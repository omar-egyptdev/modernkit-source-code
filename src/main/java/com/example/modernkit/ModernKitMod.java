package com.example.modernkit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.modernkit.item.ModItems;
import com.example.modernkit.block.ModBlocks;
import com.example.modernkit.item.RainbowItems;
import com.example.modernkit.item.EmeraldItems;
import com.example.modernkit.item.ObsidianItems;
import com.example.modernkit.entity.ModEntities;
import com.example.modernkit.entity.WindChargeFloors;
import com.example.modernkit.loot.EnchantmentLoot;
import com.example.modernkit.loot.SpearWorldLoot;
import com.example.modernkit.enchantment.ModEnchantments;
import com.example.modernkit.network.SpearPackets;
import com.example.modernkit.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;

public class ModernKitMod implements ModInitializer {
    public static final String MOD_ID = "modernkit";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEntities.register();
        ModSounds.register();
        ModItems.register();
        ModBlocks.register();
        RainbowItems.register();
        EmeraldItems.register();
        ObsidianItems.register();
        ModEnchantments.register();
        SpearWorldLoot.register();
        EnchantmentLoot.register();
        SpearPackets.register();

        // Unlock the command block recipe for every player on join. This makes
        // it visible in the recipe book and supports doLimitedCrafting worlds.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                handler.player.unlockRecipes(new Identifier[]{
                        new Identifier(MOD_ID, "command_block")
                }));

        ServerTickEvents.END_SERVER_TICK.register(WindChargeFloors::tick);
    }
}
