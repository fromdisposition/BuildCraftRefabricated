package buildcraft.energy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

import buildcraft.energy.blocks.BlockDynamoMJ;
import buildcraft.energy.blocks.BlockEngineFE;
import buildcraft.energy.blocks.BlockEngineIron_BC8;
import buildcraft.energy.blocks.BlockEngineStone_BC8;
import buildcraft.fabric.BCRegistries;

public final class BCEnergyBlocks {
    public static BlockEngineStone_BC8 ENGINE_STONE;
    public static BlockEngineIron_BC8 ENGINE_IRON;
    public static BlockEngineFE ENGINE_FE;
    public static BlockDynamoMJ DYNAMO_MJ;

    private BCEnergyBlocks() {}

    public static void register() {
        ENGINE_STONE = BCRegistries.registerBlock(BCEnergy.MODID, 
                "engine_stone",
                BlockEngineStone_BC8::new,
                p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
        ENGINE_IRON = BCRegistries.registerBlock(BCEnergy.MODID, 
                "engine_iron",
                BlockEngineIron_BC8::new,
                p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
        ENGINE_FE = BCRegistries.registerBlock(BCEnergy.MODID, 
                "engine_rf",
                BlockEngineFE::new,
                p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
        DYNAMO_MJ = BCRegistries.registerBlock(BCEnergy.MODID, 
                "mj_dynamo",
                BlockDynamoMJ::new,
                p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
    }
}
