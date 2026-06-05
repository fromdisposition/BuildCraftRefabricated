package buildcraft.factory;

import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.fabric.BCRegistries;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;

public final class BCFactoryBlockEntities {
    public static BlockEntityType<TileAutoWorkbenchItems> AUTO_WORKBENCH_ITEMS;
    public static BlockEntityType<TileMiningWell> MINING_WELL;
    public static BlockEntityType<TilePump> PUMP;
    public static BlockEntityType<TileFloodGate> FLOOD_GATE;
    public static BlockEntityType<TileTank> TANK;
    public static BlockEntityType<TileChute> CHUTE;
    public static BlockEntityType<TileDistiller_BC8> DISTILLER;
    public static BlockEntityType<TileHeatExchange> HEAT_EXCHANGE;

    private BCFactoryBlockEntities() {}

    public static void register() {
        AUTO_WORKBENCH_ITEMS = BCRegistries.registerBlockEntity(BCFactory.MODID, 
                "autoworkbench_item", TileAutoWorkbenchItems::new, BCFactoryBlocks.AUTOWORKBENCH_ITEM);
        MINING_WELL = BCRegistries.registerBlockEntity(BCFactory.MODID, 
                "mining_well", TileMiningWell::new, BCFactoryBlocks.MINING_WELL);
        PUMP = BCRegistries.registerBlockEntity(BCFactory.MODID, "pump", TilePump::new, BCFactoryBlocks.PUMP);
        FLOOD_GATE = BCRegistries.registerBlockEntity(BCFactory.MODID, 
                "flood_gate", TileFloodGate::new, BCFactoryBlocks.FLOOD_GATE);
        TANK = BCRegistries.registerBlockEntity(BCFactory.MODID, "tank", TileTank::new, BCFactoryBlocks.TANK);
        CHUTE = BCRegistries.registerBlockEntity(BCFactory.MODID, "chute", TileChute::new, BCFactoryBlocks.CHUTE);
        DISTILLER = BCRegistries.registerBlockEntity(BCFactory.MODID, 
                "distiller", TileDistiller_BC8::new, BCFactoryBlocks.DISTILLER);
        HEAT_EXCHANGE = BCRegistries.registerBlockEntity(BCFactory.MODID, 
                "heat_exchange", TileHeatExchange::new, BCFactoryBlocks.HEAT_EXCHANGE);
    }
}
