package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileLaser;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCSiliconBlockEntities {
   public static BlockEntityType<TileLaser> LASER;
   public static BlockEntityType<TileAssemblyTable> ASSEMBLY_TABLE;
   public static BlockEntityType<TileAdvancedCraftingTable> ADVANCED_CRAFTING_TABLE;
   public static BlockEntityType<TileIntegrationTable> INTEGRATION_TABLE;

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
   }
}
