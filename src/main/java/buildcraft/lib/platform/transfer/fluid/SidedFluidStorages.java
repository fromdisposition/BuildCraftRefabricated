package buildcraft.lib.platform.transfer.fluid;

import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class SidedFluidStorages {
   private SidedFluidStorages() {
   }

   public static Storage<FluidVariant> extractOnly(final Storage<FluidVariant> storage) {
      return new ExtractionOnlyStorage<FluidVariant>() {
         public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return storage.extract(resource, maxAmount, transaction);
         }

         public Iterator<StorageView<FluidVariant>> iterator() {
            return storage.iterator();
         }
      };
   }

   public static Storage<FluidVariant> insertOnly(final Storage<FluidVariant> storage) {
      return new InsertionOnlyStorage<FluidVariant>() {
         public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return storage.insert(resource, maxAmount, transaction);
         }
      };
   }
}
