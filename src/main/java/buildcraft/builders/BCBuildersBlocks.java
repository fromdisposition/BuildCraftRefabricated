/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.fabric.registry.DeferredBlock;
import buildcraft.fabric.registry.DeferredRegister;

import buildcraft.builders.block.BlockArchitectTable;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.block.BlockElectronicLibrary;
import buildcraft.builders.block.BlockFiller;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.block.BlockReplacer;

public class BCBuildersBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BCBuilders.MODID);

    public static final DeferredBlock<BlockFrame> FRAME = BLOCKS.registerBlock(
            "frame",
            BlockFrame::new, () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockFiller> FILLER = BLOCKS.registerBlock(
            "filler",
            BlockFiller::new, () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockBuilder> BUILDER = BLOCKS.registerBlock(
            "builder",
            BlockBuilder::new, () -> BlockBehaviour.Properties.of()
                .strength(5.0f, 10.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockArchitectTable> ARCHITECT = BLOCKS.registerBlock(
            "architect",
            BlockArchitectTable::new, () -> BlockBehaviour.Properties.of()
                .strength(5.0f, 10.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockElectronicLibrary> LIBRARY = BLOCKS.registerBlock(
            "library",
            BlockElectronicLibrary::new, () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockReplacer> REPLACER = BLOCKS.registerBlock(
            "replacer",
            BlockReplacer::new, () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockQuarry> QUARRY = BLOCKS.registerBlock(
            "quarry",
            BlockQuarry::new, () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.ANVIL).requiresCorrectToolForDrops());

    public static void register() {
        BLOCKS.register();
    }
}

