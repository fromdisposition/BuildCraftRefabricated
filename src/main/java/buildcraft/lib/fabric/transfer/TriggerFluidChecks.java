package buildcraft.lib.fabric.transfer;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fabric.TransferConvert;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import org.jspecify.annotations.Nullable;

public final class TriggerFluidChecks {
   private TriggerFluidChecks() {
   }

   public static boolean hasViews(@Nullable Storage<FluidVariant> storage) {
      if (storage == null) {
         return false;
      } else {
         for (StorageView<FluidVariant> ignored : storage) {
            return true;
         }

         return false;
      }
   }

   public static boolean isEmpty(@Nullable Storage<FluidVariant> storage) {
      if (storage == null) {
         return false;
      }

      for (StorageView<FluidVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return false;
         }
      }

      return hasViews(storage);
   }

   public static boolean contains(@Nullable Storage<FluidVariant> storage, FluidStack searchedFluid) {
      if (storage == null) {
         return false;
      }

      FluidVariant searched = TransferConvert.toVariant(searchedFluid);

      for (StorageView<FluidVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L && (searched.isBlank() || ((FluidVariant)view.getResource()).equals(searched))) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasSpace(@Nullable Storage<FluidVariant> storage, FluidStack searchedFluid) {
      if (storage == null) {
         return false;
      }

      if (!searchedFluid.isEmpty()) {
         return FluidStorageOps.canInsert(storage, TransferConvert.toVariant(searchedFluid), TransferConvert.mbToDroplets(1L));
      }

      for (StorageView<FluidVariant> view : storage) {
         if (view.isResourceBlank() || view.getAmount() < view.getCapacity()) {
            return true;
         }
      }

      return false;
   }

   public static boolean isFull(@Nullable Storage<FluidVariant> storage, FluidStack searchedFluid) {
      if (storage == null) {
         return false;
      }

      if (!searchedFluid.isEmpty()) {
         return !FluidStorageOps.canInsert(storage, TransferConvert.toVariant(searchedFluid), TransferConvert.mbToDroplets(1L));
      }

      boolean sawView = false;

      for (StorageView<FluidVariant> view : storage) {
         sawView = true;
         if (view.isResourceBlank() || view.getAmount() < view.getCapacity()) {
            return false;
         }
      }

      return sawView;
   }

   public static boolean belowLevel(@Nullable Storage<FluidVariant> storage, FluidStack searchedFluid, float levelFraction) {
      if (storage == null) {
         return false;
      }

      FluidVariant searched = TransferConvert.toVariant(searchedFluid);

      for (StorageView<FluidVariant> view : storage) {
         long capacity = view.getCapacity();
         if (capacity > 0L) {
            if (view.isResourceBlank()) {
               if (searched.isBlank()) {
                  return true;
               }

               if (FluidStorageOps.canInsert(view, searched, TransferConvert.mbToDroplets(1L))) {
                  return true;
               }
            } else if (searched.isBlank() || ((FluidVariant)view.getResource()).equals(searched)) {
               float percentage = (float)view.getAmount() / (float)capacity;
               if (percentage < levelFraction) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
