package buildcraft.energy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

/**
 * Fabric adaptation of BC 8.0 oil biome shaping.
 *
 * <p>1.12.2 used GenLayer replacement to inject oil_ocean/oil_desert biomes. Modern versions don't expose that
 * pipeline, so this computes equivalent synthetic biome ids for oil generation rolls.
 */
public final class BCEnergyWorldGen {
   public static final Identifier OIL_OCEAN = Identifier.parse("buildcraftenergy:oil_ocean");
   public static final Identifier OIL_DESERT = Identifier.parse("buildcraftenergy:oil_desert");

   private BCEnergyWorldGen() {
   }

   public static void init() {
      ensureLegacyOilBiomeDefaults();
   }

   public static Identifier effectiveBiomeId(ServerLevel level, int x, int z, Holder<Biome> biome, Identifier fallback) {
      String path = fallback.getPath();
      if (BCEnergyConfig.enableOilOceanBiome.get() && path.contains("ocean")) {
         if (sampleNoise(level.getSeed(), x, z, 0.0005) >= 0.9) {
            return OIL_OCEAN;
         }
      }
      if (BCEnergyConfig.enableOilDesertBiome.get() && isDesertLike(path)) {
         if (sampleNoise(level.getSeed() ^ 0x5EED5EEDL, x, z, 0.001) >= 0.7) {
            return OIL_DESERT;
         }
      }
      return fallback;
   }

   private static boolean isDesertLike(String path) {
      return path.contains("desert") || path.contains("badlands");
   }

   private static double sampleNoise(long seed, int x, int z, double scale) {
      long sx = (long)Math.floor(x * scale * 8192.0);
      long sz = (long)Math.floor(z * scale * 8192.0);
      long h = seed;
      h ^= sx * 0x9E3779B97F4A7C15L;
      h ^= sz * 0xC2B2AE3D27D4EB4FL;
      h ^= (h >>> 33);
      h *= 0xff51afd7ed558ccdL;
      h ^= (h >>> 33);
      h *= 0xc4ceb9fe1a85ec53L;
      h ^= (h >>> 33);
      return (h & 0x1fffffffffffffL) / (double)0x1fffffffffffffL;
   }

   private static void ensureLegacyOilBiomeDefaults() {
      if (BCEnergyConfig.enableOilOceanBiome.get()) {
         appendIfMissing(BCEnergyConfig.surfaceDepositBiomes, OIL_OCEAN.toString());
         appendIfMissing(BCEnergyConfig.forceExcessiveOilBiomes, OIL_OCEAN.toString());
      }
      if (BCEnergyConfig.enableOilDesertBiome.get()) {
         appendIfMissing(BCEnergyConfig.surfaceDepositBiomes, OIL_DESERT.toString());
         appendIfMissing(BCEnergyConfig.forceExcessiveOilBiomes, OIL_DESERT.toString());
      }
   }

   private static void appendIfMissing(buildcraft.core.BCCoreConfig.StringListValue value, String entry) {
      List<String> current = new ArrayList<>(value.get());
      if (!current.contains(entry)) {
         current.add(entry);
         value.set(current);
      }
   }
}
