package buildcraft.factory.platform;

import buildcraft.factory.BCFactoryAttachments;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryEntities;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileAutoWorkbenchBase;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileMiner;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.fabric.transfer.AutoProvidingItemStorage;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.MjAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;

public final class BCFactoryFabric {
   private BCFactoryFabric() {
   }

   public static void register() {
      BCFactoryBlocks.register();
      BCFactoryItems.register();
      BCFactoryBlockEntities.register();
      BCFactoryMenuTypes.register();
      registerMjCapabilities();
      registerNativeTransfer();
      BCFactoryEntities.register();
      BCFactoryAttachments.register();
      ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
         if (blockEntity instanceof TileTank tank) tank.onLoad();
         else if (blockEntity instanceof TileMiner miner) miner.onLoad();
         else if (blockEntity instanceof TileFloodGate floodGate) floodGate.onLoad();
      });
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
            (blockEntity, direction) -> blockEntity instanceof TileDistiller distiller ? distiller.getSidedFluidStorage(direction) : null,
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
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileMiningWell ? AutoProvidingItemStorage.INSTANCE : null, BCFactoryBlockEntities.MINING_WELL
         );
      if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
         BCFactoryFabricTre.register();
      }
   }

   private static void registerMjCapabilities() {
      MjAPI.CAP_RECEIVER.registerForBlockEntity((workbench, direction) -> workbench.getMjReceiver(), BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS);
      MjAPI.CAP_CONNECTOR.registerForBlockEntity((workbench, direction) -> workbench.getMjReceiver(), BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS);
      MjAPI.CAP_RECEIVER.registerForBlockEntity((miner, direction) -> miner.getMjReceiver(), BCFactoryBlockEntities.MINING_WELL);
      MjAPI.CAP_CONNECTOR.registerForBlockEntity((miner, direction) -> miner.getMjReceiver(), BCFactoryBlockEntities.MINING_WELL);
      MjAPI.CAP_RECEIVER.registerForBlockEntity((pump, direction) -> pump.getMjReceiver(), BCFactoryBlockEntities.PUMP);
      MjAPI.CAP_CONNECTOR.registerForBlockEntity((pump, direction) -> pump.getMjReceiver(), BCFactoryBlockEntities.PUMP);
      MjAPI.CAP_RECEIVER.registerForBlockEntity((chute, direction) -> chute.getMjReceiver(), BCFactoryBlockEntities.CHUTE);
      MjAPI.CAP_CONNECTOR.registerForBlockEntity((chute, direction) -> chute.getMjReceiver(), BCFactoryBlockEntities.CHUTE);
      MjAPI.CAP_RECEIVER.registerForBlockEntity((distiller, direction) -> distiller.getMjReceiver(), BCFactoryBlockEntities.DISTILLER);
      MjAPI.CAP_CONNECTOR.registerForBlockEntity((distiller, direction) -> distiller.getMjReceiver(), BCFactoryBlockEntities.DISTILLER);
      // MjBatteryReceiver / MjRedstoneBatteryReceiver both implement IMjReadable
      MjAPI.CAP_READABLE.registerForBlockEntity((chute, direction) -> chute.getMjReceiver() instanceof IMjReadable r ? r : null, BCFactoryBlockEntities.CHUTE);
      MjAPI.CAP_READABLE.registerForBlockEntity((distiller, direction) -> distiller.getMjReceiver() instanceof IMjReadable r ? r : null, BCFactoryBlockEntities.DISTILLER);
      MjAPI.CAP_READABLE.registerForBlockEntity((wb, direction) -> wb.getMjReceiver() instanceof IMjReadable r ? r : null, BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS);
   }

   public static void onConfigReloaded() {
      TilePump.onConfigReloaded();
   }
}
