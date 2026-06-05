/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.blocks.CustomPaintHelper;
import buildcraft.api.blocks.ICustomPaintHandler;

public class VanillaPaintHandlers {

    public static void init() {

        registerColorFamily(Blocks.GLASS,
            Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS,
            Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS,
            Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS,
            Blocks.BLACK_STAINED_GLASS
        );

        registerColorFamily(Blocks.GLASS_PANE,
            Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS_PANE,
            Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE,
            Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
            Blocks.CYAN_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE,
            Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS_PANE,
            Blocks.BLACK_STAINED_GLASS_PANE
        );

        registerColorFamily(Blocks.TERRACOTTA,
            Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA,
            Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA,
            Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA,
            Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA,
            Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA,
            Blocks.BLACK_TERRACOTTA
        );

        registerColorOnlyFamily(
            Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL,
            Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL,
            Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL,
            Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL,
            Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL,
            Blocks.BLACK_WOOL
        );

        registerColorOnlyFamily(
            Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET,
            Blocks.LIGHT_BLUE_CARPET, Blocks.YELLOW_CARPET, Blocks.LIME_CARPET,
            Blocks.PINK_CARPET, Blocks.GRAY_CARPET, Blocks.LIGHT_GRAY_CARPET,
            Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET,
            Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET,
            Blocks.BLACK_CARPET
        );

        registerColorOnlyFamily(
            Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE,
            Blocks.LIGHT_BLUE_CONCRETE, Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE,
            Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE,
            Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
            Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE,
            Blocks.BLACK_CONCRETE
        );

        registerColorOnlyFamily(
            Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER,
            Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER,
            Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER,
            Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER,
            Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER,
            Blocks.BLACK_CONCRETE_POWDER
        );

        registerColorFamily(Blocks.SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
        );
    }

    private static void registerColorFamily(Block clearBlock, Block... coloredBlocks) {
        if (coloredBlocks.length != 16) {
            throw new IllegalArgumentException("Expected 16 colored blocks, got " + coloredBlocks.length);
        }

        Map<Block, DyeColor> blockToColor = new IdentityHashMap<>();
        blockToColor.put(clearBlock, null);
        for (int i = 0; i < 16; i++) {
            blockToColor.put(coloredBlocks[i], DyeColor.values()[i]);
        }

        ICustomPaintHandler handler = (world, pos, state, hitPos, hitSide, paintColour) -> {
            Block currentBlock = state.getBlock();
            if (!blockToColor.containsKey(currentBlock)) {
                return InteractionResult.PASS;
            }
            DyeColor currentColour = blockToColor.get(currentBlock);

            if (currentColour == paintColour) {
                return InteractionResult.FAIL;
            }

            Block targetBlock;
            if (paintColour == null) {
                targetBlock = clearBlock;
            } else {
                targetBlock = coloredBlocks[paintColour.ordinal()];
            }

            BlockState newState = copyMatchingProperties(state, targetBlock.defaultBlockState());
            world.setBlock(pos, newState, Block.UPDATE_ALL);
            return InteractionResult.SUCCESS;
        };

        CustomPaintHelper.INSTANCE.registerHandler(clearBlock, handler);
        for (Block colored : coloredBlocks) {
            CustomPaintHelper.INSTANCE.registerHandler(colored, handler);
        }
    }

    private static void registerColorOnlyFamily(Block... coloredBlocks) {
        if (coloredBlocks.length != 16) {
            throw new IllegalArgumentException("Expected 16 colored blocks, got " + coloredBlocks.length);
        }

        Map<Block, DyeColor> blockToColor = new IdentityHashMap<>();
        for (int i = 0; i < 16; i++) {
            blockToColor.put(coloredBlocks[i], DyeColor.values()[i]);
        }

        ICustomPaintHandler handler = (world, pos, state, hitPos, hitSide, paintColour) -> {
            Block currentBlock = state.getBlock();
            if (!blockToColor.containsKey(currentBlock)) {
                return InteractionResult.PASS;
            }

            if (paintColour == null) {

                return InteractionResult.FAIL;
            }

            DyeColor currentColour = blockToColor.get(currentBlock);
            if (currentColour == paintColour) {
                return InteractionResult.FAIL;
            }

            Block targetBlock = coloredBlocks[paintColour.ordinal()];
            BlockState newState = copyMatchingProperties(state, targetBlock.defaultBlockState());
            world.setBlock(pos, newState, Block.UPDATE_ALL);
            return InteractionResult.SUCCESS;
        };

        for (Block colored : coloredBlocks) {
            CustomPaintHelper.INSTANCE.registerHandler(colored, handler);
        }
    }

    private static BlockState copyMatchingProperties(BlockState source, BlockState target) {
        for (var property : source.getProperties()) {
            if (target.hasProperty(property)) {
                target = copyProperty(source, target, property);
            }
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState source, BlockState target, net.minecraft.world.level.block.state.properties.Property<?> rawProperty) {
        net.minecraft.world.level.block.state.properties.Property<T> property = (net.minecraft.world.level.block.state.properties.Property<T>) rawProperty;
        return target.setValue(property, source.getValue(property));
    }

}
