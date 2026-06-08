/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.fabric.transfer.TransferCommits;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class PipeNeighborTransfers {
   private PipeNeighborTransfers() {
   }

   public static PipeNeighborTransfers.@Nullable ItemProbe findMatchingItem(Storage<ItemVariant> storage, int maxCount, IStackFilter filter) {
      if (storage == null) {
         return null;
      }

      for (StorageView<ItemVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            int available = TransferCommits.saturateCount(view.getAmount());
            ItemStack stack = ((ItemVariant)view.getResource()).toStack(Math.min(available, maxCount));
            if (filter.matches(stack)) {
               return new PipeNeighborTransfers.ItemProbe(view, (ItemVariant)view.getResource(), available);
            }
         }
      }

      return null;
   }

   public static int extractFromView(StorageView<ItemVariant> view, ItemVariant variant, int amount, boolean commit) {
      if (view != null && !variant.isBlank() && amount > 0) {
         try (Transaction transaction = Transaction.openOuter()) {
            long extracted = view.extract(variant, amount, transaction);
            int moved = TransferCommits.saturateCount(extracted);
            if (commit && moved > 0) {
               transaction.commit();
            }

            return moved;
         }
      } else {
         return 0;
      }
   }

   public static int insertItems(Storage<ItemVariant> storage, ItemStack stack, boolean commit) {
      return TransferCommits.insertItems(storage, stack, commit);
   }

   public record ItemProbe(StorageView<ItemVariant> view, ItemVariant resource, int available) {
   }
}
