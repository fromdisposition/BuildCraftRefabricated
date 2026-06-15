package buildcraft.fabric;

import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.tile.TileLaser;
import buildcraft.silicon.tile.TileLaserTableBase;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

final class BCSiliconFabricTre {
   private BCSiliconFabricTre() {}

   static void register() {
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileLaser l ? l.getSidedEnergyStorage() : null,
         BCSiliconBlockEntities.LASER
      );
      registerLaserTableEnergy(BCSiliconBlockEntities.ASSEMBLY_TABLE);
      registerLaserTableEnergy(BCSiliconBlockEntities.ADVANCED_CRAFTING_TABLE);
      registerLaserTableEnergy(BCSiliconBlockEntities.INTEGRATION_TABLE);
      registerLaserTableEnergy(BCSiliconBlockEntities.CHARGING_TABLE);
      registerLaserTableEnergy(BCSiliconBlockEntities.PROGRAMMING_TABLE);
      registerLaserTableEnergy(BCSiliconBlockEntities.STAMPING_TABLE);
   }

   private static void registerLaserTableEnergy(BlockEntityType<? extends TileLaserTableBase> type) {
      EnergyStorage.SIDED.registerForBlockEntity(
         (be, dir) -> be instanceof TileLaserTableBase t ? t.getSidedEnergyStorage() : null, type
      );
   }
}
