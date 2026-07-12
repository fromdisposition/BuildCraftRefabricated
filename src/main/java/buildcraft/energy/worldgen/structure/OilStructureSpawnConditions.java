package buildcraft.energy.worldgen.structure;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.worldgen.core.BCEnergyBiomeTags;
import buildcraft.energy.worldgen.core.WorldgenDimensionFilters;
import buildcraft.energy.worldgen.core.WorldgenSpawnContext;
import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Config/dimension gates shared by the oil structures. Biome selection is plain vanilla — each
 * structure's {@code biomes} tag ({@code has_structure/...}) — and field rarity is plain
 * structure_set spacing, so there is no custom region logic here.
 */
public final class OilStructureSpawnConditions {
   public enum Tier {
      NORMAL,
      FIELD_DESERT,
      FIELD_OCEAN;

      public static final Codec<Tier> CODEC = Codec.STRING.xmap(
         value -> Tier.valueOf(value.toUpperCase(Locale.ROOT)),
         value -> value.name().toLowerCase(Locale.ROOT)
      );
   }

   private OilStructureSpawnConditions() {
   }

   /** Salt for the frequency roll; +tier ordinal so overlapping grids roll independently. */
   private static final int FREQUENCY_SALT = 0x6F696C;

   public static boolean canSpawn(Tier tier, Structure.GenerationContext context) {
      if (WorldgenSpawnContext.isChunkDecoration(context)
         && (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get() || !tierEnabled(tier))) {
         return false;
      }
      if (context.chunkGenerator() instanceof FlatLevelSource) {
         return false;
      }
      if (WorldgenDimensionFilters.isDimensionExcluded(context)) {
         return false;
      }
      if (!passesFrequencyRoll(tier, context)) {
         return false;
      }

      // Normal wells stay out of the oceans unless ocean oil is enabled (their biome tag spans the
      // whole overworld); fields are already biome-scoped by their has_structure tags.
      if (tier == Tier.NORMAL && !BCEnergyConfig.oilOceanFields.get()) {
         int sampleX = context.chunkPos().getMiddleBlockX();
         int sampleZ = context.chunkPos().getMiddleBlockZ();
         Holder<Biome> biome = context.biomeSource()
            .getNoiseBiome(QuartPos.fromBlock(sampleX), QuartPos.fromBlock(0), QuartPos.fromBlock(sampleZ), context.randomState().sampler());
         if (biome.is(BCEnergyBiomeTags.OIL_OCEAN)) {
            return false;
         }
      }

      return true;
   }

   private static boolean tierEnabled(Tier tier) {
      return switch (tier) {
         case NORMAL -> BCEnergyConfig.oilWells.get();
         case FIELD_DESERT -> BCEnergyConfig.oilDesertFields.get();
         case FIELD_OCEAN -> BCEnergyConfig.oilOceanFields.get();
      };
   }

   /**
    * Mirrors vanilla StructurePlacement's DEFAULT frequency_reduction_method: a roll seeded from
    * world seed + chunk pos, so {@code /locate} and real generation always agree on the outcome.
    */
   private static boolean passesFrequencyRoll(Tier tier, Structure.GenerationContext context) {
      int raw = switch (tier) {
         case NORMAL -> BCEnergyConfig.oilWellFrequencyPercent.get();
         case FIELD_DESERT -> BCEnergyConfig.oilDesertFieldFrequencyPercent.get();
         case FIELD_OCEAN -> BCEnergyConfig.oilOceanFieldFrequencyPercent.get();
      };
      int percent = Math.max(0, Math.min(100, raw));
      if (percent >= 100) {
         return true;
      }
      if (percent <= 0) {
         return false;
      }

      // getMinBlockX/Z >> 4 = chunk coords; the x/z accessors diverge between mapping sets.
      WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
      random.setLargeFeatureWithSalt(
         context.seed(), FREQUENCY_SALT + tier.ordinal(), context.chunkPos().getMinBlockX() >> 4, context.chunkPos().getMinBlockZ() >> 4
      );
      return random.nextFloat() < percent / 100.0F;
   }
}
