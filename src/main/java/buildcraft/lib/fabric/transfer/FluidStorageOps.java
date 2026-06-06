package buildcraft.lib.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class FluidStorageOps {
   private FluidStorageOps() {
   }

   public static long move(Storage<FluidVariant> from, Storage<FluidVariant> to, long maxDroplets, TransactionContext transaction) {
      return from != null && to != null && maxDroplets > 0L ? StorageUtil.move(from, to, variant -> true, maxDroplets, transaction) : 0L;
   }

   public static long move(Storage<FluidVariant> from, Storage<FluidVariant> to, long maxDroplets) {
      try (Transaction transaction = Transaction.openOuter()) {
         long moved = move(from, to, maxDroplets, transaction);
         transaction.commit();
         return moved;
      }
   }

   public static boolean canInsert(@Nullable Storage<FluidVariant> storage, FluidVariant resource, long maxDroplets) {
      if (storage != null && !resource.isBlank() && maxDroplets > 0L) {
         try (Transaction transaction = Transaction.openOuter()) {
            return storage.insert(resource, maxDroplets, transaction) > 0L;
         }
      } else {
         return false;
      }
   }

   public static boolean canInsert(StorageView<FluidVariant> view, FluidVariant resource, long maxDroplets) {
      if (!resource.isBlank() && maxDroplets > 0L) {
         return !view.isResourceBlank() && !((FluidVariant)view.getResource()).equals(resource) ? false : view.getCapacity() - view.getAmount() >= maxDroplets;
      } else {
         return false;
      }
   }
}
