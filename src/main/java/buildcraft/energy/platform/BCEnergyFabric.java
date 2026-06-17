package buildcraft.energy.platform;

import buildcraft.api.enums.EnumSpring;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.BCEnergyRecipeSerializers;
import buildcraft.energy.BCEnergyRecipeTypes;
import buildcraft.energy.worldgen.BCEnergyWorldgen;
import buildcraft.energy.BCEnergyItems;
import buildcraft.fabric.fluid.BcFluidEntityInteractions;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.MjAPI;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;

public final class BCEnergyFabric {
   private BCEnergyFabric() {
   }

   public static void register() {
      BCEnergyRecipeTypes.register();
      BCEnergyRecipeSerializers.register();
      BCEnergyFluidsFabric.register();
      BcFluidEntityInteractions.register();
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
      if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
         BCEnergyFabricTre.register();
      }
   }

   private static void registerMjCapabilities() {
      MjAPI.CAP_CONNECTOR.registerForBlockEntity((engine, direction) -> engine.getMjConnector(), BCEnergyBlockEntities.ENGINE_STONE);
      MjAPI.CAP_CONNECTOR.registerForBlockEntity((engine, direction) -> engine.getMjConnector(), BCEnergyBlockEntities.ENGINE_IRON);
      if (BCEnergyBlockEntities.ENGINE_FE != null) {
         MjAPI.CAP_CONNECTOR.registerForBlockEntity(
            (engine, direction) -> direction != engine.getOrientation() ? null : engine.getMjConnector(), BCEnergyBlockEntities.ENGINE_FE
         );
      }

      if (BCEnergyBlockEntities.DYNAMO_MJ != null) {
         MjAPI.CAP_CONNECTOR.registerForBlockEntity(
            (dynamo, direction) -> direction == dynamo.getOrientation() ? null : dynamo.getMjConnector(), BCEnergyBlockEntities.DYNAMO_MJ
         );
         MjAPI.CAP_RECEIVER.registerForBlockEntity((dynamo, direction) -> {
            if (direction == dynamo.getOrientation()) {
               return null;
            } else {
               return dynamo.getMjConnector() instanceof IMjReceiver receiver ? receiver : null;
            }
         }, BCEnergyBlockEntities.DYNAMO_MJ);
         MjAPI.CAP_READABLE.registerForBlockEntity((dynamo, direction) -> dynamo.getMjReceiver() instanceof IMjReadable r ? r : null, BCEnergyBlockEntities.DYNAMO_MJ);
      }
   }
}
