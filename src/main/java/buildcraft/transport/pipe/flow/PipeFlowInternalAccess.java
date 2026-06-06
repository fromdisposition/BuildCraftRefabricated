package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.PipeFlow;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import team.reborn.energy.api.EnergyStorage;

public final class PipeFlowInternalAccess {
   private PipeFlowInternalAccess() {
   }

   @Nullable
   public static Storage<FluidVariant> fluidStorage(@Nullable PipeFlow flow, @Nullable Direction facing) {
      return flow instanceof PipeFlowFluids fluids && facing != null ? fluids.getFluidStorage(facing) : null;
   }

   @Nullable
   public static Storage<ItemVariant> itemStorage(@Nullable PipeFlow flow, @Nullable Direction facing) {
      return flow instanceof PipeFlowItems items && facing != null ? items.getItemStorage(facing) : null;
   }

   @Nullable
   public static EnergyStorage energyStorage(@Nullable PipeFlow flow, @Nullable Direction facing) {
      return flow instanceof PipeFlowRedstoneFlux rf && facing != null ? rf.getEnergyStorage(facing) : null;
   }
}
