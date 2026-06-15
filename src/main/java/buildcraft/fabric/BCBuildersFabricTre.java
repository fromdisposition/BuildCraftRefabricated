package buildcraft.fabric;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import team.reborn.energy.api.EnergyStorage;

final class BCBuildersFabricTre {
   private BCBuildersFabricTre() {}

   static void register() {
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileQuarry q ? q.getSidedEnergyStorage() : null,
         BCBuildersBlockEntities.QUARRY
      );
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileFiller f ? f.getSidedEnergyStorage() : null,
         BCBuildersBlockEntities.FILLER
      );
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileBuilder b ? b.getSidedEnergyStorage() : null,
         BCBuildersBlockEntities.BUILDER
      );
   }
}
