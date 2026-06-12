package buildcraft.lib.fabric.transfer;

import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.fabric.transfer.TransferCommits;
import buildcraft.lib.fabric.transfer.FluidVariants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public record FluidStorageSnapshot(FluidStack fluid, int amountMb, int capacityMb) {
   public static final FluidStorageSnapshot EMPTY = new FluidStorageSnapshot(FluidStack.EMPTY, 0, 0);

   public static FluidStorageSnapshot fromLevels(@Nullable Fluid fluid, int amountMb, int capacityMb) {
      if (capacityMb <= 0) {
         return EMPTY;
      } else {
         return fluid != null && fluid != Fluids.EMPTY && amountMb > 0
            ? new FluidStorageSnapshot(new FluidStack(fluid, amountMb), amountMb, capacityMb)
            : new FluidStorageSnapshot(FluidStack.EMPTY, 0, capacityMb);
      }
   }

   public static FluidStorageSnapshot fromSingleSlot(@Nullable SingleFluidTank tank) {
      if (tank == null) {
         return EMPTY;
      }

      FluidStack contents = tank.getFluidStack();
      return new FluidStorageSnapshot(contents, tank.getAmountMb(), tank.getCapacityMb());
   }

   public static FluidStack identityFrom(@Nullable Storage<FluidVariant> storage) {
      if (storage == null) {
         return FluidStack.EMPTY;
      }

      for (StorageView<FluidVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return FluidVariants.toStack((FluidVariant)view.getResource());
         }
      }

      return FluidStack.EMPTY;
   }

   public static FluidStorageSnapshot of(@Nullable Storage<FluidVariant> storage) {
      if (storage == null) {
         return EMPTY;
      }

      FluidVariant variant = FluidVariant.blank();
      long amountMb = 0L;
      long capacityMb = 0L;

      for (StorageView<FluidVariant> view : storage) {
         capacityMb += FluidVariants.dropletsToMb(view.getCapacity());
         if (!view.isResourceBlank()) {
            if (variant.isBlank()) {
               variant = (FluidVariant)view.getResource();
            }

            amountMb += FluidVariants.dropletsToMb(view.getAmount());
         }
      }

      FluidStack fluid = variant.isBlank() ? FluidStack.EMPTY : FluidVariants.toStack(variant, FluidVariants.mbToDroplets(TransferCommits.saturateMb(amountMb)));
      return new FluidStorageSnapshot(fluid, TransferCommits.saturateMb(amountMb), TransferCommits.saturateMb(capacityMb));
   }

   public boolean isEmpty() {
      return this.fluid.isEmpty() || this.amountMb <= 0;
   }

   public FluidStack toFluidStack() {
      return this.isEmpty() ? FluidStack.EMPTY : this.fluid.copyWithAmount(this.amountMb);
   }

}
