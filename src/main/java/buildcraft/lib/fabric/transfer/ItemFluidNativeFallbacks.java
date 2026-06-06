package buildcraft.lib.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ItemFluidNativeFallbacks {
   private static boolean registered;

   private ItemFluidNativeFallbacks() {
   }

   public static synchronized void register() {
      if (!registered) {
         registered = true;
         FluidStorage.ITEM.registerFallback(ItemFluidNativeFallbacks::resolveItemFluid);
      }
   }

   private static @Nullable Storage<FluidVariant> resolveItemFluid(ItemStack stack, ContainerItemContext context) {
      return FragileFluidContainerStorage.of(context);
   }
}
