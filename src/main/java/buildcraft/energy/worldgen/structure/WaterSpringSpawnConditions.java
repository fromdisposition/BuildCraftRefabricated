package buildcraft.energy.worldgen.structure;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.worldgen.core.WorldgenDimensionFilters;
import buildcraft.energy.worldgen.processor.WaterSpringBedrockProcessor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class WaterSpringSpawnConditions {
   private WaterSpringSpawnConditions() {
   }

   public static boolean canSpawn(Structure.GenerationContext context) {
      if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.worldGenWaterSpring.get()) {
         return false;
      }
      if (context.chunkGenerator() instanceof FlatLevelSource) {
         return false;
      }
      if (WorldgenDimensionFilters.isDimensionExcluded(context)) {
         return false;
      }

      if (!(context.heightAccessor() instanceof WorldGenLevel level)) {
         return false;
      }

      ChunkPos chunkPos = context.chunkPos();
      int x = chunkPos.getMinBlockX() + context.random().nextInt(16);
      int z = chunkPos.getMinBlockZ() + context.random().nextInt(16);
      return WaterSpringBedrockProcessor.findBedrock(level, x, z) != null;
   }
}
