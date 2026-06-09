package buildcraft.fabric;

import buildcraft.api.facades.FacadeAPI;
import buildcraft.lib.mj.MjBlockCapabilities;
import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconCreativeTabs;
import buildcraft.silicon.BCSiliconEntities;
import buildcraft.silicon.BCSiliconIntegrationRecipes;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.BCSiliconRecipeSerializers;
import buildcraft.silicon.BCSiliconRecipes;
import buildcraft.silicon.BCSiliconStatements;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileChargingTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileLaser;
import buildcraft.silicon.tile.TilePackager;
import buildcraft.silicon.tile.TileProgrammingTable;
import buildcraft.silicon.tile.TileStampingTable;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import team.reborn.energy.api.EnergyStorage;

public final class BCSiliconFabric {
   private BCSiliconFabric() {
   }

   public static void register() {
      BCSiliconPlugs.preInit();
      BCSiliconStatements.preInit();
      FabricModuleBootstrap.registerContent(
         BCSiliconBlocks::register,
         BCSiliconItems::register,
         BCSiliconBlockEntities::register,
         BCSiliconMenuTypes::register
      );
      BCSiliconEntities.register();
      BCSiliconRecipeSerializers.register();
      BCSiliconCreativeTabs.register();
      FabricModuleBootstrap.registerCapabilities(BCSiliconFabric::registerMjCapabilities, BCSiliconFabric::registerNativeTransfer);
      ServerLifecycleEvents.SERVER_STARTING.register((ServerStarting)server -> {
         BCSiliconPlugs.registerAll();
         FacadeAPI.facadeItem = BCSiliconItems.PLUG_FACADE;
         FacadeAPI.registry = FacadeStateManager.INSTANCE;
         FacadeStateManager.ensureInitialized();
         BCSiliconRecipes.init();
         BCSiliconIntegrationRecipes.init();
      });
   }

   private static void registerNativeTransfer() {
      EnergyStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileLaser laser ? laser.getSidedEnergyStorage() : null, BCSiliconBlockEntities.LASER
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileAssemblyTable table ? table.getSidedItemStorage(direction) : null,
            BCSiliconBlockEntities.ASSEMBLY_TABLE
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileAdvancedCraftingTable table ? table.getSidedItemStorage(direction) : null,
            BCSiliconBlockEntities.ADVANCED_CRAFTING_TABLE
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileIntegrationTable table ? table.getSidedItemStorage(direction) : null,
            BCSiliconBlockEntities.INTEGRATION_TABLE
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileChargingTable table ? table.getSidedItemStorage(direction) : null,
            BCSiliconBlockEntities.CHARGING_TABLE
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileProgrammingTable table ? table.getSidedItemStorage(direction) : null,
            BCSiliconBlockEntities.PROGRAMMING_TABLE
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileStampingTable table ? table.getSidedItemStorage(direction) : null,
            BCSiliconBlockEntities.STAMPING_TABLE
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TilePackager packager ? packager.getSidedItemStorage(direction) : null,
            BCSiliconBlockEntities.PACKAGER
         );
   }

   private static void registerMjCapabilities() {
      MjBlockCapabilities.registerReceiver(BCSiliconBlockEntities.LASER, (laser, direction) -> laser.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCSiliconBlockEntities.LASER, (laser, direction) -> laser.getMjReceiver());
   }
}
