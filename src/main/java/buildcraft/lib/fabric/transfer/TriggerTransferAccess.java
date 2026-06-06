package buildcraft.lib.fabric.transfer;

import buildcraft.lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class TriggerTransferAccess {
   private TriggerTransferAccess() {
   }

   public static @Nullable Storage<FluidVariant> blockFluidStorage(Level level, BlockPos pos, @Nullable Direction side) {
      return BcTransfers.fluid(level, pos, side);
   }

   public static @Nullable Storage<FluidVariant> blockFluidStorage(
      Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, @Nullable Direction side
   ) {
      return BcTransfers.fluid(level, pos, state, blockEntity, side);
   }

   public static @Nullable Storage<ItemVariant> blockItemStorage(Level level, BlockPos pos, @Nullable Direction side) {
      return BcTransfers.item(level, pos, side);
   }

   public static @Nullable EnergyStorage blockEnergyStorage(Level level, BlockPos pos, @Nullable Direction side) {
      return BcTransfers.energy(level, pos, side);
   }

   public static boolean hasBlockFluid(Level level, BlockPos pos, @Nullable Direction side) {
      return TriggerFluidChecks.hasViews(blockFluidStorage(level, pos, side));
   }

   public static boolean hasBlockItem(Level level, BlockPos pos, @Nullable Direction side) {
      return TriggerItemChecks.hasViews(blockItemStorage(level, pos, side));
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
