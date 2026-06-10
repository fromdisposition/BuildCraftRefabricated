package buildcraft.energy.gen;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.OilGenerator;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Vanilla {@link Feature} hook for oil deposits. Geometry is defined in {@link buildcraft.energy.generation.OilGenStructure};
 * {@link OilGenerator} rolls one deposit per chunk at chunk center and may clip into cardinal neighbor chunks.
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

      return OilGenerator.placeForChunk(level, context.origin(), context.random());
   }
}
