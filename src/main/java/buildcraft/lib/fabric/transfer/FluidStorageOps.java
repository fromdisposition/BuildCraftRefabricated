package buildcraft.lib.fabric.transfer;

import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.fabric.transfer.FluidVariants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import java.util.function.Predicate;
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

   public static int extractFluidMb(Storage<FluidVariant> storage, FluidStack fluid, int maxMillibuckets, TransactionContext transaction) {
      return fluid != null && !fluid.isEmpty() && maxMillibuckets > 0
         ? extractFluidMb(storage, FluidVariants.toVariant(fluid), maxMillibuckets, transaction)
         : 0;
   }

   public static int insertFluidMb(Storage<FluidVariant> storage, FluidStack fluid, int millibuckets, TransactionContext transaction) {
      return fluid != null && !fluid.isEmpty() && millibuckets > 0
         ? insertFluidMb(storage, FluidVariants.toVariant(fluid), millibuckets, transaction)
         : 0;
   }

   public static int extractFluidMb(Storage<FluidVariant> storage, FluidStack fluid, int maxMillibuckets, boolean commit) {
      if (storage != null && fluid != null && !fluid.isEmpty() && maxMillibuckets > 0) {
         FluidVariant variant = FluidVariants.toVariant(fluid);
         try (Transaction transaction = Transaction.openOuter()) {
            int extracted = extractFluidMb(storage, variant, maxMillibuckets, transaction);
            if (commit && extracted > 0) {
               transaction.commit();
            }

            return extracted;
         }
      } else {
         return 0;
      }
   }

   public static int extractFluidMb(Storage<FluidVariant> storage, FluidVariant variant, int maxMillibuckets, TransactionContext transaction) {
      if (storage != null && !variant.isBlank() && maxMillibuckets > 0) {
         long extracted = storage.extract(variant, FluidVariants.mbToDroplets(maxMillibuckets), transaction);
         return TransferCommits.saturateMb(FluidVariants.dropletsToMb(extracted));
      } else {
         return 0;
      }
   }

   public static int insertFluidMb(Storage<FluidVariant> storage, FluidStack fluid, int millibuckets, boolean commit) {
      if (storage != null && fluid != null && !fluid.isEmpty() && millibuckets > 0) {
         FluidVariant variant = FluidVariants.toVariant(fluid);
         try (Transaction transaction = Transaction.openOuter()) {
            int inserted = insertFluidMb(storage, variant, millibuckets, transaction);
            if (commit && inserted > 0) {
               transaction.commit();
            }

            return inserted;
         }
      } else {
         return 0;
      }
   }

   public static int moveMb(Storage<FluidVariant> from, Storage<FluidVariant> to, int maxMb, TransactionContext transaction) {
      return moveMb(from, to, maxMb, variant -> true, transaction);
   }

   public static int moveMb(
      Storage<FluidVariant> from, Storage<FluidVariant> to, int maxMb, Predicate<FluidVariant> filter, TransactionContext transaction
   ) {
      return maxMb > 0
         ? TransferCommits.saturateMb(FluidVariants.dropletsToMb(StorageUtil.move(from, to, filter, FluidVariants.mbToDroplets(maxMb), transaction)))
         : 0;
   }

   public static int insertFluidMb(Storage<FluidVariant> storage, FluidVariant variant, int millibuckets, TransactionContext transaction) {
      if (storage != null && !variant.isBlank() && millibuckets > 0) {
         long inserted = storage.insert(variant, FluidVariants.mbToDroplets(millibuckets), transaction);
         return TransferCommits.saturateMb(FluidVariants.dropletsToMb(inserted));
      } else {
         return 0;
      }
   }
}
