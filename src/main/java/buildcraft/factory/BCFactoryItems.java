package buildcraft.factory;

import buildcraft.fabric.BCRegistries;
import buildcraft.factory.item.ItemWaterGel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class BCFactoryItems {
   public static BlockItem AUTOWORKBENCH_ITEM;
   public static BlockItem MINING_WELL;
   public static BlockItem PUMP;
   public static BlockItem FLOOD_GATE;
   public static BlockItem TANK;
   public static BlockItem CHUTE;
   public static BlockItem DISTILLER;
   public static BlockItem HEAT_EXCHANGE;
   public static ItemWaterGel WATER_GEL_SPAWN;
   public static Item GELLED_WATER;

   private BCFactoryItems() {
   }

   public static void register() {
      AUTOWORKBENCH_ITEM = BCRegistries.registerBlockItem("buildcraftfactory", "autoworkbench_item", BCFactoryBlocks.AUTOWORKBENCH_ITEM);
      MINING_WELL = BCRegistries.registerBlockItem("buildcraftfactory", "mining_well", BCFactoryBlocks.MINING_WELL);
      PUMP = BCRegistries.registerBlockItem("buildcraftfactory", "pump", BCFactoryBlocks.PUMP);
      FLOOD_GATE = BCRegistries.registerBlockItem("buildcraftfactory", "flood_gate", BCFactoryBlocks.FLOOD_GATE);
      TANK = BCRegistries.registerBlockItem("buildcraftfactory", "tank", BCFactoryBlocks.TANK);
      CHUTE = BCRegistries.registerBlockItem("buildcraftfactory", "chute", BCFactoryBlocks.CHUTE);
      DISTILLER = BCRegistries.registerBlockItem("buildcraftfactory", "distiller", BCFactoryBlocks.DISTILLER);
      HEAT_EXCHANGE = BCRegistries.registerBlockItem("buildcraftfactory", "heat_exchange", BCFactoryBlocks.HEAT_EXCHANGE);
      WATER_GEL_SPAWN = BCRegistries.registerItem("buildcraftfactory", "water_gel_spawn", props -> new ItemWaterGel(props.stacksTo(16)));
      GELLED_WATER = BCRegistries.registerItem("buildcraftfactory", "gelled_water", Item::new);
   }
}
