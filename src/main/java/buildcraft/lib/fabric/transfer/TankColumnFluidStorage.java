package buildcraft.lib.fabric.transfer;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.fabric.transfer.TransferCommits;
import buildcraft.lib.transfer.fabric.TransferConvert;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class TankColumnFluidStorage implements Storage<FluidVariant> {
   private final TileTank owner;

   public TankColumnFluidStorage(TileTank owner) {
      this.owner = owner;
   }

   public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         FluidStack fluid = FluidUtilBC.canonicalFluidStack(TransferConvert.toFluidStack(resource));
         FluidVariant variant = TransferConvert.toVariant(fluid);
         long millibuckets = TransferConvert.dropletsToMb(maxAmount);
         if (millibuckets <= 0L) {
            return 0L;
         }

         List<TileTank> tanks = this.owner.getTankColumn();
         FluidStack resolved = resolveColumnFluid(tanks, fluid);
         if (resolved.isEmpty() && !fluid.isEmpty()) {
            resolved = fluid;
         } else if (!resolved.isEmpty() && !FluidUtilBC.areEquivalentFluidStacks(resolved, fluid)) {
            return 0L;
         }

         boolean gaseous = FluidUtilBC.isGaseous(resolved);
         long remaining = millibuckets;
         long totalInserted = 0L;
         if (gaseous) {
            for (int i = tanks.size() - 1; i >= 0 && remaining > 0L; i--) {
               long accepted = tanks.get(i).fluidTank.insert(variant, TransferConvert.mbToDroplets(remaining), transaction);
               if (accepted > 0L) {
                  remaining -= TransferConvert.dropletsToMb(accepted);
                  totalInserted += accepted;
               }
            }
         } else {
            for (TileTank tank : tanks) {
               if (remaining <= 0L) {
                  break;
               }

               long accepted = tank.fluidTank.insert(variant, TransferConvert.mbToDroplets(remaining), transaction);
               if (accepted > 0L) {
                  remaining -= TransferConvert.dropletsToMb(accepted);
                  totalInserted += accepted;
               }
            }
         }

         if (totalInserted > 0L) {
            this.owner.requestColumnBalance();
         }

         return totalInserted;
      } else {
         return 0L;
      }
   }

   public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         FluidStack fluid = FluidUtilBC.canonicalFluidStack(TransferConvert.toFluidStack(resource));
         long millibuckets = TransferConvert.dropletsToMb(maxAmount);
         if (millibuckets <= 0L) {
            return 0L;
         }

         List<TileTank> tanks = this.owner.getTankColumn();
         FluidStack resolved = resolveExtractFluid(tanks, fluid);
         if (resolved.isEmpty()) {
            return 0L;
         }

         FluidVariant variant = TransferConvert.toVariant(resolved);
         boolean gaseous = FluidUtilBC.isGaseous(resolved);
         long remaining = millibuckets;
         long totalExtracted = 0L;
         if (gaseous) {
            for (TileTank tank : tanks) {
               if (remaining <= 0L) {
                  break;
               }

               long extracted = tank.fluidTank.extract(variant, TransferConvert.mbToDroplets(remaining), transaction);
               if (extracted > 0L) {
                  remaining -= TransferConvert.dropletsToMb(extracted);
                  totalExtracted += extracted;
               }
            }
         } else {
            for (int i = tanks.size() - 1; i >= 0 && remaining > 0L; i--) {
               long extracted = tanks.get(i).fluidTank.extract(variant, TransferConvert.mbToDroplets(remaining), transaction);
               if (extracted > 0L) {
                  remaining -= TransferConvert.dropletsToMb(extracted);
                  totalExtracted += extracted;
               }
            }
         }

         if (totalExtracted > 0L) {
            this.owner.requestColumnBalance();
         }

         return totalExtracted;
      } else {
         return 0L;
      }
   }

   @SuppressWarnings("unchecked")
   public Iterator<StorageView<FluidVariant>> iterator() {
      FluidStorageSnapshot snapshot = this.snapshotFromColumn();
      return snapshot.capacityMb() <= 0
         ? Collections.emptyIterator()
         : (Iterator<StorageView<FluidVariant>>)(Iterator<?>)List.of(new TankColumnFluidStorage.ColumnView(snapshot)).iterator();
   }

   private FluidStorageSnapshot snapshotFromColumn() {
      List<TileTank> tanks = this.owner.getTankColumn();
      FluidStack fluid = FluidStack.EMPTY;
      long amountMb = 0L;
      long capacityMb = 0L;

      for (TileTank tank : tanks) {
         FluidStack held = tank.fluidTank.getFluidStack();
         if (fluid.isEmpty() && !held.isEmpty()) {
            fluid = held.copyWithAmount(1);
         }

         amountMb += tank.fluidTank.getAmountMb();
         capacityMb += tank.fluidTank.getCapacityMb();
      }

      FluidStack stack = fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(TransferCommits.saturateMb(amountMb));
      return new FluidStorageSnapshot(stack, TransferCommits.saturateMb(amountMb), TransferCommits.saturateMb(capacityMb));
   }

   private static FluidStack resolveColumnFluid(List<TileTank> tanks, FluidStack candidate) {
      for (TileTank tank : tanks) {
         FluidStack current = tank.fluidTank.getFluidStack();
         if (!current.isEmpty()) {
            FluidStack identity = current.copyWithAmount(1);
            if (!candidate.isEmpty() && !FluidUtilBC.areEquivalentFluidStacks(identity, candidate)) {
               return FluidStack.EMPTY;
            }

            return identity;
         }
      }

      return candidate;
   }

   private static FluidStack resolveExtractFluid(List<TileTank> tanks, FluidStack candidate) {
      for (TileTank tank : tanks) {
         FluidStack current = tank.fluidTank.getFluidStack();
         if (!current.isEmpty()) {
            FluidStack identity = current.copyWithAmount(1);
            if (FluidUtilBC.areEquivalentFluidStacks(identity, candidate)) {
               return identity;
            }
         }
      }

      return FluidStack.EMPTY;
   }

   private final class ColumnView implements StorageView<FluidVariant> {
      private final FluidStorageSnapshot snapshot;

      private ColumnView(FluidStorageSnapshot snapshot) {
         this.snapshot = snapshot;
      }

      public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
         return TankColumnFluidStorage.this.extract(resource, maxAmount, transaction);
      }

      public boolean isResourceBlank() {
         return this.snapshot.isEmpty();
      }

      public FluidVariant getResource() {
         return this.snapshot.fluid().isEmpty() ? FluidVariant.blank() : TransferConvert.toVariant(this.snapshot.fluid());
      }

      public long getAmount() {
         return TransferConvert.mbToDroplets(this.snapshot.amountMb());
      }

      public long getCapacity() {
         return TransferConvert.mbToDroplets(this.snapshot.capacityMb());
      }
   }
}
