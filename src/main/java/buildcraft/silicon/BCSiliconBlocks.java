package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.container.ContainerIntegrationTable;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import net.minecraft.world.level.block.SoundType;

public final class BCSiliconBlocks {
   public static BlockLaser LASER;
   public static BlockLaserTable ASSEMBLY_TABLE;
   public static BlockLaserTable ADVANCED_CRAFTING_TABLE;
   public static BlockLaserTable INTEGRATION_TABLE;

   private BCSiliconBlocks() {
   }

   public static void register() {
      LASER = BCRegistries.registerBlock(
         "buildcraftsilicon", "laser", BlockLaser::new, p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      ASSEMBLY_TABLE = BCRegistries.registerBlock(
         "buildcraftsilicon",
         "assembly_table",
         props -> new BlockLaserTable(
            props, () -> BCSiliconBlockEntities.ASSEMBLY_TABLE, (id, inv, tile) -> new ContainerAssemblyTable(id, inv.player, (TileAssemblyTable)tile)
         ),
         p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      ADVANCED_CRAFTING_TABLE = BCRegistries.registerBlock(
         "buildcraftsilicon",
         "advanced_crafting_table",
         props -> new BlockLaserTable(
            props,
            () -> BCSiliconBlockEntities.ADVANCED_CRAFTING_TABLE,
            (id, inv, tile) -> new ContainerAdvancedCraftingTable(id, inv.player, (TileAdvancedCraftingTable)tile)
         ),
         p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      INTEGRATION_TABLE = BCRegistries.registerBlock(
         "buildcraftsilicon",
         "integration_table",
         props -> new BlockLaserTable(
            props, () -> BCSiliconBlockEntities.INTEGRATION_TABLE, (id, inv, tile) -> new ContainerIntegrationTable(id, inv.player, (TileIntegrationTable)tile)
         ),
         p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
   }
}
