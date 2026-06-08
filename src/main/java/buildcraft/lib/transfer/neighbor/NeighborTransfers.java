/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.transfer.neighbor;

import buildcraft.lib.fabric.transfer.FluidStorageOps;
import buildcraft.lib.fabric.transfer.FluidStorageSnapshot;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.lib.transfer.fabric.TransferConvert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public final class NeighborTransfers {
   private NeighborTransfers() {
   }

   public static ItemStack insertItemsShuffled(
      Level level, BlockPos pos, ItemStack stack, @Nullable Direction ignore, @Nullable Predicate<BlockEntity> skipNeighbor
   ) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      }

      List<Direction> toTry = new ArrayList<>(6);
      Collections.addAll(toTry, Direction.values());
      Collections.shuffle(toTry);
      int remaining = stack.getCount();

      for (Direction face : toTry) {
         if (remaining <= 0) {
            return ItemStack.EMPTY;
         }

         if (face != ignore) {
            BlockPos adjPos = pos.relative(face);
            if (skipNeighbor != null) {
               BlockEntity neighbor = level.getBlockEntity(adjPos);
               if (neighbor != null && skipNeighbor.test(neighbor)) {
                  continue;
               }
            }

            Storage<ItemVariant> storage = BcTransfers.item(level, adjPos, face.getOpposite());
            if (storage != null) {
               int inserted = insertCommitted(storage, stack, remaining);
               remaining -= inserted;
            }
         }
      }

      return remaining <= 0 ? ItemStack.EMPTY : stack.copyWithCount(remaining);
   }

   public static ItemStack insertItemsShuffled(Level level, BlockPos pos, ItemStack stack) {
      return insertItemsShuffled(level, pos, stack, null, null);
   }

   public static ItemStack insertItemsShuffled(Level level, BlockPos pos, ItemStack stack, @Nullable Direction ignore) {
      return insertItemsShuffled(level, pos, stack, ignore, null);
   }

   public static int insertItemCountShuffled(Level level, BlockPos pos, ItemStack template, int amount, @Nullable Direction ignore) {
      if (!template.isEmpty() && amount > 0) {
         List<Direction> toTry = new ArrayList<>(6);
         Collections.addAll(toTry, Direction.values());
         Collections.shuffle(toTry);
         int remaining = amount;
         int totalInserted = 0;

         for (Direction face : toTry) {
            if (remaining <= 0) {
               break;
            }

            if (face != ignore) {
               Storage<ItemVariant> storage = BcTransfers.item(level, pos.relative(face), face.getOpposite());
               if (storage != null) {
                  int inserted = insertCommitted(storage, template, remaining);
                  totalInserted += inserted;
                  remaining -= inserted;
               }
            }
         }

         return totalInserted;
      } else {
         return 0;
      }
   }

   public static void pushFluidToNeighbors(Level level, BlockPos pos, Storage<FluidVariant> from) {
      pushFluidToNeighbors(level, pos, from, 1000);
   }

   public static void pushFluidToNeighbors(Level level, BlockPos pos, Storage<FluidVariant> from, int maxPerNeighbor) {
      if (from == null || FluidStorageSnapshot.of(from).isEmpty()) {
         return;
      }

      for (Direction dir : Direction.values()) {
         Storage<FluidVariant> neighbor = BcTransfers.fluid(level, pos.relative(dir), dir.getOpposite());
         if (neighbor != null) {
            int moved = moveFluidCommitted(from, neighbor, maxPerNeighbor);
            if (moved > 0 && FluidStorageSnapshot.of(from).isEmpty()) {
               break;
            }
         }
      }
   }

   public static int moveFluidCommitted(Storage<FluidVariant> from, Storage<FluidVariant> to, int maxAmountMb) {
      if (maxAmountMb <= 0) {
         return 0;
      }

      try (Transaction transaction = Transaction.openOuter()) {
         long moved = FluidStorageOps.move(from, to, TransferConvert.mbToDroplets(maxAmountMb), transaction);
         if (moved > 0L) {
            transaction.commit();
         }

         return saturateMb(TransferConvert.dropletsToMb(moved));
      }
   }

   private static int insertCommitted(Storage<ItemVariant> storage, ItemStack template, int amount) {
      ItemVariant variant = ItemVariant.of(template);
      try (Transaction transaction = Transaction.openOuter()) {
         long inserted = storage.insert(variant, amount, transaction);
         if (inserted > 0L) {
            transaction.commit();
         }

         return saturateCount(inserted);
      }
   }

   private static int saturateMb(long millibuckets) {
      return millibuckets > 2147483647L ? Integer.MAX_VALUE : (int)millibuckets;
   }

   private static int saturateCount(long count) {
      return count > 2147483647L ? Integer.MAX_VALUE : (int)count;
   }
}
