package com.example.modernkit.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * Redstone Slab: redstone wire and components treat it like a full solid block,
 * in both the single slab state and the doubled (full cube) state.
 *
 * Two vanilla checks normally reject slabs:
 * - Redstone wire placement and wire climbing use SideShapeType.FULL
 *   (BlockState.isSideSolidFullSquare), which reads the block's getSidesShape.
 *   Vanilla slabs report their half-cube outline shape, so the check fails.
 *   This block reports a full cube, so dust can be placed on the slab and wire
 *   climbs onto it.
 * - Power flowing through the block (World.getEmittedRedstonePower) and the
 *   wire's slope checks gate on BlockState.isSolidBlock, which is configured
 *   with AbstractBlock.Settings.solidBlock. The block is built with a predicate
 *   that always returns true (see ModBlocks).
 */
public class RedstoneSlabBlock extends SlabBlock {

    public RedstoneSlabBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    /**
     * Full cube sides so the slab passes the SideShapeType.FULL checks that
     * redstone wire (and torches, levers, ...) use for attachment.
     */
    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.fullCube();
    }
}
