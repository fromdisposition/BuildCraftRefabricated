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
      Transaction tx = Transaction.openOuter();

      long var7;
      try {
         long moved = move(from, to, maxDroplets, tx);
         tx.commit();
         var7 = moved;
      } catch (Throwable var10) {
         if (tx != null) {
            try {
               tx.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (tx != null) {
         tx.close();
      }

      return var7;
   }

   public static boolean canInsert(@Nullable Storage<FluidVariant> storage, FluidVariant resource, long maxDroplets) {
      if (storage != null && !resource.isBlank() && maxDroplets > 0L) {
         Transaction tx = Transaction.openOuter();

         boolean var5;
         try {
            var5 = storage.insert(resource, maxDroplets, tx) > 0L;
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

         return var5;
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
