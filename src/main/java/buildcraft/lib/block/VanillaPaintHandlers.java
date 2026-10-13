/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import buildcraft.api.blocks.CustomPaintHelper;
import buildcraft.api.blocks.ICustomPaintHandler;
import buildcraft.lib.fabric.BcRegistryUtil;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class VanillaPaintHandlers {
   public static void init() {
      registerColorFamily(Blocks.GLASS, dyed("stained_glass"));
      registerColorFamily(Blocks.GLASS_PANE, dyed("stained_glass_pane"));
      registerColorFamily(Blocks.TERRACOTTA, dyed("terracotta"));
      registerColorOnlyFamily(dyed("wool"));
      registerColorOnlyFamily(dyed("carpet"));
      registerColorOnlyFamily(dyed("concrete"));
      registerColorOnlyFamily(dyed("concrete_powder"));
      registerColorFamily(Blocks.SHULKER_BOX, dyed("shulker_box"));
      registerColorOnlyFamily(dyed("glazed_terracotta"));
      registerColorFamily(Blocks.CANDLE, dyed("candle"));
      registerBedFamily(dyed("bed"));
   }

   private static Block[] dyed(String suffix) {
      Block[] blocks = new Block[16];
      for (DyeColor color : DyeColor.values()) {
         blocks[color.ordinal()] = Objects.requireNonNull(BcRegistryUtil.getBlock(Identifier.withDefaultNamespace(color.getName() + "_" + suffix)), suffix);
      }

      return blocks;
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
         world.setBlock(pos, newState, 3);
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
         world.setBlock(pos, newState, 3);
         return InteractionResult.SUCCESS;
      };

      for (Block colored : coloredBlocks) {
         CustomPaintHelper.INSTANCE.registerHandler(colored, handler);
      }
   }

   // Recolouring only one bed half lets the other's updateShape see a mismatched partner and pop it off,
   // so both halves swap under UPDATE_KNOWN_SHAPE.
   private static void registerBedFamily(Block... beds) {
      if (beds.length != 16) {
         throw new IllegalArgumentException("Expected 16 bed blocks, got " + beds.length);
      }

      Map<Block, DyeColor> blockToColor = new IdentityHashMap<>();
      for (int i = 0; i < 16; i++) {
         blockToColor.put(beds[i], DyeColor.values()[i]);
      }

      ICustomPaintHandler handler = (world, pos, state, hitPos, hitSide, paintColour) -> {
         Block currentBlock = state.getBlock();
         if (!blockToColor.containsKey(currentBlock)) {
            return InteractionResult.PASS;
         }

         if (paintColour == null) {
            return InteractionResult.FAIL;
         }

         if (blockToColor.get(currentBlock) == paintColour) {
            return InteractionResult.FAIL;
         }

         Block targetBlock = beds[paintColour.ordinal()];
         int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
         BlockPos otherPos = pos.relative(BedBlock.getConnectedDirection(state));
         BlockState otherState = world.getBlockState(otherPos);

         world.setBlock(pos, copyMatchingProperties(state, targetBlock.defaultBlockState()), flags);
         if (otherState.getBlock() == currentBlock) {
            world.setBlock(otherPos, copyMatchingProperties(otherState, targetBlock.defaultBlockState()), flags);
         }

         return InteractionResult.SUCCESS;
      };

      for (Block bed : beds) {
         CustomPaintHelper.INSTANCE.registerHandler(bed, handler);
      }
   }

   private static BlockState copyMatchingProperties(BlockState source, BlockState target) {
      for (Property<?> property : source.getProperties()) {
         if (target.hasProperty(property)) {
            target = copyProperty(source, target, property);
         }
      }

      return target;
   }

   @SuppressWarnings("unchecked")
   private static <T extends Comparable<T>> BlockState copyProperty(BlockState source, BlockState target, Property<?> rawProperty) {
      Property<T> property = (Property<T>)rawProperty;
      return target.setValue(property, source.getValue(property));
   }
}
