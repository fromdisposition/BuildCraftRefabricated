package buildcraft.factory;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.fabric.BCRegistries;
import buildcraft.factory.block.BlockAutoWorkbenchItems;
import buildcraft.factory.block.BlockChute;
import buildcraft.factory.block.BlockDistiller;
import buildcraft.factory.block.BlockFloodGate;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.block.BlockMiningWell;
import buildcraft.factory.block.BlockPump;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.block.BlockTube;
import buildcraft.factory.block.BlockWaterGel;

public final class BCFactoryBlocks {
    public static BlockAutoWorkbenchItems AUTOWORKBENCH_ITEM;
    public static BlockMiningWell MINING_WELL;
    public static BlockPump PUMP;
    public static BlockFloodGate FLOOD_GATE;
    public static BlockTank TANK;
    public static BlockTube TUBE;
    public static BlockChute CHUTE;
    public static BlockDistiller DISTILLER;
    public static BlockHeatExchange HEAT_EXCHANGE;
    public static BlockWaterGel WATER_GEL;

    private BCFactoryBlocks() {}

    public static void register() {
        AUTOWORKBENCH_ITEM = BCRegistries.registerBlock(BCFactory.MODID, 
                "autoworkbench_item",
                BlockAutoWorkbenchItems::new,
                p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
        MINING_WELL = BCRegistries.registerBlock(BCFactory.MODID, 
                "mining_well",
                BlockMiningWell::new,
                p -> p.strength(5.0f, 10.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
        PUMP = BCRegistries.registerBlock(BCFactory.MODID, 
                "pump",
                BlockPump::new,
                p -> p.strength(5.0f, 10.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
        FLOOD_GATE = BCRegistries.registerBlock(BCFactory.MODID, 
                "flood_gate",
                BlockFloodGate::new,
                p -> p.strength(5.0f, 10.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
        TANK = BCRegistries.registerBlock(BCFactory.MODID, 
                "tank",
                BlockTank::new,
                p -> p.strength(0.3f).noOcclusion().sound(SoundType.GLASS).requiresCorrectToolForDrops());
        TUBE = BCRegistries.registerBlock(BCFactory.MODID, 
                "tube",
                BlockTube::new,
                p -> p.destroyTime(-1.0f).noOcclusion().sound(SoundType.METAL));
        CHUTE = BCRegistries.registerBlock(BCFactory.MODID, 
                "chute",
                BlockChute::new,
                p -> p.strength(5.0f, 10.0f).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops());
        DISTILLER = BCRegistries.registerBlock(BCFactory.MODID, 
                "distiller",
                BlockDistiller::new,
                p -> p.strength(5.0f, 10.0f).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops());
        HEAT_EXCHANGE = BCRegistries.registerBlock(BCFactory.MODID, 
                "heat_exchange",
                BlockHeatExchange::new,
                p -> p.strength(5.0f, 10.0f).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops());
        WATER_GEL = BCRegistries.registerBlock(BCFactory.MODID, 
                "water_gel",
                BlockWaterGel::new,
                p -> p.strength(0.6f).sound(net.minecraft.world.level.block.SoundType.SLIME_BLOCK));
    }
}
