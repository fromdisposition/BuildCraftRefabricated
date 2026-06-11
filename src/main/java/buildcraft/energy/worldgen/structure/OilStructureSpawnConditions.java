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

   /**
    * True when this chunk lies in a rich desert or ocean patch sector (same roll as structure spawn).
    * Used for the Fine Riches advancement — not every desert/ocean biome, only contiguous patch sectors.
    */
   public static boolean isRichOilSlice(Holder<Biome> biome, ChunkPos chunkPos) {
      if (biome.is(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME)) {
         return false;
      }

      boolean desert = biome.is(BCEnergyBiomeTags.OIL_PATCH_DESERT);
      boolean ocean = biome.is(BCEnergyBiomeTags.OIL_PATCH_OCEAN);
      if (!desert && !ocean) {
         return false;
      }

      int roll = sectorRoll(chunkPos);
      int desertRichCutoff = clampPercent(BCEnergyConfig.oilDesertRichChancePercent.get());
      int oceanPatchCutoff = clampPercent(BCEnergyConfig.oilOceanPatchChancePercent.get());
      if (desert && roll < desertRichCutoff) {
         return true;
      }
      return ocean && BCEnergyConfig.enableOilOnWater.get() && roll < oceanPatchCutoff;
   }

   private static boolean tierMatches(Structure.GenerationContext context, Holder<Biome> biome, Tier tier) {
      if (biome.is(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME)) {
         return false;
      }

      boolean desert = biome.is(BCEnergyBiomeTags.OIL_PATCH_DESERT);
      boolean ocean = biome.is(BCEnergyBiomeTags.OIL_PATCH_OCEAN);
      int roll = sectorRoll(context.chunkPos());
      int desertRichCutoff = clampPercent(BCEnergyConfig.oilDesertRichChancePercent.get());
      int oceanPatchCutoff = clampPercent(BCEnergyConfig.oilOceanPatchChancePercent.get());

      return switch (tier) {
         case NORMAL -> {
            if (!biome.is(BCEnergyBiomeTags.OIL_SPAWN_NORMAL)) {
               yield false;
            }
            if (ocean) {
               yield BCEnergyConfig.enableOilOnWater.get() && roll >= oceanPatchCutoff;
            }
            if (desert) {
               yield roll >= desertRichCutoff;
            }
            yield true;
         }
         case PATCH_DESERT -> desert && roll < desertRichCutoff;
         case PATCH_OCEAN -> ocean && BCEnergyConfig.enableOilOnWater.get() && roll < oceanPatchCutoff;
      };
   }

   /**
    * Stable sector roll in [0, 99]. All chunks in the same {@link OilStructureDefaults#SLICE_SECTOR_CHUNKS}
    * sector share one roll so rich/patch tiers form contiguous patches (not a per-chunk checkerboard).
    */
   static int sectorRoll(ChunkPos chunkPos) {
      int sector = OilStructureDefaults.SLICE_SECTOR_CHUNKS;
      int sectorX = Math.floorDiv(chunkPos.x(), sector);
      int sectorZ = Math.floorDiv(chunkPos.z(), sector);
      long hash = sectorX * 341873128713L + sectorZ * 1327217883L + OilStructureDefaults.SLICE_ROLL_SALT;
      return Math.floorMod(hash, 100);
   }

   private static int clampPercent(int value) {
      return Math.max(0, Math.min(100, value));
   }

}
