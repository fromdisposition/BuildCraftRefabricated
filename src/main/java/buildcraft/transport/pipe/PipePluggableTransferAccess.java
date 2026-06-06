package buildcraft.transport.pipe;

import buildcraft.api.transport.pluggable.PipePluggable;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import team.reborn.energy.api.EnergyStorage;

public final class PipePluggableTransferAccess {
   private PipePluggableTransferAccess() {
   }

   @Nullable
   public static Storage<FluidVariant> fluidStorage(@Nullable PipePluggable plug) {
      return plug == null ? null : plug.fluidStorage();
   }

   @Nullable
   public static Storage<ItemVariant> itemStorage(@Nullable PipePluggable plug) {
      return plug == null ? null : plug.itemStorage();
   }

   @Nullable
   public static EnergyStorage energyStorage(@Nullable PipePluggable plug) {
      return plug == null ? null : plug.energyStorage();
   }
}
