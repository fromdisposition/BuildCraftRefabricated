package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;
import buildcraft.silicon.block.BlockPackager;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.container.ContainerChargingTable;
import buildcraft.silicon.container.ContainerIntegrationTable;
import buildcraft.silicon.container.ContainerPackager;
import buildcraft.silicon.container.ContainerProgrammingTable;
import buildcraft.silicon.container.ContainerStampingTable;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileChargingTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TilePackager;
import buildcraft.silicon.tile.TileProgrammingTable;
import buildcraft.silicon.tile.TileStampingTable;
import net.minecraft.world.level.block.SoundType;

public final class BCSiliconBlocks {
   public static BlockLaser LASER;
   public static BlockLaserTable ASSEMBLY_TABLE;
   public static BlockLaserTable ADVANCED_CRAFTING_TABLE;
   public static BlockLaserTable INTEGRATION_TABLE;
   public static BlockLaserTable CHARGING_TABLE;
   public static BlockLaserTable PROGRAMMING_TABLE;
   public static BlockLaserTable STAMPING_TABLE;
   public static BlockPackager PACKAGER;

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
      CHARGING_TABLE = BCRegistries.registerBlock(
         "buildcraftsilicon",
         "charging_table",
         props -> new BlockLaserTable(
            props, () -> BCSiliconBlockEntities.CHARGING_TABLE, (id, inv, tile) -> new ContainerChargingTable(id, inv.player, (TileChargingTable)tile)
         ),
         p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      PROGRAMMING_TABLE = BCRegistries.registerBlock(
         "buildcraftsilicon",
         "programming_table",
         props -> new BlockLaserTable(
            props, () -> BCSiliconBlockEntities.PROGRAMMING_TABLE, (id, inv, tile) -> new ContainerProgrammingTable(id, inv.player, (TileProgrammingTable)tile)
         ),
         p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      STAMPING_TABLE = BCRegistries.registerBlock(
         "buildcraftsilicon",
         "stamping_table",
         props -> new BlockLaserTable(
            props, () -> BCSiliconBlockEntities.STAMPING_TABLE, (id, inv, tile) -> new ContainerStampingTable(id, inv.player, (TileStampingTable)tile)
         ),
         p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      PACKAGER = BCRegistries.registerBlock(
         "buildcraftsilicon",
         "packager",
         props -> new BlockPackager(
            props, () -> BCSiliconBlockEntities.PACKAGER, (id, inv, tile) -> new ContainerPackager(id, inv.player, (TilePackager)tile)
         ),
         p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
   }
}
