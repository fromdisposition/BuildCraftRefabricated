/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.lib.fabric.transfer.FabricDeferredCommit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public final class PipeItemInjectStorage implements Storage<ItemVariant> {
   private static final double DEFAULT_SPEED = 0.25;
   private final IFlowItems flow;
   private final Direction side;
   private final FabricDeferredCommit<ItemStack> pendingInjects;
   private final List<StorageView<ItemVariant>> extractableViews = new ArrayList<>(4);

   public PipeItemInjectStorage(IFlowItems flow, Direction side) {
      this.flow = flow;
      this.side = side;
      this.pendingInjects = new FabricDeferredCommit<>(stack -> {
         ItemStack leftover = flow.injectItem(stack, true, side, null, DEFAULT_SPEED);
         if (!leftover.isEmpty()) {
            // Commit may accept less than the simulation did if another insert in the same transaction filled
            // the pipe first; force the remainder in rather than silently void it.
            flow.insertItemsForce(leftover, side, null, DEFAULT_SPEED);
         }
      });
   }

   @Override
   public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L && this.flow.canInjectItems(this.side)) {
         int amount = saturate(maxAmount);
         ItemStack stack = resource.toStack(amount);
         ItemStack remaining = this.flow.injectItem(stack, false, this.side, null, DEFAULT_SPEED);
         int accepted = amount - remaining.getCount();
         if (accepted <= 0) {
            return 0L;
         }

         this.pendingInjects.enqueue(transaction, stack.copyWithCount(accepted));
         return accepted;
      } else {
         return 0L;
      }
   }

   @Override
   public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         return this.flow instanceof PipeFlowItems pipeFlow ? pipeFlow.extractItemsForExternalSide(this.side, resource, saturate(maxAmount), transaction) : 0L;
      } else {
         return 0L;
      }
   }

   @Override
   public Iterator<StorageView<ItemVariant>> iterator() {
      if (!(this.flow instanceof PipeFlowItems pipeFlow)) {
         return Collections.emptyIterator();
      }

      this.extractableViews.clear();
      for (PipeFlowItems.ExtractableEntry entry : pipeFlow.snapshotExtractable(this.side)) {
         this.extractableViews.add(new ItemView(entry));
      }

      return this.extractableViews.iterator();
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }

   private final class ItemView implements StorageView<ItemVariant> {
      private final PipeFlowItems.ExtractableEntry entry;

      private ItemView(PipeFlowItems.ExtractableEntry entry) {
         this.entry = entry;
      }

      @Override
      public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
         return PipeItemInjectStorage.this.extract(resource, maxAmount, transaction);
      }

      @Override
      public boolean isResourceBlank() {
         return this.entry.amount() <= 0L;
      }

      @Override
      public ItemVariant getResource() {
         return this.entry.variant();
      }

      @Override
      public long getAmount() {
         return this.entry.amount();
      }

      @Override
      public long getCapacity() {
         return this.entry.amount();
      }
   }
}
