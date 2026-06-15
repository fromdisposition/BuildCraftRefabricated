package buildcraft.factory.platform;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileAutoWorkbenchBase;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller;
import buildcraft.factory.tile.TileMiner;
import team.reborn.energy.api.EnergyStorage;

final class BCFactoryFabricTre {
   private BCFactoryFabricTre() {}

   static void register() {
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileMiner m ? m.getSidedEnergyStorage() : null,
         BCFactoryBlockEntities.MINING_WELL
      );
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileMiner m ? m.getSidedEnergyStorage() : null,
         BCFactoryBlockEntities.PUMP
      );
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileChute c ? c.getSidedEnergyStorage() : null,
         BCFactoryBlockEntities.CHUTE
      );
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileDistiller d ? d.getSidedEnergyStorage() : null,
         BCFactoryBlockEntities.DISTILLER
      );
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileAutoWorkbenchBase wb ? wb.getSidedEnergyStorage() : null,
         BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS
      );
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileAutoWorkbenchBase wb ? wb.getSidedEnergyStorage() : null,
         BCFactoryBlockEntities.AUTO_WORKBENCH_FLUIDS
      );
   }
}
