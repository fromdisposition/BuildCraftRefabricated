package buildcraft.fabric;

import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineRF;
import team.reborn.energy.api.EnergyStorage;

final class BCEnergyFabricTre {
   private BCEnergyFabricTre() {}

   static void register() {
      if (BCEnergyBlockEntities.ENGINE_FE != null) {
         EnergyStorage.SIDED.registerForBlockEntity(
            (be, dir) -> be instanceof TileEngineRF e ? e.getSidedEnergyStorage(dir) : null,
            BCEnergyBlockEntities.ENGINE_FE
         );
      }
      if (BCEnergyBlockEntities.DYNAMO_MJ != null) {
         EnergyStorage.SIDED.registerForBlockEntity(
            (be, dir) -> be instanceof TileDynamoMJ d ? d.getSidedEnergyStorage(dir) : null,
            BCEnergyBlockEntities.DYNAMO_MJ
         );
      }
   }
}
