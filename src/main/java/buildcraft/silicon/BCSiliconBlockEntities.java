package buildcraft.silicon;

import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileLaser;

public final class BCSiliconBlockEntities {
    public static BlockEntityType<TileLaser> LASER;
    public static BlockEntityType<TileAssemblyTable> ASSEMBLY_TABLE;
    public static BlockEntityType<TileAdvancedCraftingTable> ADVANCED_CRAFTING_TABLE;
    public static BlockEntityType<TileIntegrationTable> INTEGRATION_TABLE;

    private BCSiliconBlockEntities() {}

    public static void register() {
        LASER = BCRegistries.registerBlockEntity(BCSilicon.MODID, "laser", TileLaser::new, BCSiliconBlocks.LASER.get());
        ASSEMBLY_TABLE = BCRegistries.registerBlockEntity(BCSilicon.MODID, 
                "assembly_table", TileAssemblyTable::new, BCSiliconBlocks.ASSEMBLY_TABLE.get());
        ADVANCED_CRAFTING_TABLE = BCRegistries.registerBlockEntity(BCSilicon.MODID, 
                "advanced_crafting_table", TileAdvancedCraftingTable::new,
                BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get());
        INTEGRATION_TABLE = BCRegistries.registerBlockEntity(BCSilicon.MODID,
                "integration_table", TileIntegrationTable::new, BCSiliconBlocks.INTEGRATION_TABLE.get());
    }
}
