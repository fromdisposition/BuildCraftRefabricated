package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileChargingTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileLaser;
import buildcraft.silicon.tile.TilePackager;
import buildcraft.silicon.tile.TileProgrammingTable;
import buildcraft.silicon.tile.TileStampingTable;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCSiliconBlockEntities {
   public static BlockEntityType<TileLaser> LASER;
   public static BlockEntityType<TileAssemblyTable> ASSEMBLY_TABLE;
   public static BlockEntityType<TileAdvancedCraftingTable> ADVANCED_CRAFTING_TABLE;
   public static BlockEntityType<TileIntegrationTable> INTEGRATION_TABLE;
   public static BlockEntityType<TileChargingTable> CHARGING_TABLE;
   public static BlockEntityType<TileProgrammingTable> PROGRAMMING_TABLE;
   public static BlockEntityType<TileStampingTable> STAMPING_TABLE;
   public static BlockEntityType<TilePackager> PACKAGER;

   private BCSiliconBlockEntities() {
   }

   public static void register() {
      LASER = BCRegistries.registerBlockEntity("buildcraftsilicon", "laser", TileLaser::new, BCSiliconBlocks.LASER);
      ASSEMBLY_TABLE = BCRegistries.registerBlockEntity("buildcraftsilicon", "assembly_table", TileAssemblyTable::new, BCSiliconBlocks.ASSEMBLY_TABLE);
      ADVANCED_CRAFTING_TABLE = BCRegistries.registerBlockEntity(
         "buildcraftsilicon", "advanced_crafting_table", TileAdvancedCraftingTable::new, BCSiliconBlocks.ADVANCED_CRAFTING_TABLE
      );
      INTEGRATION_TABLE = BCRegistries.registerBlockEntity(
         "buildcraftsilicon", "integration_table", TileIntegrationTable::new, BCSiliconBlocks.INTEGRATION_TABLE
      );
      CHARGING_TABLE = BCRegistries.registerBlockEntity("buildcraftsilicon", "charging_table", TileChargingTable::new, BCSiliconBlocks.CHARGING_TABLE);
      PROGRAMMING_TABLE = BCRegistries.registerBlockEntity(
         "buildcraftsilicon", "programming_table", TileProgrammingTable::new, BCSiliconBlocks.PROGRAMMING_TABLE
      );
      STAMPING_TABLE = BCRegistries.registerBlockEntity(
         "buildcraftsilicon", "stamping_table", TileStampingTable::new, BCSiliconBlocks.STAMPING_TABLE
      );
      PACKAGER = BCRegistries.registerBlockEntity("buildcraftsilicon", "packager", TilePackager::new, BCSiliconBlocks.PACKAGER);
   }
}
