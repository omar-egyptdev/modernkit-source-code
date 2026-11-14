package com.example.modernkit.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * ModernKit blocks. Heavy Core is a real placeable block whose item form keeps
 * the "minecraft:heavy_core" id, so recipes and loot keep working unchanged.
 */
public final class ModBlocks {
    private ModBlocks() {}

    public static final Block HEAVY_CORE = new Block(AbstractBlock.Settings.of(Material.METAL)
            .strength(5.0F, 6.0F)
            .sounds(BlockSoundGroup.METAL));

    public static void register() {
        Registry.register(Registry.BLOCK, new Identifier("minecraft", "heavy_core"), HEAVY_CORE);
        Registry.register(Registry.ITEM, new Identifier("minecraft", "heavy_core"),
                new BlockItem(HEAVY_CORE, new Item.Settings().group(ItemGroup.MISC)));
    }
}
