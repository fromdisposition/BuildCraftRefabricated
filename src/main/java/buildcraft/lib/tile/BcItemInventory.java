/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.inventory.AbstractInvItemTransactor;
import buildcraft.lib.misc.INBTSerializable;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BcItemInventory extends AbstractInvItemTransactor implements Storage<ItemVariant>, StackInsertionChecker, INBTSerializable<CompoundTag> {
   public final NonNullList<ItemStack> stacks;
   private final SnapshotParticipant<ItemStack[]> journal = new SnapshotParticipant<ItemStack[]>() {
      protected ItemStack[] createSnapshot() {
         ItemStack[] snap = new ItemStack[BcItemInventory.this.stacks.size()];

         for (int i = 0; i < snap.length; i++) {
            snap[i] = ((ItemStack)BcItemInventory.this.stacks.get(i)).copy();
         }

         return snap;
      }

      protected void readSnapshot(ItemStack[] snapshot) {
         for (int i = 0; i < snapshot.length; i++) {
            BcItemInventory.this.stacks.set(i, snapshot[i] != null ? snapshot[i] : StackUtil.EMPTY);
         }
      }

      protected void onFinalCommit() {
         BcItemInventory.this.markDirtyIfNeeded();
      }
   };
   private StackInsertionChecker checker;
   private StackInsertionFunction inserter;
   @Nullable
   private StackChangeCallback callback;
   private int firstUsed = Integer.MAX_VALUE;
   protected int slotCapacity = 64;

   public BcItemInventory(int size) {
      this(size, (slot, stack) -> true, StackInsertionFunction.getDefaultInserter(), null);
   }

   public BcItemInventory(int size, int maxStackSize) {
      this(size);
      this.setLimitedInsertor(maxStackSize);
      this.slotCapacity = maxStackSize;
   }

   public BcItemInventory(int size, @Nullable StackChangeCallback callback) {
      this(size, (slot, stack) -> true, StackInsertionFunction.getDefaultInserter(), callback);
   }

   public BcItemInventory(int size, StackInsertionChecker checker, StackInsertionFunction insertionFunction, @Nullable StackChangeCallback callback) {
      this.stacks = NonNullList.withSize(size, StackUtil.EMPTY);
      this.checker = checker;
      this.inserter = insertionFunction;
      this.callback = callback;
   }

   protected void markDirtyIfNeeded() {
   }

   public void setChecker(StackInsertionChecker checker) {
      this.checker = checker;
   }

   public void setInsertor(StackInsertionFunction insertor) {
      this.inserter = insertor;
   }

   public void setLimitedInsertor(int maxStackSize) {
      this.setInsertor(StackInsertionFunction.getInsertionFunction(maxStackSize));
      this.slotCapacity = maxStackSize;
   }

   public void setCallback(StackChangeCallback callback) {
      this.callback = callback;
   }

   @Override
   public int size() {
      return this.stacks.size();
   }

   public int getSlots() {
      return this.size();
   }

   public int getSlotLimit(int slot) {
      return this.slotCapacity;
   }

   @Override
   protected boolean isEmpty(int slot) {
      return this.badSlotIndex(slot) ? true : ((ItemStack)this.stacks.get(slot)).isEmpty();
   }

   protected boolean badSlotIndex(int slot) {
      return slot < 0 || slot >= this.stacks.size();
   }

   @Nonnull
   public ItemStack getStackInSlot(int slot) {
      return this.badSlotIndex(slot) ? ItemStack.EMPTY : (ItemStack)this.stacks.get(slot);
   }

   public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
      if (this.badSlotIndex(slot)) {
         throw new IndexOutOfBoundsException("Slot index out of range: " + slot);
      }

      ItemStack before = (ItemStack)this.stacks.get(slot);
      this.setStackInternal(slot, stack);
      if (this.callback != null) {
         this.callback.onStackChange(this, slot, before, asValid(stack));
      }
   }

   @Override
   public final boolean canSet(int slot, @Nonnull ItemStack stack) {
      ItemStack copied = asValid(stack);
      return copied.isEmpty() ? true : this.checker.canSet(slot, copied);
   }

   private void setStackInternal(int slot, @Nonnull ItemStack stack) {
      this.stacks.set(slot, asValid(stack));
      if (stack.isEmpty() && this.firstUsed == slot) {
         for (int s = this.firstUsed; s < this.size(); s++) {
            if (!((ItemStack)this.stacks.get(s)).isEmpty()) {
               this.firstUsed = s;
               break;
            }
         }

         if (this.firstUsed == slot) {
            this.firstUsed = Integer.MAX_VALUE;
         }
      } else if (!stack.isEmpty() && this.firstUsed > slot) {
         this.firstUsed = slot;
      }
   }

   protected void onChanged(int slot, ItemStack before, ItemStack after) {
      if (this.callback != null) {
         this.callback.onStackChange(this, slot, before, after);
      }
   }

   protected int insertToSlot(int index, @Nonnull ItemStack toInsert, @Nullable TransactionContext tx) {
      if (!this.badSlotIndex(index) && !toInsert.isEmpty()) {
         int amount = toInsert.getCount();
         ItemStack current = (ItemStack)this.stacks.get(index);
         if (this.canSet(index, toInsert) && this.canSet(index, current)) {
            StackInsertionFunction.InsertionResult result = this.inserter.modifyForInsertion(index, asValid(current.copy()), asValid(toInsert.copy()));
            if (!this.canSet(index, result.toSet)) {
               CrashReport report = new CrashReport("Inserting an item (buildcraft:BcItemInventory)", new IllegalStateException("Conflicting Insertion!"));
               CrashReportCategory cat = report.addCategory("Inventory details");
               cat.setDetail("Existing Item", current.toString());
               cat.setDetail("Inserting Item", toInsert.toString());
               cat.setDetail("To Set", result.toSet.toString());
               cat.setDetail("To Return", result.toReturn.toString());
               cat.setDetail("Slot", String.valueOf(index));
               throw new RuntimeException("Conflicting Insertion! See log for details.");
            }

            int inserted = amount - result.toReturn.getCount();
            if (inserted > 0) {
               if (tx != null) {
                  this.journal.updateSnapshots(tx);
               }

               ItemStack before = current.copy();
               this.setStackInternal(index, result.toSet);
               this.onChanged(index, before, result.toSet);
            }

            return inserted;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   protected int extractFromSlot(int index, @Nonnull ItemVariant variant, int amount, @Nullable TransactionContext tx) {
      if (!this.badSlotIndex(index) && amount > 0 && !variant.isBlank()) {
         ItemStack current = (ItemStack)this.stacks.get(index);
         if (!current.isEmpty() && variant.matches(current)) {
            int toExtract = Math.min(amount, current.getCount());
            if (toExtract > 0) {
               ItemStack before = current.copy();
               ItemStack after = current.copy();
               after.shrink(toExtract);
               if (after.getCount() <= 0) {
                  after = StackUtil.EMPTY;
               }

               if (tx != null) {
                  this.journal.updateSnapshots(tx);
               }

               this.setStackInternal(index, after);
               this.onChanged(index, before, after);
            }

            return toExtract;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   @Nonnull
   public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
      return this.insert(slot, stack, simulate);
   }

   @Nonnull
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return this.extract(slot, s -> true, 1, amount, simulate);
   }

   @Nonnull
   @Override
   protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      }

      Transaction tx = Transaction.openOuter();

      ItemStack var6;
      try {
         int inserted = this.insertToSlot(slot, stack, tx);
         if (!simulate) {
            tx.commit();
         }

         var6 = stack.copyWithCount(stack.getCount() - inserted);
      } catch (Throwable var8) {
         if (tx != null) {
            try {
               tx.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (tx != null) {
         tx.close();
      }

      return var6;
   }

   @Nonnull
   @Override
   protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
      if (!this.badSlotIndex(slot) && max >= min) {
         ItemStack current = (ItemStack)this.stacks.get(slot);
         if (!current.isEmpty() && current.getCount() >= min && filter.matches(asValid(current))) {
            Transaction tx = Transaction.openOuter();

            ItemStack var10;
            try {
               int toExtract = Math.min(max, current.getCount());
               int ex = this.extractFromSlot(slot, ItemVariant.of(current), toExtract, tx);
               if (!simulate) {
                  tx.commit();
               }

               var10 = current.copyWithCount(ex);
            } catch (Throwable var12) {
               if (tx != null) {
                  try {
                     tx.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }
               }

               throw var12;
            }

            if (tx != null) {
               tx.close();
            }

            return var10;
         } else {
            return StackUtil.EMPTY;
         }
      } else {
         return StackUtil.EMPTY;
      }
   }

   public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         int remaining = saturate(maxAmount);
         int total = 0;

         for (int slot = 0; slot < this.size() && remaining > 0; slot++) {
            int inserted = this.insertToSlot(slot, resource.toStack(remaining), transaction);
            total += inserted;
            remaining -= inserted;
         }

         return total;
      } else {
         return 0L;
      }
   }

   public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         int remaining = saturate(maxAmount);
         int total = 0;

         for (int slot = 0; slot < this.size() && remaining > 0; slot++) {
            int extracted = this.extractFromSlot(slot, resource, remaining, transaction);
            total += extracted;
            remaining -= extracted;
         }

         return total;
      } else {
         return 0L;
      }
   }

   public Iterator<StorageView<ItemVariant>> iterator() {
      List<StorageView<ItemVariant>> views = new ArrayList<>(this.size());

      for (int i = 0; i < this.size(); i++) {
         views.add(new BcItemInventory.SlotView(i));
      }

      return views.iterator();
   }

   protected static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }

   public Storage<ItemVariant> insertOnlyView() {
      return new BcItemInventory.FilteredView(true, false);
   }

   public Storage<ItemVariant> extractOnlyView() {
      return new BcItemInventory.FilteredView(false, true);
   }

   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      ListTag list = new ListTag();

      for (ItemStack stack : this.stacks) {
         CompoundTag itemNbt = new CompoundTag();
         if (!stack.isEmpty()) {
            ItemStack.CODEC.encodeStart(NBTUtilBC.registryAwareOps(), stack).resultOrPartial().ifPresent(payload -> itemNbt.put("stack", payload));
         }

         list.add(itemNbt);
      }

      nbt.put("items", list);
      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      ListTag list = (ListTag)nbt.getList("items").orElseGet(ListTag::new);

      for (int i = 0; i < list.size() && i < this.size(); i++) {
         CompoundTag itemNbt = (CompoundTag)list.getCompound(i).orElseGet(CompoundTag::new);
         ItemStack stack = ItemStack.EMPTY;
         Tag stackPayload = itemNbt.get("stack");
         if (stackPayload != null) {
            stack = ItemStack.CODEC.parse(NBTUtilBC.registryAwareOps(), stackPayload).resultOrPartial().orElse(ItemStack.EMPTY);
         } else if (itemNbt.contains("id")) {
            String idStr = itemNbt.getString("id").orElse("");
            Identifier id = Identifier.tryParse(idStr);
            if (id != null) {
               Item item = (Item)BuiltInRegistries.ITEM.getValue(id);
               int count = itemNbt.getInt("count").orElse(1);
               if (item != null && item != Items.AIR) {
                  stack = new ItemStack(item, count);
               }
            }
         }

         this.setStackInternal(i, stack);
      }

      for (int i = list.size(); i < this.size(); i++) {
         this.setStackInternal(i, StackUtil.EMPTY);
      }
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + " " + this.stacks;
   }

   private final class FilteredView implements Storage<ItemVariant> {
      private final boolean allowInsert;
      private final boolean allowExtract;

      private FilteredView(boolean allowInsert, boolean allowExtract) {
         this.allowInsert = allowInsert;
         this.allowExtract = allowExtract;
      }

      public boolean supportsInsertion() {
         return this.allowInsert;
      }

      public boolean supportsExtraction() {
         return this.allowExtract;
      }

      public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
         return this.allowInsert ? BcItemInventory.this.insert(resource, maxAmount, transaction) : 0L;
      }

      public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
         return this.allowExtract ? BcItemInventory.this.extract(resource, maxAmount, transaction) : 0L;
      }

      public Iterator<StorageView<ItemVariant>> iterator() {
         return BcItemInventory.this.iterator();
      }
   }

   private final class SlotView implements StorageView<ItemVariant> {
      private final int index;

      private SlotView(int index) {
         this.index = index;
      }

      public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
         return !resource.isBlank() && maxAmount > 0L
            ? BcItemInventory.this.extractFromSlot(this.index, resource, BcItemInventory.saturate(maxAmount), transaction)
            : 0L;
      }

      public boolean isResourceBlank() {
         return BcItemInventory.this.getStackInSlot(this.index).isEmpty();
      }

      public ItemVariant getResource() {
         ItemStack stack = BcItemInventory.this.getStackInSlot(this.index);
         return stack.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stack);
      }

      public long getAmount() {
         return BcItemInventory.this.getStackInSlot(this.index).getCount();
      }

      public long getCapacity() {
         return BcItemInventory.this.slotCapacity;
      }
   }
}
