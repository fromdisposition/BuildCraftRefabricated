package buildcraft.transport.pipe.behaviour;

import buildcraft.lib.fabric.transfer.TriggerTransferAccess;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fabric.TransferConvert;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.ItemStack;

public final class FilterFluidStacks {
   private FilterFluidStacks() {
   }

   public static FluidStack fluidFromFilter(ItemStack stack) {
      if (stack.isEmpty()) {
         return FluidStack.EMPTY;
      }

      Storage<FluidVariant> storage = TriggerTransferAccess.itemFluidStorage(stack);
      if (storage == null) {
         return FluidStack.EMPTY;
      }

      for (StorageView<FluidVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return TransferConvert.toFluidStack((FluidVariant)view.getResource(), view.getAmount());
         }
      }

      return FluidStack.EMPTY;
   }
}
