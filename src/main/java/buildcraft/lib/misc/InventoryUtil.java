/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.tile.BcItemInventory;
import buildcraft.lib.fabric.transfer.fluid.NeighborTransfers;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class InventoryUtil {
   public static void addAll(BcItemInventory inventory, List<ItemStack> list) {
      for (int i = 0; i < inventory.getSlots(); i++) {
         ItemStack stack = inventory.getStackInSlot(i);
         if (!stack.isEmpty()) {
            list.add(stack);
         }
      }
   }

   public static void addToBestAcceptor(Level level, BlockPos pos, @Nullable Direction ignore, @Nonnull ItemStack stack) {
      if (!stack.isEmpty()) {
         stack = addToRandomInjectable(level, pos, ignore, stack);
         stack = addToRandomInventory(level, pos, stack);
         if (!stack.isEmpty()) {
            drop(level, pos, stack);
         }
      }
   }

   @Nonnull
   public static ItemStack addToRandomInjectable(Level level, BlockPos pos, @Nullable Direction ignore, @Nonnull ItemStack stack) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      }

      List<Direction> toTry = new ArrayList<>(6);
      Collections.addAll(toTry, Direction.values());
      Collections.shuffle(toTry);

      for (Direction face : toTry) {
         if (face != ignore) {
            if (stack.isEmpty()) {
               return ItemStack.EMPTY;
            }

            BlockPos adjPos = pos.relative(face);
            BlockEntity tile = level.getBlockEntity(adjPos);
            if (tile != null) {
               IInjectable injectable = getInjectable(tile, face.getOpposite());
               if (injectable != null) {
                  stack = injectable.injectItem(stack, true, face.getOpposite(), null, 0.0);
                  if (stack.isEmpty()) {
                     return ItemStack.EMPTY;
                  }
               }
            }
         }
      }

      return stack;
   }

   @Nullable
   private static IInjectable getInjectable(BlockEntity tile, Direction face) {
      if (tile instanceof IPipeHolder holder) {
         IPipe pipe = holder.getPipe();
         if (pipe != null && pipe.getFlow() instanceof PipeFlowItems items) {
            return items.getInjectable(face);
         }
      }

      return null;
   }

   @Nonnull
   public static ItemStack addToRandomInventory(Level level, BlockPos pos, @Nonnull ItemStack stack) {
      return NeighborTransfers.insertItemsShuffled(level, pos, stack);
   }

   public static void drop(Level level, BlockPos pos, @Nonnull ItemStack stack) {
      if (!stack.isEmpty()) {
         Block.popResource(level, pos, stack);
      }
   }
}
