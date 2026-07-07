package buildcraft.energy.worldgen.structure;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.worldgen.core.WorldgenDimensionFilters;
import buildcraft.energy.worldgen.core.WorldgenSpawnContext;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class WaterSpringSpawnConditions {
   private WaterSpringSpawnConditions() {
   }

   public static boolean canSpawn(Structure.GenerationContext context) {
      if (WorldgenSpawnContext.isChunkDecoration(context)
         && (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableWaterSprings.get())) {
         return false;
      }
      if (context.chunkGenerator() instanceof FlatLevelSource) {
         return false;
      }
      if (WorldgenDimensionFilters.isDimensionExcluded(context)) {
         return false;
      }

      return true;
   }
}
