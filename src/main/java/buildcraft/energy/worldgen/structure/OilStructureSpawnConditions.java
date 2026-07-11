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

   public static boolean canSpawn(Tier tier, Structure.GenerationContext context) {
      if (WorldgenSpawnContext.isChunkDecoration(context)
         && (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get())) {
         return false;
      }
      if (context.chunkGenerator() instanceof FlatLevelSource) {
         return false;
      }
      if (WorldgenSpawnContext.isChunkDecoration(context) && tier == Tier.FIELD_OCEAN && !BCEnergyConfig.enableOilOnWater.get()) {
         return false;
      }
      if (WorldgenDimensionFilters.isDimensionExcluded(context)) {
         return false;
      }

      // Normal wells stay out of the oceans unless oil-on-water is enabled (their biome tag spans the
      // whole overworld); fields are already biome-scoped by their has_structure tags.
      if (tier == Tier.NORMAL && !BCEnergyConfig.enableOilOnWater.get()) {
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
}
