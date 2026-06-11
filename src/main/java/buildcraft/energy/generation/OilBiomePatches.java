package buildcraft.energy.generation;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

/**
 * Synthetic oil_ocean / oil_desert ids for spawn rolls (BC 1.12 GenLayer replacement).
 */
public final class OilBiomePatches {
   public static final Identifier OIL_OCEAN = Identifier.parse("buildcraftenergy:oil_ocean");
   public static final Identifier OIL_DESERT = Identifier.parse("buildcraftenergy:oil_desert");

   private OilBiomePatches() {
   }

   public static Identifier effectiveBiomeId(ServerLevel level, int x, int z, Holder<Biome> biome, Identifier fallback) {
      OilGenSettings settings = OilGenSettings.current();
      if (settings.enableOilOnWater()
         && settings.enableOilOceanBiome()
         && isShallowOcean(biome, fallback)) {
         double patchChance = clampChance(settings.oilOceanPatchChance());
         if (sampleNoise(level.getSeed(), x, z, 0.0005) >= 1.0 - patchChance) {
            return OIL_OCEAN;
         }
      }
      if (settings.enableOilDesertBiome() && isDesertLike(fallback.getPath())) {
         double patchChance = clampChance(settings.oilDesertPatchChance());
         if (sampleNoise(level.getSeed() ^ 0x5EED5EEDL, x, z, 0.001) >= 1.0 - patchChance) {
            return OIL_DESERT;
         }
      }
      return fallback;
   }

   private static double clampChance(double chance) {
      if (chance <= 0.0) {
         return 0.0;
      }
      return Math.min(chance, 1.0);
   }

   private static boolean isShallowOcean(Holder<Biome> biome, Identifier id) {
      if (!biome.is(BiomeTags.IS_OCEAN)) {
         return false;
      }
      String path = id.getPath();
      return !path.contains("deep") && !path.equals("frozen_ocean");
   }

   private static boolean isDesertLike(String path) {
      return path.contains("desert") || path.contains("badlands");
   }

   private static double sampleNoise(long seed, int x, int z, double scale) {
      long sx = (long) Math.floor(x * scale * 8192.0);
      long sz = (long) Math.floor(z * scale * 8192.0);
      long h = seed;
      h ^= sx * 0x9E3779B97F4A7C15L;
      h ^= sz * 0xC2B2AE3D27D4EB4FL;
      h ^= (h >>> 33);
      h *= 0xff51afd7ed558ccdL;
      h ^= (h >>> 33);
      h *= 0xc4ceb9fe1a85ec53L;
      h ^= (h >>> 33);
      return (h & 0x1fffffffffffffL) / (double) 0x1fffffffffffffL;
   }
}
