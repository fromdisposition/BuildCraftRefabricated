package buildcraft.energy;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import buildcraft.fabric.BCRegistries;

public final class BCEnergyItems {
    public static BlockItem ENGINE_STONE;
    public static BlockItem ENGINE_IRON;
    public static BlockItem ENGINE_FE;
    public static BlockItem DYNAMO_MJ;
    public static Item GLOB_OF_OIL;

    private BCEnergyItems() {}

    public static void register() {
        ENGINE_STONE = BCRegistries.registerBlockItem(BCEnergy.MODID, "engine_stone", BCEnergyBlocks.ENGINE_STONE);
        ENGINE_IRON = BCRegistries.registerBlockItem(BCEnergy.MODID, "engine_iron", BCEnergyBlocks.ENGINE_IRON);
        ENGINE_FE = BCRegistries.registerBlockItem(BCEnergy.MODID, "engine_rf", BCEnergyBlocks.ENGINE_FE);
        DYNAMO_MJ = BCRegistries.registerBlockItem(BCEnergy.MODID, "mj_dynamo", BCEnergyBlocks.DYNAMO_MJ);
        GLOB_OF_OIL = BCRegistries.registerItem(BCEnergy.MODID, "glob_of_oil", Item::new);
    }
}
