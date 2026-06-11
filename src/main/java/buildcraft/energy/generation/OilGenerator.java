/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.lib.misc.RegistryKeyUtil;
import java.util.Locale;
import javax.annotation.Nullable;
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

public class OilGenerator {
   public static final int CHUNK_CENTER_OFFSET = 8;
   private static final int OWNER_SCAN_RADIUS = 3;
   private static final int MAX_DEPOSIT_HORIZONTAL_REACH = 60;
   private static final long OIL_PLACEMENT_SALT = -0x4F696C4465706F73L;
   private static final int BIOME_SAMPLE_Y = 64;
   private static final double OIL_BIOME_BONUS = 3.0;
   private static final double DEFAULT_BONUS = 1.0;
   private static final double EXCESSIVE_BONUS = 30.0;
   private static final double MAX_BONUS_MULTIPLIER = OIL_BIOME_BONUS * EXCESSIVE_BONUS;
   public static final boolean DEBUG_OILGEN_BASIC = BCDebugging.shouldDebugLog("energy.oilgen");
   public static final boolean DEBUG_OILGEN_ALL = BCDebugging.shouldDebugComplex("energy.oilgen");
   private static final Object SEED_CACHE_LOCK = new Object();
   private static volatile long cachedSeed = Long.MIN_VALUE;
   private static volatile long cachedXScale = 1L;
   private static volatile long cachedZScale = 1L;

   private OilGenerator() {
   }

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

      return BiomeRoll.create(sample).pickType(new PreRoll(random.nextDouble(), random.nextDouble(), random.nextDouble())) != null;
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return BCEnergyConfig.getRichSurfaceDepositBiomes().contains(biomeId) || BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId);
   }

   private static ChunkSample sampleAt(Level level, int x, int z, RandomSource random) {
      Holder<Biome> biome = level.getBiome(new BlockPos(x, BIOME_SAMPLE_Y, z));
      Identifier biomeId = Identifier.parse(biome.getRegisteredName());
      return new ChunkSample(random, x, z, biome, biomeId);
   }

   private static ChunkSample sampleAt(WorldGenLevel level, int x, int z, RandomSource random) {
      Holder<Biome> biome = noiseBiomeAt(level, x, BIOME_SAMPLE_Y, z);
      Identifier biomeId = Identifier.parse(biome.getRegisteredName());
      return new ChunkSample(random, x, z, biome, biomeId);
   }

   /**
    * Samples the biome straight from the chunk generator's {@link net.minecraft.world.level.biome.BiomeSource}. Unlike
    * {@link WorldGenLevel#getBiome} this never routes through {@code WorldGenRegion.getChunk}, so it is safe to query for
    * owner chunks several chunks away from the chunk currently being decorated (those neighbours are not yet available
    * during the FEATURES step and would otherwise throw "Requested chunk unavailable during world generation").
    */
   private static Holder<Biome> noiseBiomeAt(WorldGenLevel level, int x, int y, int z) {
      ServerLevel server = level.getLevel();
      var chunkSource = server.getChunkSource();
      return chunkSource.getGenerator()
         .getBiomeSource()
         .getNoiseBiome(x >> 2, y >> 2, z >> 2, chunkSource.randomState().sampler());
   }

   public static boolean isOilDesignBiomeAt(Level level, int chunkX, int chunkZ) {
      return isOilDesignBiome(sampleAt(level, chunkCenterBlockX(chunkX), chunkCenterBlockZ(chunkZ), RandomSource.create(0L)).biomeId());
   }

   public static int chunkCenterBlockX(int chunkX) {
      return (chunkX << 4) + CHUNK_CENTER_OFFSET;
   }

   public static int chunkCenterBlockZ(int chunkZ) {
      return (chunkZ << 4) + CHUNK_CENTER_OFFSET;
   }

   public static boolean placeForChunk(WorldGenLevel level, BlockPos origin, RandomSource random) {
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
            if (!canReadOwnerChunk(level, sliceChunkX, sliceChunkZ, ownerChunkX, ownerChunkZ)) {
               continue;
            }

            if (!ownerMightReachSlice(ownerChunkX, ownerChunkZ, sliceChunkX, sliceChunkZ)) {
               continue;
            }

            RandomSource ownerRandom = createChunkPlacementRandom(level.getLevel(), ownerChunkX, ownerChunkZ);
            PreRoll preRoll = new PreRoll(ownerRandom.nextDouble(), ownerRandom.nextDouble(), ownerRandom.nextDouble());
            if (!passesNumericPrefilter(preRoll)) {
               continue;
            }

            boolean log = dcx == 0 && dcz == 0;
            OilDepositPlan plan = tryPlanDeposit(level, ownerChunkX, ownerChunkZ, sliceChunkX, sliceChunkZ, ownerRandom, preRoll, log);
            if (plan != null) {
               placed |= OilSliceRenderer.renderSlice(level, plan, sliceChunkX, sliceChunkZ);
            }
         }
      }

      return placed;
   }

   private static boolean canReadOwnerChunk(WorldGenLevel level, int sliceChunkX, int sliceChunkZ, int ownerChunkX, int ownerChunkZ) {
      if (sliceChunkX == ownerChunkX && sliceChunkZ == ownerChunkZ) {
         return true;
      }

      return level.hasChunk(ownerChunkX, ownerChunkZ);
   }

   private static boolean ownerMightReachSlice(int ownerChunkX, int ownerChunkZ, int sliceChunkX, int sliceChunkZ) {
      int anchorX = chunkCenterBlockX(ownerChunkX);
      int anchorZ = chunkCenterBlockZ(ownerChunkZ);
      int sliceMinX = sliceChunkX << 4;
      int sliceMaxX = sliceMinX + 15;
      int sliceMinZ = sliceChunkZ << 4;
      int sliceMaxZ = sliceMinZ + 15;
      return horizontalRangesOverlap(sliceMinX, sliceMaxX, anchorX - MAX_DEPOSIT_HORIZONTAL_REACH, anchorX + MAX_DEPOSIT_HORIZONTAL_REACH)
         && horizontalRangesOverlap(sliceMinZ, sliceMaxZ, anchorZ - MAX_DEPOSIT_HORIZONTAL_REACH, anchorZ + MAX_DEPOSIT_HORIZONTAL_REACH);
   }

   private static boolean horizontalRangesOverlap(int aMin, int aMax, int bMin, int bMax) {
      return aMin <= bMax && bMin <= aMax;
   }

   private static boolean passesNumericPrefilter(PreRoll preRoll) {
      double maxRate = BCEnergyConfig.oilWellGenerationRate.get() * MAX_BONUS_MULTIPLIER;
      return preRoll.largeRoll <= BCEnergyConfig.largeOilGenProb.get() * maxRate
         || preRoll.mediumRoll <= BCEnergyConfig.mediumOilGenProb.get() * maxRate
         || preRoll.smallRoll <= BCEnergyConfig.smallOilGenProb.get() * maxRate;
   }

   @Nullable
   private static OilDepositPlan tryPlanDeposit(
      WorldGenLevel level,
      int ownerChunkX,
      int ownerChunkZ,
      int sliceChunkX,
      int sliceChunkZ,
      RandomSource ownerRandom,
      PreRoll preRoll,
      boolean log
   ) {
      int anchorX = chunkCenterBlockX(ownerChunkX);
      int anchorZ = chunkCenterBlockZ(ownerChunkZ);
      DepositRoll roll = rollDeposit(level, anchorX, anchorZ, ownerRandom, preRoll, log);
      if (roll == null || !roll.intersectsSlice(sliceChunkX, sliceChunkZ, ownerChunkX, ownerChunkZ)) {
         return null;
      }

      BlockPos patternStart = roll.surfaceCenter.offset(-roll.tendrilRadius, 0, -roll.tendrilRadius);
      boolean[][] pattern = buildTendrilPattern(roll.lakeRadius, roll.tendrilRadius, ownerRandom);
      int tendrilDepth = ownerRandom.nextDouble() < 0.5 ? 1 : 2;

      Integer wellY = null;
      Integer wellRadius = null;
      int spoutSegmentHeight = 0;
      int spoutRadius = 0;
      boolean hasSpring = false;
      BlockPos springPos = null;

      if (roll.type != OilDepositPlan.DepositType.LAKE) {
         wellY = bcWellY(level, ownerRandom);
         wellRadius = roll.type == OilDepositPlan.DepositType.LARGE ? 8 + ownerRandom.nextInt(9) : 4 + ownerRandom.nextInt(4);
         SpoutKind kind = roll.type == OilDepositPlan.DepositType.LARGE ? SpoutKind.LARGE : SpoutKind.FINITE;
         spoutRadius = BCEnergyConfig.enableOilSpouts.get() ? kind.radius : 0;
         spoutSegmentHeight = BCEnergyConfig.enableOilSpouts.get() ? kind.pickSegmentHeight(ownerRandom) : 0;
         hasSpring = roll.type == OilDepositPlan.DepositType.LARGE && BCEnergyConfig.spawnOilSprings.get();
         if (hasSpring) {
            springPos = new BlockPos(anchorX, level.getMinY(), anchorZ);
         }
      }

      int sourceCount = estimatePlanSourceCount(level, pattern, tendrilDepth, wellY, wellRadius, spoutSegmentHeight, spoutRadius, hasSpring);

      return new OilDepositPlan(
         ownerChunkX,
         ownerChunkZ,
         anchorX,
         anchorZ,
         roll.type,
         roll.lakeRadius,
         roll.tendrilRadius,
         tendrilDepth,
         patternStart,
         pattern,
         roll.surfaceCenter.getY(),
         wellY,
         wellRadius,
         spoutSegmentHeight,
         spoutRadius,
         hasSpring,
         springPos,
         sourceCount
      );
   }

   private static int estimatePlanSourceCount(
      WorldGenLevel level,
      boolean[][] pattern,
      int tendrilDepth,
      @Nullable Integer wellY,
      @Nullable Integer wellRadius,
      int spoutSegmentHeight,
      int spoutRadius,
      boolean hasSpring
   ) {
      int total = countPatternCells(pattern) * tendrilDepth;
      if (wellRadius != null) {
         total += estimateSphereBlocks(wellRadius);
         total += estimateSpoutBlocks(spoutSegmentHeight, spoutRadius);
         if (hasSpring && wellY != null) {
            int tubeStart = level.getMinY() + 2;
            total += estimateTubeBlocks(Math.max(0, wellY - tubeStart), spoutRadius);
         }
      }

      return total;
   }

   private static int estimateSpoutBlocks(int segmentHeight, int spoutRadius) {
      int estimate = 0;

      for (int r = spoutRadius; r >= 0; r--) {
         int side = 2 * r + 1;
         estimate += side * side * segmentHeight;
      }

      int stemSide = 2 * spoutRadius + 1;
      estimate += stemSide * stemSide * 16;
      return estimate;
   }

   private static int countPatternCells(boolean[][] pattern) {
      int count = 0;

      for (boolean[] row : pattern) {
         for (boolean cell : row) {
            if (cell) {
               count++;
            }
         }
      }

      return count;
   }

   private static boolean isDimensionExcluded(ResourceKey<Level> dimKey) {
      boolean inList = BCEnergyConfig.getExcludedDimensions().contains(RegistryKeyUtil.id(dimKey));
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }

   private static boolean isBiomeExcluded(Identifier biomeId) {
      boolean isExcludedBiome = BCEnergyConfig.getExcludedBiomes().contains(biomeId);
      boolean biomeBlacklisted = BCEnergyConfig.biomeListMode.get() == BCEnergyConfig.ListMode.BLACKLIST;
      return isExcludedBiome == biomeBlacklisted;
   }

   @Nullable
   private static DepositRoll rollDeposit(WorldGenLevel level, int x, int z, RandomSource random, PreRoll preRoll, boolean log) {
      int cx = x >> 4;
      int cz = z >> 4;
      ChunkSample sample = sampleAt(level, x, z, random);
      if (isBiomeExcluded(sample.biomeId())) {
         if (DEBUG_OILGEN_BASIC && log) {
            BCLog.logger.info("[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because the biome (" + sample.biomeId() + ") is excluded!");
         }

         return null;
      }

      if (sample.biome().is(BiomeTags.IS_END) && (Math.abs(x) < 1200 || Math.abs(z) < 1200)) {
         return null;
      }

      BiomeRoll roll = BiomeRoll.create(sample);
      GenType type = roll.pickType(preRoll);
      if (type == null) {
         logNoRoll(cx, cz, log);
         return null;
      }

      if (DEBUG_OILGEN_BASIC && log) {
         BCLog.logger
            .info(
               "[energy.oilgen] Generating an oil well ("
                  + type.name().toLowerCase(Locale.ROOT)
                  + ") in chunk "
                  + cx
                  + ", "
                  + cz
                  + " at "
                  + x
                  + ", "
                  + z
            );
      }

      return rollOverworldDeposit(level, sample, type);
   }

   private static void logNoRoll(int cx, int cz, boolean log) {
      if (DEBUG_OILGEN_ALL && log) {
         BCLog.logger
            .info("[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because none of the random numbers were above the thresholds.");
      }
   }

   private static DepositRoll rollOverworldDeposit(WorldGenLevel level, ChunkSample sample, GenType type) {
      BlockPos surfaceCenter = OilGenStructure.findTerrainUpper(level, sample.x(), sample.z());
      RandomSource rand = sample.rand();
      int lakeRadius;
      int tendrilRadius;
      if (type == GenType.LARGE) {
         lakeRadius = 4;
         tendrilRadius = 25 + rand.nextInt(20);
      } else if (type == GenType.LAKE) {
         lakeRadius = 6;
         tendrilRadius = 25 + rand.nextInt(20);
      } else {
         lakeRadius = 2;
         tendrilRadius = 5 + rand.nextInt(10);
      }

      return new DepositRoll(toDepositType(type), surfaceCenter, lakeRadius, tendrilRadius);
   }

   /** BC 8.0: well center is measured up from bedrock, not sea level. */
   private static int bcWellY(WorldGenLevel level, RandomSource rand) {
      return level.getMinY() + 25 + rand.nextInt(10);
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

   public static OilGenStructure.Spring createSpring(BlockPos at) {
      return new OilGenStructure.Spring(at);
   }

   private static int estimateSphereBlocks(int radius) {
      return (int)Math.round(4.0 / 3.0 * Math.PI * radius * radius * radius);
   }

   private static int estimateTubeBlocks(int length, int radius) {
      return (int)Math.round(Math.PI * radius * radius * (length + 1));
   }

   private static boolean[][] buildTendrilPattern(int lakeRadius, int radius, RandomSource rand) {
      int diameter = radius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int px = radius;
      int pz = radius;

      for (int dx = -lakeRadius; dx <= lakeRadius; dx++) {
         for (int dz = -lakeRadius; dz <= lakeRadius; dz++) {
            pattern[px + dx][pz + dz] = dx * dx + dz * dz <= lakeRadius * lakeRadius;
         }
      }

      for (int w = 1; w < radius; w++) {
         float proba = (float)(radius - w + 4) / (radius + 4);
         fillPatternIfProba(rand, proba, px, pz + w, pattern);
         fillPatternIfProba(rand, proba, px, pz - w, pattern);
         fillPatternIfProba(rand, proba, px + w, pz, pattern);
         fillPatternIfProba(rand, proba, px - w, pz, pattern);

         for (int i = 1; i <= w; i++) {
            fillPatternIfProba(rand, proba, px + i, pz + w, pattern);
            fillPatternIfProba(rand, proba, px + i, pz - w, pattern);
            fillPatternIfProba(rand, proba, px + w, pz + i, pattern);
            fillPatternIfProba(rand, proba, px - w, pz + i, pattern);
            fillPatternIfProba(rand, proba, px - i, pz + w, pattern);
            fillPatternIfProba(rand, proba, px - i, pz - w, pattern);
            fillPatternIfProba(rand, proba, px + w, pz - i, pattern);
            fillPatternIfProba(rand, proba, px - w, pz - i, pattern);
         }
      }

      return pattern;
   }

   private static void fillPatternIfProba(RandomSource rand, float proba, int x, int z, boolean[][] pattern) {
      if (rand.nextFloat() <= proba) {
         pattern[x][z] = isSet(pattern, x, z - 1) | isSet(pattern, x, z + 1) | isSet(pattern, x - 1, z) | isSet(pattern, x + 1, z);
      }
   }

   private static boolean isSet(boolean[][] pattern, int x, int z) {
      if (x < 0 || x >= pattern.length) {
         return false;
      }

      return z >= 0 && z < pattern[x].length && pattern[x][z];
   }

   private static OilDepositPlan.DepositType toDepositType(GenType type) {
      return switch (type) {
         case LARGE -> OilDepositPlan.DepositType.LARGE;
         case MEDIUM -> OilDepositPlan.DepositType.MEDIUM;
         case LAKE -> OilDepositPlan.DepositType.LAKE;
      };
   }

   private record DepositRoll(OilDepositPlan.DepositType type, BlockPos surfaceCenter, int lakeRadius, int tendrilRadius) {
      boolean intersectsSlice(int sliceChunkX, int sliceChunkZ, int ownerChunkX, int ownerChunkZ) {
         int anchorX = surfaceCenter.getX();
         int anchorZ = surfaceCenter.getZ();
         int sliceMinX = sliceChunkX << 4;
         int sliceMaxX = sliceMinX + 15;
         int sliceMinZ = sliceChunkZ << 4;
         int sliceMaxZ = sliceMinZ + 15;
         int tendrilMinX = anchorX - tendrilRadius;
         int tendrilMaxX = anchorX + tendrilRadius;
         int tendrilMinZ = anchorZ - tendrilRadius;
         int tendrilMaxZ = anchorZ + tendrilRadius;

         if (horizontalRangesOverlap(sliceMinX, sliceMaxX, tendrilMinX, tendrilMaxX)
            && horizontalRangesOverlap(sliceMinZ, sliceMaxZ, tendrilMinZ, tendrilMaxZ)) {
            return true;
         }

         if (type != OilDepositPlan.DepositType.LAKE) {
            int maxWellRadius = type == OilDepositPlan.DepositType.LARGE ? 16 : 7;
            int wellMinX = anchorX - maxWellRadius;
            int wellMaxX = anchorX + maxWellRadius;
            int wellMinZ = anchorZ - maxWellRadius;
            int wellMaxZ = anchorZ + maxWellRadius;
            if (horizontalRangesOverlap(sliceMinX, sliceMaxX, wellMinX, wellMaxX)
               && horizontalRangesOverlap(sliceMinZ, sliceMaxZ, wellMinZ, wellMaxZ)) {
               return true;
            }
         }

         return type != OilDepositPlan.DepositType.LAKE && sliceChunkX == ownerChunkX && sliceChunkZ == ownerChunkZ;
      }
   }

   private record ChunkSample(RandomSource rand, int x, int z, Holder<Biome> biome, Identifier biomeId) {
   }

   private record PreRoll(double largeRoll, double mediumRoll, double smallRoll) {
   }

   private record BiomeRoll(boolean oilBiome, boolean excessiveBiome, double globalMultiplier) {
      static BiomeRoll create(ChunkSample sample) {
         boolean oilBiome = BCEnergyConfig.getSurfaceDepositBiomes().contains(sample.biomeId());
         boolean excessiveBiome = BCEnergyConfig.getForceExcessiveOilBiomes().contains(sample.biomeId());
         return new BiomeRoll(oilBiome, excessiveBiome, BCEnergyConfig.oilWellGenerationRate.get());
      }

      @Nullable
      GenType pickType(PreRoll preRoll) {
         double effectiveRate = this.spawnBonus() * this.globalMultiplier;
         if (preRoll.largeRoll <= BCEnergyConfig.largeOilGenProb.get() * effectiveRate) {
            return GenType.LARGE;
         }

         if (preRoll.mediumRoll <= BCEnergyConfig.mediumOilGenProb.get() * effectiveRate) {
            return GenType.MEDIUM;
         }

         if (this.oilBiome && preRoll.smallRoll <= BCEnergyConfig.smallOilGenProb.get() * effectiveRate) {
            return GenType.LAKE;
         }

         return null;
      }

      private double spawnBonus() {
         double bonus = this.oilBiome ? OIL_BIOME_BONUS : DEFAULT_BONUS;
         if (this.excessiveBiome) {
            bonus *= EXCESSIVE_BONUS;
         }

         return bonus;
      }
   }

   private enum GenType {
      LARGE,
      MEDIUM,
      LAKE
   }

   private enum SpoutKind {
      FINITE(0) {
         @Override
         int minSegmentHeight() {
            return BCEnergyConfig.finiteSpoutMinHeight.get();
         }

         @Override
         int maxSegmentHeight() {
            return BCEnergyConfig.finiteSpoutMaxHeight.get();
         }
      },
      LARGE(1) {
         @Override
         int minSegmentHeight() {
            return BCEnergyConfig.largeSpoutMinHeight.get();
         }

         @Override
         int maxSegmentHeight() {
            return BCEnergyConfig.largeSpoutMaxHeight.get();
         }
      };

      final int radius;

      SpoutKind(int radius) {
         this.radius = radius;
      }

      abstract int minSegmentHeight();

      abstract int maxSegmentHeight();

      int pickSegmentHeight(RandomSource rand) {
         int min = this.minSegmentHeight();
         int max = this.maxSegmentHeight();
         if (max <= min) {
            return min;
         }

         return min + rand.nextInt(max - min);
      }
   }
}
