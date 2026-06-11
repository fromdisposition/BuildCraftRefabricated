package buildcraft.energy.generation.adapter;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
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
 * <p>Spawn chance is enforced by {@code rarity_filter} on the placed feature. {@link OilGenerator} only scans and
 * places procedural geometry.
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

      return OilGenerator.placeForChunk(level, origin, OilGenSettings.from(context.config()));
   }
}
