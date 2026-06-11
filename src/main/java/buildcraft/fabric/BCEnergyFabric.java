package buildcraft.fabric;

import buildcraft.api.enums.EnumSpring;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.worldgen.BCEnergyWorldgen;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.mj.MjBlockCapabilities;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import team.reborn.energy.api.EnergyStorage;

public final class BCEnergyFabric {
   private BCEnergyFabric() {
   }

   public static void register() {
      BCEnergyFluidsFabric.register();
      BCEnergyFluids.refreshSnapshot();
      BCEnergyBlocks.register();
      BCEnergyItems.register();
      BCEnergyBlockEntities.register();
      BCEnergyMenuTypes.register();
      registerMjCapabilities();
      registerNativeTransfer();
      EnumSpring.OIL.liquidBlock = BCEnergyFluidsFabric.sourceBlockState(BCEnergyFluidsFabric.OIL_COOL);
      applySpringConfig();
      BCEnergyWorldgen.register();
   }

   public static void onConfigReloaded() {
      BCEnergyFluidsFabric.reapplyConfigProperties();
      BCEnergyFluids.refreshSnapshot();
      applySpringConfig();
   }

   private static void applySpringConfig() {
      EnumSpring.OIL.canGen = BCEnergyConfig.enableOilSprings.get();
      BCEnergyConfig.refreshWaterSpringFlag();
   }

   private static void registerNativeTransfer() {
      FluidStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileEngineIron_BC8 engine ? engine.getSidedFluidStorage(direction) : null,
            BCEnergyBlockEntities.ENGINE_IRON
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileEngineStone_BC8 engine ? engine.getSidedFuelStorage(direction) : null,
            BCEnergyBlockEntities.ENGINE_STONE
         );
      if (BCEnergyBlockEntities.ENGINE_FE != null) {
         EnergyStorage.SIDED
            .registerForBlockEntity(
               (blockEntity, direction) -> blockEntity instanceof TileEngineRF engine ? engine.getSidedEnergyStorage(direction) : null,
               BCEnergyBlockEntities.ENGINE_FE
            );
      }

      if (BCEnergyBlockEntities.DYNAMO_MJ != null) {
         EnergyStorage.SIDED
            .registerForBlockEntity(
               (blockEntity, direction) -> blockEntity instanceof TileDynamoMJ dynamo ? dynamo.getSidedEnergyStorage(direction) : null,
               BCEnergyBlockEntities.DYNAMO_MJ
            );
      }
   }

   private static void registerMjCapabilities() {
      MjBlockCapabilities.registerConnector(BCEnergyBlockEntities.ENGINE_STONE, (engine, direction) -> engine.getMjConnector());
      MjBlockCapabilities.registerConnector(BCEnergyBlockEntities.ENGINE_IRON, (engine, direction) -> engine.getMjConnector());
      if (BCEnergyBlockEntities.ENGINE_FE != null) {
         MjBlockCapabilities.registerConnector(
            BCEnergyBlockEntities.ENGINE_FE, (engine, direction) -> direction != engine.getOrientation() ? null : engine.getMjConnector()
         );
      }

      if (BCEnergyBlockEntities.DYNAMO_MJ != null) {
         MjBlockCapabilities.registerConnector(
            BCEnergyBlockEntities.DYNAMO_MJ, (dynamo, direction) -> direction == dynamo.getOrientation() ? null : dynamo.getMjConnector()
         );
         MjBlockCapabilities.registerReceiver(BCEnergyBlockEntities.DYNAMO_MJ, (dynamo, direction) -> {
            if (direction == dynamo.getOrientation()) {
               return null;
            } else {
               return dynamo.getMjConnector() instanceof IMjReceiver receiver ? receiver : null;
            }
         });
      }
   }
}
