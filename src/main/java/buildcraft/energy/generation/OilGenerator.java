/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.lib.misc.RegistryKeyUtil;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;

/**
 * BC 8.0 oil worldgen orchestrator.
 *
 * <p>Each overworld chunk runs {@link #placeForChunk} once (via {@link buildcraft.energy.gen.OilDepositFeature}).
 * The decorating chunk scans nearby <em>owner</em> chunks (±{@value #OWNER_SCAN_RADIUS}) for deposits whose geometry
 * overlaps the current slice, rolls deterministically from the owner chunk seed, and renders only blocks inside the
 * decorating 16×16 column.
 *
 * <p>Surface geometry is always a BC fractal tendril ({@link OilTendrilPattern}). {@link OilDepositPlan.DepositType}
 * selects tendril tier and optional underground sphere, geyser tube, and bedrock spring.
 */
public final class OilGenerator {
   public static final int CHUNK_CENTER_OFFSET = 8;

   /** BC 8.0 owner scan radius — large tendrils (r≤44) can span ~5 chunks from the owner. */
   private static final int OWNER_SCAN_RADIUS = 5;
   private static final int MAX_TENDRIL_RADIUS = 44;
   /** BC 8.0: anchor = chunkCenterSample + uniform(0..15) on each axis. */
   private static final int MAX_ANCHOR_OFFSET = 15;
   private static final int MAX_DEPOSIT_HORIZONTAL_REACH = MAX_TENDRIL_RADIUS + CHUNK_CENTER_OFFSET + MAX_ANCHOR_OFFSET;
   /** LARGE owner pass always covers at least the immediate 3×3 chunk neighborhood. */
   private static final int LARGE_OWNER_MIN_CHUNK_RADIUS = 1;

   private static final long OIL_PLACEMENT_SALT = -0x4F696C4465706F73L;
   private static final int BIOME_SAMPLE_Y = 64;
   private static final double OIL_BIOME_BONUS = 3.0;
   private static final double EXCESSIVE_BONUS = 30.0;

   public static final boolean DEBUG_OILGEN_BASIC = BCDebugging.shouldDebugLog("energy.oilgen");
   public static final boolean DEBUG_OILGEN_ALL = BCDebugging.shouldDebugComplex("energy.oilgen");

   private static final Object SEED_CACHE_LOCK = new Object();
   private static volatile long cachedSeed = Long.MIN_VALUE;
   private static volatile long cachedXScale = 1L;
   private static volatile long cachedZScale = 1L;

   private OilGenerator() {
   }

   // --- Public API ---

   public static boolean canGenerateOilIn(ServerLevel level) {
      if (level.getChunkSource().getGenerator() instanceof FlatLevelSource) {
         return false;
      }
      return !isDimensionExcluded(level.dimension());
   }

   public static boolean wouldGenerateOilForOriginChunk(ServerLevel level, int chunkX, int chunkZ) {
      if (!canGenerateOilIn(level)) {
         return false;
      }
      RandomSource random = createChunkPlacementRandom(level, chunkX, chunkZ);
      return predictOilAt(level, chunkCenterBlockX(chunkX), chunkCenterBlockZ(chunkZ), random);
   }

   public static boolean predictOilAt(Level level, int x, int z, RandomSource random) {
      ChunkSample sample = sampleAt(level, x, z, random);
      if (isBiomeExcluded(sample.biomeId())) {
         return false;
      }
      if (sample.biome().is(BiomeTags.IS_END) && (Math.abs(x) < 1200 || Math.abs(z) < 1200)) {
         return false;
      }
      return BiomeRoll.from(sample).pickType(PreRoll.from(random)) != null;
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return BCEnergyConfig.getRichSurfaceDepositBiomes().contains(biomeId)
         || BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId);
   }

   public static boolean isOilDesignBiomeAt(Level level, int chunkX, int chunkZ) {
      return isOilDesignBiome(
         sampleAt(level, chunkCenterBlockX(chunkX), chunkCenterBlockZ(chunkZ), RandomSource.create(0L)).biomeId()
      );
   }

   public static int chunkCenterBlockX(int chunkX) {
      return (chunkX << 4) + CHUNK_CENTER_OFFSET;
   }

   public static int chunkCenterBlockZ(int chunkZ) {
      return (chunkZ << 4) + CHUNK_CENTER_OFFSET;
   }

   /**
    * Entry point for chunk decoration: find overlapping owner deposits and render this slice.
    */
   public static boolean placeForChunk(WorldGenLevel level, BlockPos origin) {
      if (!canGenerateOilIn(level.getLevel())) {
         return false;
      }

      int sliceChunkX = origin.getX() >> 4;
      int sliceChunkZ = origin.getZ() >> 4;
      boolean placed = false;

      for (int dcx = -OWNER_SCAN_RADIUS; dcx <= OWNER_SCAN_RADIUS; dcx++) {
         for (int dcz = -OWNER_SCAN_RADIUS; dcz <= OWNER_SCAN_RADIUS; dcz++) {
            int ownerChunkX = sliceChunkX + dcx;
            int ownerChunkZ = sliceChunkZ + dcz;
            if (!ownerMightReachSlice(ownerChunkX, ownerChunkZ, sliceChunkX, sliceChunkZ)) {
               continue;
            }

            RandomSource ownerRandom = createChunkPlacementRandom(level.getLevel(), ownerChunkX, ownerChunkZ);
            PreRoll preRoll = PreRoll.from(ownerRandom);
            ChunkSample ownerSample = sampleAt(level, chunkCenterBlockX(ownerChunkX), chunkCenterBlockZ(ownerChunkZ), ownerRandom);
            if (!passesNumericPrefilter(preRoll, ownerSample)) {
               continue;
            }

            boolean verbose = ownerChunkX == sliceChunkX && ownerChunkZ == sliceChunkZ;
            placed |= tryPlanAndRender(level, ownerChunkX, ownerChunkZ, sliceChunkX, sliceChunkZ, ownerRandom, preRoll, verbose);
         }
      }

      return placed;
   }

   public static OilGenStructure.Spring createSpring(BlockPos at) {
      return new OilGenStructure.Spring(at);
   }

   // --- Planning + rendering pipeline ---

   private static boolean tryPlanAndRender(
      WorldGenLevel level,
      int ownerChunkX,
      int ownerChunkZ,
      int sliceChunkX,
      int sliceChunkZ,
      RandomSource ownerRandom,
      PreRoll preRoll,
      boolean verbose
   ) {
      DepositRoll roll = rollDeposit(
         level, chunkCenterBlockX(ownerChunkX), chunkCenterBlockZ(ownerChunkZ), ownerRandom, preRoll, verbose
      );
      if (roll == null || !roll.intersectsSlice(sliceChunkX, sliceChunkZ, ownerChunkX, ownerChunkZ)) {
         return false;
      }

      OilDepositPlan plan = OilDepositPlan.build(
         level,
         ownerChunkX,
         ownerChunkZ,
         roll.type(),
         roll.anchor(),
         roll.surfacePlacement(),
         roll.lakeRadius(),
         roll.tendrilRadius(),
         roll.surfacePattern(),
         ownerRandom
      );

      return renderDepositSlices(level, plan, ownerChunkX, ownerChunkZ, sliceChunkX, sliceChunkZ);
   }

   @Nullable
   private static DepositRoll rollDeposit(
      WorldGenLevel level, int chunkCenterX, int chunkCenterZ, RandomSource random, PreRoll preRoll, boolean verbose
   ) {
      int cx = chunkCenterX >> 4;
      int cz = chunkCenterZ >> 4;
      ChunkSample sample = sampleAt(level, chunkCenterX, chunkCenterZ, random);

      if (isBiomeExcluded(sample.biomeId())) {
         if (DEBUG_OILGEN_BASIC && verbose) {
            BCLog.logger.info("[energy.oilgen] Skipping chunk " + cx + ", " + cz + " — excluded biome " + sample.biomeId());
         }
         return null;
      }

      if (sample.biome().is(BiomeTags.IS_END) && (Math.abs(chunkCenterX) < 1200 || Math.abs(chunkCenterZ) < 1200)) {
         return null;
      }

      OilDepositPlan.DepositType type = BiomeRoll.from(sample).pickType(preRoll);
      if (type == null) {
         if (DEBUG_OILGEN_ALL && verbose) {
            BCLog.logger.info("[energy.oilgen] No roll for chunk " + cx + ", " + cz);
         }
         return null;
      }

      if (DEBUG_OILGEN_BASIC && verbose) {
         BCLog.logger.info("[energy.oilgen] Rolling " + type.name().toLowerCase(Locale.ROOT) + " in chunk " + cx + ", " + cz);
      }

      return rollGeometry(level, sample, type);
   }

   @Nullable
   private static DepositRoll rollGeometry(WorldGenLevel level, ChunkSample sample, OilDepositPlan.DepositType type) {
      RandomSource rand = sample.rand();
      int anchorX = sample.x() + rand.nextInt(16);
      int anchorZ = sample.z() + rand.nextInt(16);
      ChunkSample anchorSample = sampleAt(level, anchorX, anchorZ, rand);
      Holder<Biome> biome = anchorSample.biome();
      OilDepositPlan.SurfacePlacement placement = OilGenStructure.surfacePlacementFor(biome);

      BlockPos anchor = OilGenStructure.resolveDepositAnchor(level, biome, anchorX, anchorZ);
      if (anchor == null) {
         return null;
      }

      OilDepositPlan.TendrilSize size = type.tendrilSize();
      int lakeRadius = size.rollLakeRadius();
      int tendrilRadius = size.rollTendrilRadius(rand);

      if (type.requiresFlatLandSite() && !OilGenStructure.isFlatLandDepositSite(level, anchorX, anchorZ, tendrilRadius)) {
         return null;
      }

      boolean[][] pattern = OilTendrilPattern.build(lakeRadius, tendrilRadius, rand);
      BlockPos patternStart = anchor.offset(-tendrilRadius, 0, -tendrilRadius);
      if (!OilGenStructure.passesPatternPreflight(level, pattern, patternStart, type, placement, anchor.getY())) {
         return null;
      }

      return new DepositRoll(type, anchor, placement, lakeRadius, tendrilRadius, pattern);
   }

   /**
    * LARGE deposits: owner chunk paints every overlapping slice (min 3×3, up to full tendril reach).
    * Other tiers and non-owner passes paint only the decorating slice.
    */
   private static boolean renderDepositSlices(
      WorldGenLevel level,
      OilDepositPlan plan,
      int ownerChunkX,
      int ownerChunkZ,
      int sliceChunkX,
      int sliceChunkZ
   ) {
      boolean ownerSlice = sliceChunkX == ownerChunkX && sliceChunkZ == ownerChunkZ;
      if (ownerSlice && plan.type.tendrilSize() == OilDepositPlan.TendrilSize.LARGE) {
         int reach = largeDepositChunkRadius(plan.tendrilRadius);
         boolean placed = false;
         for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
               int sx = ownerChunkX + dx;
               int sz = ownerChunkZ + dz;
               if (plan.intersectsSlice(sx, sz)) {
                  placed |= OilSliceRenderer.renderSlice(level, plan, sx, sz);
               }
            }
         }
         return placed;
      }

      return OilSliceRenderer.renderSlice(level, plan, sliceChunkX, sliceChunkZ);
   }

   // --- Biome sampling & reach ---

   private static ChunkSample sampleAt(Level level, int x, int z, RandomSource random) {
      Holder<Biome> biome = level.getBiome(new BlockPos(x, BIOME_SAMPLE_Y, z));
      return new ChunkSample(random, x, z, biome, Mc26Compat.biomeId(biome));
   }

   private static ChunkSample sampleAt(WorldGenLevel level, int x, int z, RandomSource random) {
      Holder<Biome> biome = noiseBiomeAt(level, x, BIOME_SAMPLE_Y, z);
      return new ChunkSample(random, x, z, biome, Mc26Compat.biomeId(biome));
   }

   private static Holder<Biome> noiseBiomeAt(WorldGenLevel level, int x, int y, int z) {
      ServerLevel server = level.getLevel();
      var chunkSource = server.getChunkSource();
      return chunkSource.getGenerator().getBiomeSource().getNoiseBiome(x >> 2, y >> 2, z >> 2, chunkSource.randomState().sampler());
   }

   private static boolean ownerMightReachSlice(int ownerChunkX, int ownerChunkZ, int sliceChunkX, int sliceChunkZ) {
      int ownerMinX = ownerChunkX << 4;
      int ownerMaxX = ownerMinX + 15;
      int ownerMinZ = ownerChunkZ << 4;
      int ownerMaxZ = ownerMinZ + 15;
      int sliceMinX = sliceChunkX << 4;
      int sliceMaxX = sliceMinX + 15;
      int sliceMinZ = sliceChunkZ << 4;
      int sliceMaxZ = sliceMinZ + 15;
      return OilDepositPlan.rangesOverlap(sliceMinX, sliceMaxX, ownerMinX - MAX_DEPOSIT_HORIZONTAL_REACH, ownerMaxX + MAX_DEPOSIT_HORIZONTAL_REACH)
         && OilDepositPlan.rangesOverlap(sliceMinZ, sliceMaxZ, ownerMinZ - MAX_DEPOSIT_HORIZONTAL_REACH, ownerMaxZ + MAX_DEPOSIT_HORIZONTAL_REACH);
   }

   private static int largeDepositChunkRadius(int tendrilRadius) {
      int blockReach = tendrilRadius + CHUNK_CENTER_OFFSET + MAX_ANCHOR_OFFSET;
      return Math.max(LARGE_OWNER_MIN_CHUNK_RADIUS, chunkRadiusCeil(blockReach));
   }

   private static int chunkRadiusCeil(int blockReach) {
      return (blockReach + 15) >> 4;
   }

   // --- Spawn rolls ---

   private static boolean passesNumericPrefilter(PreRoll preRoll, ChunkSample sample) {
      BiomeRoll roll = BiomeRoll.from(sample);
      double rate = roll.spawnBonus() * roll.globalMultiplier();
      if (preRoll.largeRoll() <= BCEnergyConfig.largeOilGenProb.get() * rate) {
         return true;
      }
      if (preRoll.mediumRoll() <= BCEnergyConfig.mediumOilGenProb.get() * rate) {
         return true;
      }
      return roll.surfaceDepositBiome() && preRoll.smallRoll() <= BCEnergyConfig.smallOilGenProb.get() * rate;
   }

   private static double spawnRateMultiplier() {
      return BCEnergyConfig.oilWellGenerationRate.get();
   }

   // --- Exclusion & RNG ---

   private static boolean isDimensionExcluded(ResourceKey<Level> dimKey) {
      boolean inList = BCEnergyConfig.getExcludedDimensions().contains(RegistryKeyUtil.id(dimKey));
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }

   private static boolean isBiomeExcluded(Identifier biomeId) {
      boolean isExcludedBiome = BCEnergyConfig.getExcludedBiomes().contains(biomeId);
      boolean biomeBlacklisted = BCEnergyConfig.biomeListMode.get() == BCEnergyConfig.ListMode.BLACKLIST;
      return isExcludedBiome == biomeBlacklisted;
   }

   private static long decorationSeedForChunk(long worldSeed, int chunkX, int chunkZ) {
      long xScale = cachedXScale;
      long zScale = cachedZScale;
      if (cachedSeed != worldSeed) {
         synchronized (SEED_CACHE_LOCK) {
            if (cachedSeed != worldSeed) {
               RandomSource seed = RandomSource.create(worldSeed);
               cachedXScale = seed.nextLong() | 1L;
               cachedZScale = seed.nextLong() | 1L;
               cachedSeed = worldSeed;
            }
            xScale = cachedXScale;
            zScale = cachedZScale;
         }
      }
      return (long)(chunkX << 4) * xScale + (long)(chunkZ << 4) * zScale ^ worldSeed;
   }

   private static RandomSource createChunkPlacementRandom(ServerLevel level, int chunkX, int chunkZ) {
      return RandomSource.create(decorationSeedForChunk(level.getSeed(), chunkX, chunkZ) ^ OIL_PLACEMENT_SALT);
   }

   // --- Internal records ---

   private record DepositRoll(
      OilDepositPlan.DepositType type,
      BlockPos anchor,
      OilDepositPlan.SurfacePlacement surfacePlacement,
      int lakeRadius,
      int tendrilRadius,
      boolean[][] surfacePattern
   ) {
      boolean intersectsSlice(int sliceChunkX, int sliceChunkZ, int ownerChunkX, int ownerChunkZ) {
         int ax = anchor.getX();
         int az = anchor.getZ();
         int sliceMinX = sliceChunkX << 4;
         int sliceMaxX = sliceMinX + 15;
         int sliceMinZ = sliceChunkZ << 4;
         int sliceMaxZ = sliceMinZ + 15;

         if (OilDepositPlan.rangesOverlap(sliceMinX, sliceMaxX, ax - tendrilRadius, ax + tendrilRadius)
            && OilDepositPlan.rangesOverlap(sliceMinZ, sliceMaxZ, az - tendrilRadius, az + tendrilRadius)) {
            return true;
         }

         if (type.hasUndergroundPool()) {
            int reach = type.maxWellReach();
            if (OilDepositPlan.rangesOverlap(sliceMinX, sliceMaxX, ax - reach, ax + reach)
               && OilDepositPlan.rangesOverlap(sliceMinZ, sliceMaxZ, az - reach, az + reach)) {
               return true;
            }
         }

         return type.hasSpout() && sliceChunkX == ownerChunkX && sliceChunkZ == ownerChunkZ;
      }
   }

   private record ChunkSample(RandomSource rand, int x, int z, Holder<Biome> biome, Identifier biomeId) {
   }

   private record PreRoll(double largeRoll, double mediumRoll, double smallRoll) {
      static PreRoll from(RandomSource random) {
         return new PreRoll(random.nextDouble(), random.nextDouble(), random.nextDouble());
      }
   }

   private record BiomeRoll(boolean excessiveBiome, boolean ocean, boolean surfaceDepositBiome, double globalMultiplier) {
      static BiomeRoll from(ChunkSample sample) {
         return new BiomeRoll(
            BCEnergyConfig.getForceExcessiveOilBiomes().contains(sample.biomeId()),
            sample.biome().is(BiomeTags.IS_OCEAN),
            BCEnergyConfig.getSurfaceDepositBiomes().contains(sample.biomeId()),
            spawnRateMultiplier()
         );
      }

      OilDepositPlan.@Nullable DepositType pickType(PreRoll preRoll) {
         double rate = spawnBonus() * globalMultiplier;

         if (ocean) {
            if (preRoll.largeRoll() <= BCEnergyConfig.largeOilGenProb.get() * rate) {
               return OilDepositPlan.DepositType.OCEAN_LARGE;
            }
            if (preRoll.mediumRoll() <= BCEnergyConfig.mediumOilGenProb.get() * rate) {
               return OilDepositPlan.DepositType.OCEAN_FOUNTAIN;
            }
            if (surfaceDepositBiome && preRoll.smallRoll() <= BCEnergyConfig.smallOilGenProb.get() * rate) {
               return OilDepositPlan.DepositType.OCEAN_PATCH;
            }
            return null;
         }

         if (preRoll.largeRoll() <= BCEnergyConfig.largeOilGenProb.get() * rate) {
            return OilDepositPlan.DepositType.LAND_LARGE;
         }
         if (preRoll.mediumRoll() <= BCEnergyConfig.mediumOilGenProb.get() * rate) {
            return OilDepositPlan.DepositType.LAND_FOUNTAIN;
         }
         if (surfaceDepositBiome && preRoll.smallRoll() <= BCEnergyConfig.smallOilGenProb.get() * rate) {
            return OilDepositPlan.DepositType.LAND_LAKE;
         }
         return null;
      }

      private double spawnBonus() {
         double bonus = surfaceDepositBiome ? OIL_BIOME_BONUS : 1.0;
         if (excessiveBiome) {
            bonus *= EXCESSIVE_BONUS;
         }
         return bonus;
      }
   }
}
