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
 * Native Fabric entry point for oil worldgen. All deposit geometry lives in {@link OilGenerator} / {@link buildcraft.energy.generation.OilGenStructure}.
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

      return OilGenerator.placeForChunk(level, context.origin());
   }
}
