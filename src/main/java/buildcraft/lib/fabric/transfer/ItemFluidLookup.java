package buildcraft.lib.fabric.transfer;

import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.fabric.transfer.FluidVariants;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ItemFluidLookup {
   private ItemFluidLookup() {
   }

   public static @Nullable Storage<FluidVariant> storage(ItemStack stack, ContainerItemContext context) {
      return stack.isEmpty() ? null : FluidStorage.ITEM.find(stack, context);
   }

   public static @Nullable Storage<FluidVariant> storage(ItemStack stack) {
      return stack.isEmpty() ? null : storage(stack, ContainerItemContext.withConstant(stack));
   }

   public static boolean hasStorage(ItemStack stack) {
      return storage(stack) != null;
   }

   public static boolean hasStorage(ItemStack stack, ContainerItemContext context) {
      return storage(stack, context) != null;
   }

   public static @Nullable FluidStack firstFluid(@Nullable Storage<FluidVariant> storage) {
      if (storage == null) {
         return null;
      }

      for (StorageView<FluidVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return FluidVariants.toFluidStack((FluidVariant)view.getResource(), view.getAmount());
         }
      }

      return null;
   }

   public static FluidStack firstFluid(ItemStack stack) {
      FluidStack fluid = firstFluid(storage(stack));
      return fluid == null ? FluidStack.EMPTY : fluid;
   }
}
