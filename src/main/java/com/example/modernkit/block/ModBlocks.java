package com.example.modernkit.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.Material;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * ModernKit blocks. Heavy Core is a real placeable block whose item form keeps
 * the "minecraft:heavy_core" id, so recipes and loot keep working unchanged.
 *
 * v1.5.9 adds obsidian + rainbow building blocks and vanilla-material slabs.
 * All doors use Material.STONE so they can be opened by hand (vanilla
 * DoorBlock refuses hand-opening for Material.METAL) and stay zombie-proof.
 */
public final class ModBlocks {
    private ModBlocks() {}

    public static final Block HEAVY_CORE = new Block(AbstractBlock.Settings.of(Material.METAL)
            .strength(5.0F, 6.0F)
            .sounds(BlockSoundGroup.METAL));

    public static final Block OBSIDIAN_SLAB = new SlabBlock(AbstractBlock.Settings.of(Material.STONE)
            .strength(50.0F, 1200.0F)
            .sounds(BlockSoundGroup.STONE));

    public static final Block OBSIDIAN_STAIRS = new StairsBlock(Blocks.OBSIDIAN.getDefaultState(),
            AbstractBlock.Settings.of(Material.STONE)
                    .strength(50.0F, 1200.0F)
                    .sounds(BlockSoundGroup.STONE)) {};

    public static final Block OBSIDIAN_DOOR = new DoorBlock(AbstractBlock.Settings.of(Material.STONE)
            .strength(3.0F, 1200.0F)
            .sounds(BlockSoundGroup.STONE)
            .nonOpaque()) {};

    public static final Block RAINBOW_BLOCK = new Block(AbstractBlock.Settings.of(Material.METAL)
            .strength(5.0F, 6.0F)
            .sounds(BlockSoundGroup.METAL));

    public static final Block RAINBOW_SLAB = new SlabBlock(AbstractBlock.Settings.of(Material.METAL)
            .strength(5.0F, 6.0F)
            .sounds(BlockSoundGroup.METAL));

    public static final Block RAINBOW_DOOR = new DoorBlock(AbstractBlock.Settings.of(Material.STONE)
            .strength(3.0F, 6.0F)
            .sounds(BlockSoundGroup.METAL)
            .nonOpaque()) {};

    public static final Block GOLD_SLAB = new SlabBlock(AbstractBlock.Settings.of(Material.METAL)
            .strength(3.0F, 6.0F)
            .sounds(BlockSoundGroup.METAL));

    public static final Block DIAMOND_SLAB = new SlabBlock(AbstractBlock.Settings.of(Material.METAL)
            .strength(5.0F, 6.0F)
            .sounds(BlockSoundGroup.METAL));

    public static final Block EMERALD_SLAB = new SlabBlock(AbstractBlock.Settings.of(Material.METAL)
            .strength(5.0F, 6.0F)
            .sounds(BlockSoundGroup.METAL));

    public static final Block COAL_SLAB = new SlabBlock(AbstractBlock.Settings.of(Material.STONE)
            .strength(5.0F, 6.0F)
            .sounds(BlockSoundGroup.STONE));

    public static final Block REDSTONE_SLAB = new SlabBlock(AbstractBlock.Settings.of(Material.STONE)
            .strength(5.0F, 6.0F)
            .sounds(BlockSoundGroup.STONE));

    public static void register() {
        block("heavy_core", HEAVY_CORE);

        block("obsidian_slab", OBSIDIAN_SLAB);
        block("obsidian_stairs", OBSIDIAN_STAIRS);
        block("obsidian_door", OBSIDIAN_DOOR);

        block("rainbow_block", RAINBOW_BLOCK);
        block("rainbow_slab", RAINBOW_SLAB);
        block("rainbow_door", RAINBOW_DOOR);

        block("gold_slab", GOLD_SLAB);
        block("diamond_slab", DIAMOND_SLAB);
        block("emerald_slab", EMERALD_SLAB);
        block("coal_slab", COAL_SLAB);
        block("redstone_slab", REDSTONE_SLAB);
    }

    private static void block(String name, Block block) {
        Identifier id = new Identifier("minecraft", name);
        Registry.register(Registry.BLOCK, id, block);
        Registry.register(Registry.ITEM, id, new BlockItem(block,
                new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
    }
}
