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

   public PipeItemInjectStorage(IFlowItems flow, Direction side) {
      this.flow = flow;
      this.side = side;
      this.pendingInjects = new FabricDeferredCommit<>(stack -> flow.injectItem(stack, true, side, null, 0.25));
   }

   public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L && this.flow.canInjectItems(this.side)) {
         int amount = saturate(maxAmount);
         ItemStack stack = resource.toStack(amount);
         ItemStack remaining = this.flow.injectItem(stack, false, this.side, null, 0.25);
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

   public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         return this.flow instanceof PipeFlowItems pipeFlow ? pipeFlow.extractItemsForExternalSide(this.side, resource, saturate(maxAmount), transaction) : 0L;
      } else {
         return 0L;
      }
   }

   @SuppressWarnings("unchecked")
   public Iterator<StorageView<ItemVariant>> iterator() {
      if (!(this.flow instanceof PipeFlowItems pipeFlow)) {
         return Collections.emptyIterator();
      }

      List<StorageView<ItemVariant>> views = new ArrayList<>();
      for (PipeFlowItems.ExtractableEntry entry : pipeFlow.snapshotExtractable(this.side)) {
         views.add(new ItemView(entry));
      }

      return (Iterator<StorageView<ItemVariant>>)(Iterator<?>)views.iterator();
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }

   private final class ItemView implements StorageView<ItemVariant> {
      private final PipeFlowItems.ExtractableEntry entry;

      private ItemView(PipeFlowItems.ExtractableEntry entry) {
         this.entry = entry;
      }

      public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
         return PipeItemInjectStorage.this.extract(resource, maxAmount, transaction);
      }

      public boolean isResourceBlank() {
         return this.entry.amount() <= 0L;
      }

      public ItemVariant getResource() {
         return this.entry.variant();
      }

      public long getAmount() {
         return this.entry.amount();
      }

      public long getCapacity() {
         return this.entry.amount();
      }
   }
}
