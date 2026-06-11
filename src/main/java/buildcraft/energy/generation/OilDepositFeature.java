package buildcraft.energy.generation.adapter;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.core.OilGenDebugLog;
import buildcraft.energy.generation.core.OilGenerator;
import buildcraft.energy.generation.core.OilGenSettings;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Fabric feature: one pass per overworld chunk during {@code GenerationStep.Decoration.UNDERGROUND_DECORATION}.
 *
 * <p>Matches vanilla {@link Feature#place} - honours {@link WorldGenLevel#ensureCanWrite} and uses the decoration
 * {@code origin} from {@link net.minecraft.world.level.chunk.ChunkGenerator#applyBiomeDecoration} (chunk min corner).
 */
public class OilDepositFeature extends Feature<OilDepositFeatureConfiguration> {
   public OilDepositFeature(Codec<OilDepositFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<OilDepositFeatureConfiguration> context) {
      if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get()) {
         return false;
      }

      BlockPos origin = context.origin();
      WorldGenLevel level = context.level();
      if (!level.ensureCanWrite(origin)) {
         return false;
      }

      // #region agent log
      int chunkX = origin.getX() >> 4;
      int chunkZ = origin.getZ() >> 4;
      if (Math.abs(chunkX) <= 3 && Math.abs(chunkZ) <= 3) {
         OilGenDebugLog.log(
            "H42",
            "OilDepositFeature.place",
            "feature_pass",
            java.util.Map.of(
               "chunkX", chunkX,
               "chunkZ", chunkZ,
               "forcedTier", context.config().forcedTier().name(),
               "useDatapackSpawnChance", context.config().useDatapackSpawnChance(),
               "runId", "post-fix4"
            )
         );
      }
      // #endregion
      return OilGenerator.placeForChunk(level, origin, OilGenSettings.merged(context.config()));
   }
}
