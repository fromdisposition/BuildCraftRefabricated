package buildcraft.energy;

import buildcraft.fabric.BCRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class BCEnergyItems {
   public static BlockItem ENGINE_STONE;
   public static BlockItem ENGINE_IRON;
   public static BlockItem ENGINE_FE;
   public static BlockItem DYNAMO_MJ;
   public static Item GLOB_OF_OIL;

   private BCEnergyItems() {
   }

   public static void register() {
      ENGINE_STONE = BCRegistries.registerBlockItem("buildcraftenergy", "engine_stone", BCEnergyBlocks.ENGINE_STONE);
      ENGINE_IRON = BCRegistries.registerBlockItem("buildcraftenergy", "engine_iron", BCEnergyBlocks.ENGINE_IRON);
      ENGINE_FE = BCRegistries.registerBlockItem("buildcraftenergy", "engine_rf", BCEnergyBlocks.ENGINE_FE);
      DYNAMO_MJ = BCRegistries.registerBlockItem("buildcraftenergy", "mj_dynamo", BCEnergyBlocks.DYNAMO_MJ);
      GLOB_OF_OIL = BCRegistries.registerItem("buildcraftenergy", "glob_of_oil", Item::new);
   }
}
