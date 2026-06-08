/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.transfer.handler;

import buildcraft.lib.common.util.ValueIOSerializable;
import buildcraft.lib.fabric.transfer.FabricItemStorageProvider;
import java.util.Collections;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class ItemStackResourceHandler
   extends SnapshotParticipant<ItemStack>
   implements Storage<ItemVariant>,
   FabricItemStorageProvider,
   ValueIOSerializable {
   public static final String VALUE_IO_KEY = "stack";

   protected abstract ItemStack getStack();

   protected abstract void setStack(ItemStack var1);

   protected boolean isValid(ItemStack stack) {
      return true;
   }

   protected int getCapacity(ItemStack stack) {
      return stack.isEmpty() ? 99 : Math.min(stack.getMaxStackSize(), 99);
   }

   protected ItemStack createSnapshot() {
      ItemStack original = this.getStack();
      this.setStack(original.copy());
      return original;
   }

   protected void readSnapshot(ItemStack snapshot) {
      this.setStack(snapshot);
   }

   @Override
   public void serialize(ValueOutput output) {
      if (!this.getStack().isEmpty()) {
         output.store("stack", ItemStack.CODEC, this.getStack());
      }
   }

   @Override
   public void deserialize(ValueInput input) {
      this.setStack(input.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY));
   }

   @Override
   public Storage<ItemVariant> fabricItemStorage() {
      return this;
   }

   public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         int amount = saturate(maxAmount);
         ItemStack proposed = resource.toStack(1);
         if (!this.isValid(proposed)) {
            return 0L;
         }

         ItemStack currentStack = this.getStack();
         if (!currentStack.isEmpty() && !ItemStack.isSameItemSameComponents(currentStack, proposed)) {
            return 0L;
         }

         int insertedAmount = Math.min(amount, this.getCapacity(proposed) - currentStack.getCount());
         if (insertedAmount <= 0) {
            return 0L;
         }

         this.updateSnapshots(transaction);
         currentStack = this.getStack();
         if (currentStack.isEmpty()) {
            currentStack = resource.toStack(insertedAmount);
         } else {
            currentStack.grow(insertedAmount);
         }

         this.setStack(currentStack);
         return insertedAmount;
      } else {
         return 0L;
      }
   }

   public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         ItemStack currentStack = this.getStack();
         if (!currentStack.isEmpty() && ItemStack.isSameItemSameComponents(currentStack, resource.toStack(1))) {
            int extracted = Math.min(saturate(maxAmount), currentStack.getCount());
            if (extracted <= 0) {
               return 0L;
            }

            this.updateSnapshots(transaction);
            currentStack = this.getStack();
            currentStack.shrink(extracted);
            this.setStack(currentStack);
            return extracted;
         } else {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   @SuppressWarnings("unchecked")
   public Iterator<StorageView<ItemVariant>> iterator() {
      return this.getStack().isEmpty()
         ? Collections.emptyIterator()
         : (Iterator<StorageView<ItemVariant>>)(Iterator<?>)Collections.singletonList(new ItemStackResourceHandler.SingleSlotView()).iterator();
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }

   @Override
   public String toString() {
      return this.getClass().getName() + "[" + this.getStack() + "]";
   }

   private final class SingleSlotView implements StorageView<ItemVariant> {
      public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
         return ItemStackResourceHandler.this.extract(resource, maxAmount, transaction);
      }

      public boolean isResourceBlank() {
         return ItemStackResourceHandler.this.getStack().isEmpty();
      }

      public ItemVariant getResource() {
         ItemStack stack = ItemStackResourceHandler.this.getStack();
         return stack.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stack);
      }

      public long getAmount() {
         return ItemStackResourceHandler.this.getStack().getCount();
      }

      public long getCapacity() {
         ItemStack stack = ItemStackResourceHandler.this.getStack();
         return stack.isEmpty() ? 99L : Math.min(stack.getMaxStackSize(), 99);
      }
   }
}
