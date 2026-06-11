package buildcraft.energy.generation;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Fabric feature: one pass per overworld chunk during {@code GenerationStep.Decoration.UNDERGROUND_DECORATION}.
 *
 * <p>Matches vanilla {@link Feature#place} - honours {@link WorldGenLevel#ensureCanWrite} and uses the decoration
 * {@code origin} from {@link net.minecraft.world.level.chunk.ChunkGenerator#applyBiomeDecoration} (chunk min corner).
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

      BlockPos origin = context.origin();
      WorldGenLevel level = context.level();
      if (!level.ensureCanWrite(origin)) {
         return false;
      }

      return OilGenerator.placeForChunk(level, origin);
   }
}
