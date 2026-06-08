package buildcraft.lib.fabric.transfer;

import buildcraft.lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Gate triggers and fluid widgets: item-fluid helpers and sided presence checks. Block lookups use {@link BcTransfers} directly. */
public final class TriggerTransferAccess {
   private TriggerTransferAccess() {
   }

   public static boolean hasBlockFluid(Level level, BlockPos pos, @Nullable Direction side) {
      return TriggerFluidChecks.hasViews(BcTransfers.fluid(level, pos, side));
   }

   public static boolean hasBlockItem(Level level, BlockPos pos, @Nullable Direction side) {
      return TriggerItemChecks.hasViews(BcTransfers.item(level, pos, side));
   }

   public static @Nullable Storage<FluidVariant> itemFluidStorage(ItemStack stack, ContainerItemContext context) {
      return ItemFluidLookup.storage(stack, context);
   }

   public static @Nullable Storage<FluidVariant> itemFluidStorage(ItemStack stack) {
      return ItemFluidLookup.storage(stack);
   }

   public static FluidStack fluidFromItemParameter(ItemStack stack) {
      return ItemFluidLookup.firstFluid(stack);
   }
}
