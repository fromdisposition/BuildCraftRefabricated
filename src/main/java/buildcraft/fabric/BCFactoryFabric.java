package buildcraft.fabric;

import buildcraft.factory.BCFactoryAttachments;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileAutoWorkbenchFluids;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileMiner;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.fabric.transfer.AutoProvidingItemStorage;
import buildcraft.lib.mj.MjBlockCapabilities;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import team.reborn.energy.api.EnergyStorage;

public final class BCFactoryFabric {
   private BCFactoryFabric() {
   }

   public static void register() {
      FabricModuleBootstrap.registerStandardModule(
         BCFactoryBlocks::register,
         BCFactoryItems::register,
         BCFactoryBlockEntities::register,
         BCFactoryMenuTypes::register,
         BCFactoryFabric::registerMjCapabilities,
         BCFactoryFabric::registerNativeTransfer
      );
      BCFactoryAttachments.register();
   }

   private static void registerNativeTransfer() {
      FluidStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileTank tank ? tank.getColumnFluidStorage() : null, BCFactoryBlockEntities.TANK
         );
      FluidStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TilePump pump ? pump.getExtractFluidStorage() : null, BCFactoryBlockEntities.PUMP
         );
      FluidStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileDistiller_BC8 distiller ? distiller.getSidedFluidStorage(direction) : null,
            BCFactoryBlockEntities.DISTILLER
         );
      FluidStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileHeatExchange heatExchange ? heatExchange.getSidedFluidStorage(direction) : null,
            BCFactoryBlockEntities.HEAT_EXCHANGE
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileChute chute ? chute.getSidedItemStorage(direction) : null, BCFactoryBlockEntities.CHUTE
         );
      FluidStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileFloodGate floodGate ? floodGate.getSidedFluidStorage(direction) : null,
            BCFactoryBlockEntities.FLOOD_GATE
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileAutoWorkbenchItems workbench ? workbench.getSidedItemStorage(direction) : null,
            BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS
         );
      if (BCFactoryBlockEntities.AUTO_WORKBENCH_FLUIDS != null) {
         ItemStorage.SIDED
            .registerForBlockEntity(
               (blockEntity, direction) -> blockEntity instanceof TileAutoWorkbenchFluids workbench ? workbench.getSidedItemStorage(direction) : null,
               BCFactoryBlockEntities.AUTO_WORKBENCH_FLUIDS
            );
         FluidStorage.SIDED
            .registerForBlockEntity(
               (blockEntity, direction) -> blockEntity instanceof TileAutoWorkbenchFluids workbench ? workbench.getSidedFluidStorage(direction) : null,
               BCFactoryBlockEntities.AUTO_WORKBENCH_FLUIDS
            );
      }
      EnergyStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileMiner miner ? miner.getSidedEnergyStorage() : null, BCFactoryBlockEntities.MINING_WELL
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileMiningWell ? AutoProvidingItemStorage.INSTANCE : null, BCFactoryBlockEntities.MINING_WELL
         );
      EnergyStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileMiner miner ? miner.getSidedEnergyStorage() : null, BCFactoryBlockEntities.PUMP
         );
      EnergyStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileChute chute ? chute.getSidedEnergyStorage() : null, BCFactoryBlockEntities.CHUTE
         );
      EnergyStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileDistiller_BC8 distiller ? distiller.getSidedEnergyStorage() : null,
            BCFactoryBlockEntities.DISTILLER
         );
   }

   private static void registerMjCapabilities() {
      MjBlockCapabilities.registerReceiver(BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS, (workbench, direction) -> workbench.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS, (workbench, direction) -> workbench.getMjReceiver());
      if (BCFactoryBlockEntities.AUTO_WORKBENCH_FLUIDS != null) {
         MjBlockCapabilities.registerReceiver(BCFactoryBlockEntities.AUTO_WORKBENCH_FLUIDS, (workbench, direction) -> workbench.getMjReceiver());
         MjBlockCapabilities.registerConnector(BCFactoryBlockEntities.AUTO_WORKBENCH_FLUIDS, (workbench, direction) -> workbench.getMjReceiver());
      }
      MjBlockCapabilities.registerReceiver(BCFactoryBlockEntities.MINING_WELL, (miner, direction) -> miner.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCFactoryBlockEntities.MINING_WELL, (miner, direction) -> miner.getMjReceiver());
      MjBlockCapabilities.registerReceiver(BCFactoryBlockEntities.PUMP, (pump, direction) -> pump.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCFactoryBlockEntities.PUMP, (pump, direction) -> pump.getMjReceiver());
      MjBlockCapabilities.registerReceiver(BCFactoryBlockEntities.CHUTE, (chute, direction) -> chute.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCFactoryBlockEntities.CHUTE, (chute, direction) -> chute.getMjReceiver());
      MjBlockCapabilities.registerReceiver(BCFactoryBlockEntities.DISTILLER, (distiller, direction) -> distiller.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCFactoryBlockEntities.DISTILLER, (distiller, direction) -> distiller.getMjReceiver());
   }

   public static void onConfigReloaded() {
      TilePump.onConfigReloaded();
   }
}
