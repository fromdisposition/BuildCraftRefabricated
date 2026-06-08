package buildcraft.lib.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class BcTransfers {
   private static boolean initialized;

   private BcTransfers() {
   }

   public static synchronized void init() {
      if (!initialized) {
         initialized = true;
         VanillaTransferFallbacks.register();
         ItemFluidNativeFallbacks.register();
      }
   }

   public static @Nullable Storage<FluidVariant> fluid(
      Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side
   ) {
      return (Storage<FluidVariant>)FluidStorage.SIDED.find(level, pos, state, blockEntity, side);
   }

   public static @Nullable Storage<FluidVariant> fluid(Level level, BlockPos pos, @Nullable Direction side) {
      return fluid(level, pos, null, null, side);
   }

   public static @Nullable Storage<ItemVariant> item(
      Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side
   ) {
      return (Storage<ItemVariant>)ItemStorage.SIDED.find(level, pos, state, blockEntity, side);
   }

   public static @Nullable Storage<ItemVariant> item(Level level, BlockPos pos, @Nullable Direction side) {
      return item(level, pos, null, null, side);
   }

   public static @Nullable EnergyStorage energy(
      Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side
   ) {
      return (EnergyStorage)EnergyStorage.SIDED.find(level, pos, state, blockEntity, side);
   }

   public static @Nullable EnergyStorage energy(Level level, BlockPos pos, @Nullable Direction side) {
      return energy(level, pos, null, null, side);
   }
}
