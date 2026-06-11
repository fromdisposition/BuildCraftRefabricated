package buildcraft.energy.worldgen.structure;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.worldgen.core.BCEnergyBiomeTags;
import com.mojang.serialization.Codec;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class OilStructureSpawnConditions {
   public enum Tier {
      NORMAL,
      RICH,
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
      if (isDimensionExcluded(context)) {
         return false;
      }

      int sampleX = context.chunkPos().getMiddleBlockX();
      int sampleZ = context.chunkPos().getMiddleBlockZ();
      Holder<Biome> biome = context.biomeSource()
         .getNoiseBiome(QuartPos.fromBlock(sampleX), QuartPos.fromBlock(0), QuartPos.fromBlock(sampleZ), context.randomState().sampler());
      return biomeMatchesTier(biome, tier);
   }

   private static boolean biomeMatchesTier(Holder<Biome> biome, Tier tier) {
      if (biome.is(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME)) {
         return false;
      }
      return switch (tier) {
         case NORMAL -> biome.is(BCEnergyBiomeTags.OIL_SPAWN_NORMAL);
         case RICH -> biome.is(BCEnergyBiomeTags.OIL_SPAWN_RICH);
         case PATCH_DESERT -> biome.is(BCEnergyBiomeTags.OIL_PATCH_DESERT);
         case PATCH_OCEAN -> biome.is(BCEnergyBiomeTags.OIL_PATCH_OCEAN);
      };
   }

   private static boolean isDimensionExcluded(Structure.GenerationContext context) {
      if (!(context.heightAccessor() instanceof WorldGenLevel worldGenLevel)) {
         return false;
      }
      Level level = worldGenLevel.getLevel();
      Identifier dimensionId = level.dimension().identifier();
      Set<Identifier> excluded = BCEnergyConfig.getExcludedDimensions();
      boolean inList = excluded.contains(dimensionId);
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }
}
