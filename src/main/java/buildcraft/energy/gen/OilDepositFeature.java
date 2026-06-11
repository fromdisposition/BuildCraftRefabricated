package buildcraft.energy.gen;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.OilGenerator;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Fabric feature entry: one invocation per overworld chunk during {@code UNDERGROUND_DECORATION}.
 * Delegates to {@link OilGenerator#placeForChunk} with the chunk center as origin.
 */
public class OilDepositFeature extends Feature<NoneFeatureConfiguration> {
   public OilDepositFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get()) {
         return false;
      }

      WorldGenLevel level = context.level();
      if (level.getLevel().getChunkSource().getGenerator() instanceof FlatLevelSource) {
         return false;
      }

      int chunkX = context.origin().getX() >> 4;
      int chunkZ = context.origin().getZ() >> 4;
      BlockPos chunkAnchor = new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8);
      return OilGenerator.placeForChunk(level, chunkAnchor);
   }
}
