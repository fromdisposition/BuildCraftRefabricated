package buildcraft.energy;

import buildcraft.energy.blocks.BlockDynamoMJ;
import buildcraft.energy.blocks.BlockEngineFE;
import buildcraft.energy.blocks.BlockEngineIron_BC8;
import buildcraft.energy.blocks.BlockEngineStone_BC8;
import buildcraft.fabric.BCRegistries;
import net.minecraft.world.level.block.SoundType;

public final class BCEnergyBlocks {
   public static BlockEngineStone_BC8 ENGINE_STONE;
   public static BlockEngineIron_BC8 ENGINE_IRON;
   public static BlockEngineFE ENGINE_FE;
   public static BlockDynamoMJ DYNAMO_MJ;

   private BCEnergyBlocks() {
   }

   public static void register() {
      ENGINE_STONE = BCRegistries.registerBlock(
         "buildcraftenergy", "engine_stone", BlockEngineStone_BC8::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      ENGINE_IRON = BCRegistries.registerBlock(
         "buildcraftenergy", "engine_iron", BlockEngineIron_BC8::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      ENGINE_FE = BCRegistries.registerBlock(
         "buildcraftenergy", "engine_rf", BlockEngineFE::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      DYNAMO_MJ = BCRegistries.registerBlock(
         "buildcraftenergy", "mj_dynamo", BlockDynamoMJ::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
   }
}
