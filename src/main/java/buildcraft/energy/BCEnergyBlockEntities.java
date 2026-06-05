package buildcraft.energy;

import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.block.BlockSpring;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.fabric.BCRegistries;

public final class BCEnergyBlockEntities {
    public static BlockEntityType<TileSpringOil> SPRING_OIL;
    public static BlockEntityType<TileEngineStone_BC8> ENGINE_STONE;
    public static BlockEntityType<TileEngineIron_BC8> ENGINE_IRON;
    public static BlockEntityType<TileEngineRF> ENGINE_FE;
    public static BlockEntityType<TileDynamoMJ> DYNAMO_MJ;

    private BCEnergyBlockEntities() {}

    public static void register() {
        if (BCCoreBlocks.SPRING_OIL != null) {
            SPRING_OIL = BCRegistries.registerBlockEntity(BCEnergy.MODID, 
                    "spring_oil", TileSpringOil::new, BCCoreBlocks.SPRING_OIL);
            BlockSpring.oilTileFactory = TileSpringOil::new;
        }
        if (BCEnergyBlocks.ENGINE_STONE != null) {
            ENGINE_STONE = BCRegistries.registerBlockEntity(BCEnergy.MODID, 
                    "engine_stone", TileEngineStone_BC8::new, BCEnergyBlocks.ENGINE_STONE);
        }
        if (BCEnergyBlocks.ENGINE_IRON != null) {
            ENGINE_IRON = BCRegistries.registerBlockEntity(BCEnergy.MODID, 
                    "engine_iron", TileEngineIron_BC8::new, BCEnergyBlocks.ENGINE_IRON);
        }
        if (BCEnergyBlocks.ENGINE_FE != null) {
            ENGINE_FE = BCRegistries.registerBlockEntity(BCEnergy.MODID, 
                    "engine_rf", TileEngineRF::new, BCEnergyBlocks.ENGINE_FE);
        }
        if (BCEnergyBlocks.DYNAMO_MJ != null) {
            DYNAMO_MJ = BCRegistries.registerBlockEntity(BCEnergy.MODID, 
                    "mj_dynamo", TileDynamoMJ::new, BCEnergyBlocks.DYNAMO_MJ);
        }
    }
}
