package buildcraft.builders;

import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.fabric.BCRegistries;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.builders.tile.TileReplacer;

public final class BCBuildersBlockEntities {
    public static BlockEntityType<TileFiller> FILLER;
    public static BlockEntityType<TileBuilder> BUILDER;
    public static BlockEntityType<TileArchitectTable> ARCHITECT;
    public static BlockEntityType<TileElectronicLibrary> LIBRARY;
    public static BlockEntityType<TileReplacer> REPLACER;
    public static BlockEntityType<TileQuarry> QUARRY;

    private BCBuildersBlockEntities() {}

    public static void register() {
        FILLER = BCRegistries.registerBlockEntity(BCBuilders.MODID, "filler", TileFiller::new, BCBuildersBlocks.FILLER.get());
        BUILDER = BCRegistries.registerBlockEntity(BCBuilders.MODID, "builder", TileBuilder::new, BCBuildersBlocks.BUILDER.get());
        ARCHITECT = BCRegistries.registerBlockEntity(BCBuilders.MODID, "architect", TileArchitectTable::new, BCBuildersBlocks.ARCHITECT.get());
        LIBRARY = BCRegistries.registerBlockEntity(BCBuilders.MODID, "library", TileElectronicLibrary::new, BCBuildersBlocks.LIBRARY.get());
        REPLACER = BCRegistries.registerBlockEntity(BCBuilders.MODID, "replacer", TileReplacer::new, BCBuildersBlocks.REPLACER.get());
        QUARRY = BCRegistries.registerBlockEntity(BCBuilders.MODID, "quarry", TileQuarry::new, BCBuildersBlocks.QUARRY.get());
    }
}
