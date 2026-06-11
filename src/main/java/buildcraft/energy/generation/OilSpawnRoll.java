package buildcraft.energy.generation;

import buildcraft.energy.BCEnergyWorldGen;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

/**
 * Oil spawn math (all config percents are plain %, e.g. {@code 0.12} = 0.12% per chunk):
 * <ol>
 *   <li>Pick tier: NORMAL, RICH (desert list), or OIL_PATCH (synthetic oil_ocean / oil_desert).</li>
 *   <li>One roll: {@code random < tierPercent * generationMultiplier / 100}.</li>
 *   <li>If success, pick type by weights (large / medium / lake). Lakes only when tier != NORMAL.</li>
 * </ol>
 */
final class OilSpawnRoll {
   enum Tier {
      NORMAL,
      RICH,
      OIL_PATCH
   }

   private OilSpawnRoll() {
   }

   static boolean isOceanOil(Holder<Biome> biome, Identifier effectiveBiome) {
      return biome.is(BiomeTags.IS_OCEAN) || BCEnergyWorldGen.OIL_OCEAN.equals(effectiveBiome);
   }

   static Tier resolveTier(Identifier effectiveBiome, Identifier vanillaBiome, OilGenConfig config) {
      if (BCEnergyWorldGen.OIL_OCEAN.equals(effectiveBiome) || BCEnergyWorldGen.OIL_DESERT.equals(effectiveBiome)) {
         return Tier.OIL_PATCH;
      }
      if (config.richBiomes.contains(vanillaBiome) || config.richBiomes.contains(effectiveBiome)) {
         return Tier.RICH;
      }
      if (config.extraRichBiomes.contains(vanillaBiome) || config.extraRichBiomes.contains(effectiveBiome)) {
         return Tier.RICH;
      }
      return Tier.NORMAL;
   }

   static double spawnChanceFraction(Tier tier, OilGenConfig config) {
      double percent = switch (tier) {
         case NORMAL -> config.spawnChancePercentNormal;
         case RICH -> config.spawnChancePercentRich;
         case OIL_PATCH -> config.spawnChancePercentOilPatch;
      };
      percent *= config.generationMultiplier;
      if (percent <= 0.0) {
         return 0.0;
      }
      return Math.min(percent / 100.0, 1.0);
   }

   enum DepositType {
      LARGE,
      MEDIUM,
      LAKE
   }

   static DepositType rollDepositType(Random rand, Tier tier, OilGenConfig config) {
      int large = Math.max(0, config.typeWeightLarge);
      int medium = Math.max(0, config.typeWeightMedium);
      int lake = tier == Tier.NORMAL ? 0 : Math.max(0, config.typeWeightLake);
      int total = large + medium + lake;
      if (total <= 0) {
         return DepositType.MEDIUM;
      }
      int pick = rand.nextInt(total);
      if (pick < large) {
         return DepositType.LARGE;
      }
      pick -= large;
      if (pick < medium) {
         return DepositType.MEDIUM;
      }
      return DepositType.LAKE;
   }
}
