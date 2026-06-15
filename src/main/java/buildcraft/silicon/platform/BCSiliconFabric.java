package buildcraft.silicon.platform;

import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.MjAPI;
import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconCreativeTabs;
import buildcraft.silicon.BCSiliconEntities;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.BCSiliconRecipeSerializers;
import buildcraft.silicon.BCSiliconStatements;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileChargingTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileLaser;
import buildcraft.silicon.tile.TileLaserTableBase;
import buildcraft.silicon.tile.TilePackager;
import buildcraft.silicon.tile.TileProgrammingTable;
import buildcraft.silicon.tile.TileStampingTable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;

public final class BCSiliconFabric {
   private BCSiliconFabric() {
   }

   public static void register() {
      BCSiliconPlugs.preInit();
      BCSiliconStatements.preInit();
      BCSiliconBlocks.register();
      BCSiliconItems.register();
      BCSiliconBlockEntities.register();
      BCSiliconMenuTypes.register();
      BCSiliconEntities.register();
      BCSiliconRecipeSerializers.register();
      BCSiliconCreativeTabs.register();
      registerMjCapabilities();
      registerNativeTransfer();
   }

   private static void registerNativeTransfer() {
      if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
         BCSiliconFabricTre.register();
      }
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
      MjAPI.CAP_RECEIVER.registerForBlockEntity((laser, direction) -> laser.getMjReceiver(), BCSiliconBlockEntities.LASER);
      MjAPI.CAP_CONNECTOR.registerForBlockEntity((laser, direction) -> laser.getMjReceiver(), BCSiliconBlockEntities.LASER);
      MjAPI.CAP_READABLE.registerForBlockEntity((laser, direction) -> laser.getMjReceiver() instanceof IMjReadable r ? r : null, BCSiliconBlockEntities.LASER);
   }
}
