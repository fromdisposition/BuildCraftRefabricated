package buildcraft.transport.platform;

import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.tile.TilePipeHolder;
import team.reborn.energy.api.EnergyStorage;

final class BCTransportFabricTre {
   private BCTransportFabricTre() {}

   static void register() {
      EnergyStorage.SIDED.registerForBlockEntity(
         (tile, side) -> tile instanceof TilePipeHolder h ? h.getSidedEnergyStorage(side) : null,
         BCTransportBlockEntities.PIPE_HOLDER
      );
   }
}
