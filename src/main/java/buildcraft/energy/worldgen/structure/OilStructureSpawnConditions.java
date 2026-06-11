package buildcraft.energy.worldgen.structure;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.worldgen.core.BCEnergyBiomeTags;
import buildcraft.energy.worldgen.core.OilStructureDefaults;
import buildcraft.energy.worldgen.core.WorldgenDimensionFilters;
import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class OilStructureSpawnConditions {
   public enum Tier {
      NORMAL,
      PATCH_DESERT,
      PATCH_OCEAN;

      public static final Codec<Tier> CODEC = Codec.STRING.xmap(
         value -> Tier.valueOf(value.toUpperCase(Locale.ROOT)),
         value -> value.name().toLowerCase(Locale.ROOT)
      );
   }

   private OilStructureSpawnConditions() {
   }

   public static boolean canSpawn(Tier tier, Structure.GenerationContext context) {
      if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get()) {
         return false;
      }
      if (context.chunkGenerator() instanceof FlatLevelSource) {
         return false;
      }
      if (tier == Tier.PATCH_OCEAN && !BCEnergyConfig.enableOilOnWater.get()) {
         return false;
      }
      if (WorldgenDimensionFilters.isDimensionExcluded(context)) {
         return false;
      }

      int sampleX = context.chunkPos().getMiddleBlockX();
      int sampleZ = context.chunkPos().getMiddleBlockZ();
      Holder<Biome> biome = context.biomeSource()
         .getNoiseBiome(QuartPos.fromBlock(sampleX), QuartPos.fromBlock(0), QuartPos.fromBlock(sampleZ), context.randomState().sampler());
      return tierMatches(context, biome, tier);
   }

   private static boolean tierMatches(Structure.GenerationContext context, Holder<Biome> biome, Tier tier) {
      if (biome.is(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME)) {
         return false;
      }

      boolean desert = biome.is(BCEnergyBiomeTags.OIL_PATCH_DESERT);
      boolean ocean = biome.is(BCEnergyBiomeTags.OIL_PATCH_OCEAN);
      int roll = chunkRoll(context.chunkPos());
      int desertRichCutoff = clampPercent(BCEnergyConfig.oilDesertRichChancePercent.get());
      int oceanPatchCutoff = clampPercent(BCEnergyConfig.oilOceanPatchChancePercent.get());

      return switch (tier) {
         case NORMAL -> {
            if (ocean) {
               yield false;
            }
            if (desert) {
               yield roll >= desertRichCutoff;
            }
            yield biome.is(BCEnergyBiomeTags.OIL_SPAWN_NORMAL);
         }
         case PATCH_DESERT -> desert && roll < desertRichCutoff;
         case PATCH_OCEAN -> ocean && roll < oceanPatchCutoff;
      };
   }

   /** Stable per-chunk roll in [0, 99] for tier slicing (independent of structure-set salt). */
   static int chunkRoll(ChunkPos chunkPos) {
      long hash = chunkPos.getMiddleBlockX() * 341873128713L
         + chunkPos.getMiddleBlockZ() * 1327217883L
         + OilStructureDefaults.CHUNK_ROLL_SALT;
      return Math.floorMod(hash, 100);
   }

   private static int clampPercent(int value) {
      return Math.max(0, Math.min(100, value));
   }

}
