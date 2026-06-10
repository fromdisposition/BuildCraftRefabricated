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
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class OilGenerator {
   /** Anchor at chunk center; one cardinal neighbor adds 16 blocks (8 + 16 + 7 = 31 → reach 23). */
   public static final int CHUNK_CENTER_OFFSET = 8;
   public static final int MAX_RADIUS_WITH_CARDINAL_NEIGHBORS = 23;
   private static final long OIL_PLACEMENT_SALT = -0x4F696C4465706F73L;
   private static final int BIOME_SAMPLE_Y = 64;
   private static final double OIL_BIOME_BONUS = 3.0;
   private static final double MOUNTAINOUS_BIOME_BONUS = 0.1;
   private static final double DEFAULT_BONUS = 1.0;
   public static final boolean DEBUG_OILGEN_BASIC = BCDebugging.shouldDebugLog("energy.oilgen");
   public static final boolean DEBUG_OILGEN_ALL = BCDebugging.shouldDebugComplex("energy.oilgen");
   private static final double RICH_LAND_LARGE_SPRING_PROB = 6.0E-4;
   private static final double RICH_LAND_MEDIUM_SPRING_PROB = 0.0025;

   private OilGenerator() {
   }

   public static boolean canGenerateOilIn(ServerLevel level) {
      if (level.getChunkSource().getGenerator() instanceof FlatLevelSource) {
         return false;
      }

      return level.dimension() == Level.NETHER && BCEnergyConfig.enableNetherOilGeneration.get() || !isDimensionExcluded(level.dimension());
   }

   /**
    * Lightweight advancement probe: same placement modifiers and roll logic as worldgen, without building geometry.
    */
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

      if (level.dimension() == Level.NETHER) {
         return predictNetherOil(x, z, random);
      }

      return BiomeRoll.create(sample).pickType() != null;
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return BCEnergyConfig.getRichSurfaceDepositBiomes().contains(biomeId) || BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId);
   }

   private static Box chunkBounds(WorldGenLevel level, int chunkX, int chunkZ) {
      int baseX = chunkX << 4;
      int baseZ = chunkZ << 4;
      return new Box(new BlockPos(baseX, level.getMinY(), baseZ), new BlockPos(baseX + 15, level.getMaxY(), baseZ + 15));
   }

   private static ChunkSample sampleAt(Level level, int x, int z, RandomSource random) {
      Holder<Biome> biome = level.getBiome(new BlockPos(x, BIOME_SAMPLE_Y, z));
      Identifier biomeId = Identifier.parse(biome.getRegisteredName());
      return new ChunkSample(random, x, z, biome, biomeId);
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

   /**
    * One roll per chunk at center; writes into this chunk plus cardinal neighbors whose bounds intersect
    * the deposit (at most four extra chunks — no BC-style 11×11 scan).
    */
   public static boolean placeForChunk(WorldGenLevel level, BlockPos origin, RandomSource random) {
      if (!canGenerateOilIn(level.getLevel())) {
         return false;
      }

      int chunkX = origin.getX() >> 4;
      int chunkZ = origin.getZ() >> 4;
      int anchorX = chunkCenterBlockX(chunkX);
      int anchorZ = chunkCenterBlockZ(chunkZ);
      List<OilGenStructure> structures = buildStructures(level.getLevel(), anchorX, anchorZ, random, true);
      if (structures.isEmpty()) {
         return false;
      }

      return generateDepositAcrossChunks(level, structures, chunkX, chunkZ);
   }

   private static boolean generateDepositAcrossChunks(WorldGenLevel level, List<OilGenStructure> structures, int originChunkX, int originChunkZ) {
      OilGenStructure.Spring spring = null;
      boolean placed = false;
      int sourceCount = 0;

      for (OilGenStructure struct : structures) {
         if (struct instanceof OilGenStructure.Spring found) {
            spring = found;
         }
      }

      for (ChunkPos chunkPos : chunksForStructureOverlap(structures, originChunkX, originChunkZ)) {
         Box chunkBounds = chunkBounds(level, chunkPos.x(), chunkPos.z());

         for (OilGenStructure struct : structures) {
            if (struct instanceof OilGenStructure.Spring) {
               continue;
            }

            int placedBefore = struct.getPlacedOilPositions().size();
            struct.generate(level, chunkBounds);
            int placedAfter = struct.getPlacedOilPositions().size();
            if (placedAfter > placedBefore) {
               placed = true;
               sourceCount += placedAfter - placedBefore;
            }
         }
      }

      if (spring != null && chunkBounds(level, originChunkX, originChunkZ).contains(spring.pos)) {
         spring.generate(level, sourceCount);
         placed = true;
      }

      return placed;
   }

   /** Origin chunk plus cardinal neighbors that intersect any structure AABB (≤ 5 chunks total). */
   private static Set<ChunkPos> chunksForStructureOverlap(List<OilGenStructure> structures, int originChunkX, int originChunkZ) {
      LinkedHashSet<ChunkPos> chunks = new LinkedHashSet<>();
      chunks.add(new ChunkPos(originChunkX, originChunkZ));

      for (OilGenStructure struct : structures) {
         if (struct instanceof OilGenStructure.Spring) {
            continue;
         }

         Box box = struct.box;
         int minChunkX = box.min().getX() >> 4;
         int maxChunkX = box.max().getX() >> 4;
         int minChunkZ = box.min().getZ() >> 4;
         int maxChunkZ = box.max().getZ() >> 4;

         for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
               if (cx == originChunkX && cz == originChunkZ) {
                  continue;
               }

               if (Math.abs(cx - originChunkX) + Math.abs(cz - originChunkZ) == 1) {
                  chunks.add(new ChunkPos(cx, cz));
               }
            }
         }
      }

      return chunks;
   }

   private static int clampHorizontalReach(int radius) {
      return Math.min(radius, MAX_RADIUS_WITH_CARDINAL_NEIGHBORS);
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

   private static List<OilGenStructure> buildStructures(Level level, int x, int z, RandomSource random, boolean log) {
      int cx = x >> 4;
      int cz = z >> 4;
      ChunkSample sample = sampleAt(level, x, z, random);
      if (isBiomeExcluded(sample.biomeId())) {
         if (DEBUG_OILGEN_BASIC && log) {
            BCLog.logger.info("[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because the biome (" + sample.biomeId() + ") is excluded!");
         }

         return ImmutableList.of();
      } else if (!sample.biome().is(BiomeTags.IS_END) || Math.abs(x) >= 1200 && Math.abs(z) >= 1200) {
         if (level.dimension() == Level.NETHER) {
            return getNetherStructures(level, log, random, x, z);
         }

         BiomeRoll roll = BiomeRoll.create(sample);
         OilGenerator.GenType type = roll.pickType();
         if (type == null) {
            logNoRoll(cx, cz, log);
            return ImmutableList.of();
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

         return createOverworldStructures(level, sample, roll, type);
      } else {
         return ImmutableList.of();
      }
   }

   private static void logNoRoll(int cx, int cz, boolean log) {
      if (DEBUG_OILGEN_ALL && log) {
         BCLog.logger
            .info("[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because none of the random numbers were above the thresholds.");
      }
   }

   private static int resolveSurfaceY(Level level, Holder<Biome> biome, int x, int z) {
      Heightmap.Types heightmap = biome.is(BiomeTags.IS_OCEAN) ? Heightmap.Types.OCEAN_FLOOR : Heightmap.Types.WORLD_SURFACE;
      if (level instanceof WorldGenLevel wg) {
         return wg.getHeight(heightmap, x, z);
      }

      if (level instanceof LevelReader reader) {
         return reader.getHeight(heightmap, x, z);
      }

      return OilGenStructure.findTerrainUpper(level, x, z).getY() + 1;
   }

   private static List<OilGenStructure> createOverworldStructures(Level level, ChunkSample sample, BiomeRoll roll, GenType type) {
      List<OilGenStructure> structures = new ArrayList<>();
      int surfaceY = resolveSurfaceY(level, sample.biome(), sample.x(), sample.z());
      BlockPos surfaceCenter = new BlockPos(sample.x(), surfaceY, sample.z());

      if (type == OilGenerator.GenType.LAKE) {
         structures.add(createTendril(surfaceCenter, 6, 25 + sample.rand().nextInt(20), sample.rand()));
         return structures;
      }

      addSurfaceStructure(structures, sample.rand(), surfaceCenter, roll, type);

      int wellY = level.getMinY() + 25 + sample.rand().nextInt(10);
      int wellRadius = type == OilGenerator.GenType.LARGE ? 8 + sample.rand().nextInt(9) : 4 + sample.rand().nextInt(4);
      BlockPos wellCenter = new BlockPos(sample.x(), wellY, sample.z());
      structures.add(createSphere(wellCenter, wellRadius));

      addSpoutIfEnabled(structures, sample.rand(), wellCenter, type);
      addSpringIfEnabled(level, structures, sample, wellY, wellRadius, type == OilGenerator.GenType.LARGE);

      return structures;
   }

   private static void addSurfaceStructure(List<OilGenStructure> structures, RandomSource rand, BlockPos center, BiomeRoll roll, GenType type) {
      if (roll.richLand()) {
         structures.add(type == OilGenerator.GenType.LARGE ? createSurfacePoolLarge(center, rand) : createSurfacePoolMedium(center, rand));
      } else if (roll.isOcean()) {
         if (type == OilGenerator.GenType.LARGE) {
            structures.add(createTendril(center, 4, 25 + rand.nextInt(20), rand));
         } else {
            structures.add(createTendril(center, 2, 5 + rand.nextInt(10), rand));
         }
      }
   }

   /** BC 8.0: large geyser segments on {@link GenType#LARGE}, finite column on {@link GenType#MEDIUM}. */
   private static void addSpoutIfEnabled(List<OilGenStructure> structures, RandomSource rand, BlockPos wellCenter, GenType type) {
      if (!BCEnergyConfig.enableOilSpouts.get()) {
         return;
      }

      OilGenerator.SpoutKind kind = type == OilGenerator.GenType.LARGE ? OilGenerator.SpoutKind.LARGE : OilGenerator.SpoutKind.FINITE;
      structures.add(createSpout(wellCenter, kind.pickSegmentHeight(rand), kind.radius));
   }

   private static void addSpringIfEnabled(
      Level level, List<OilGenStructure> structures, ChunkSample sample, int wellY, int wellRadius, boolean hasSpring
   ) {
      if (!hasSpring || !BCEnergyConfig.spawnOilSprings.get()) {
         return;
      }

      int tubeStart = level.getMinY() + 2;
      structures.add(createTube(new BlockPos(sample.x(), tubeStart, sample.z()), wellY - tubeStart, wellRadius, Axis.Y));
      structures.add(createSpring(new BlockPos(sample.x(), level.getMinY(), sample.z())));
   }

   private static boolean predictNetherOil(int x, int z, RandomSource rand) {
      double globalMul = BCEnergyConfig.oilWellGenerationRate.get() * BCEnergyConfig.netherOilGenRateMultiplier.get();
      if (rand.nextDouble() <= BCEnergyConfig.largeOilGenProb.get() * globalMul) {
         return true;
      }

      return rand.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * globalMul;
   }

   private static List<OilGenStructure> getNetherStructures(Level level, boolean log, RandomSource rand, int x, int z) {
      double globalMul = BCEnergyConfig.oilWellGenerationRate.get() * BCEnergyConfig.netherOilGenRateMultiplier.get();
      OilGenerator.GenType type;
      if (rand.nextDouble() <= BCEnergyConfig.largeOilGenProb.get() * globalMul) {
         type = OilGenerator.GenType.LARGE;
      } else {
         if (!(rand.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * globalMul)) {
            if (DEBUG_OILGEN_ALL && log) {
               BCLog.logger.info("[energy.oilgen] Not generating nether oil in chunk at " + x + ", " + z);
            }

            return ImmutableList.of();
         }

         type = OilGenerator.GenType.MEDIUM;
      }

      if (DEBUG_OILGEN_BASIC && log) {
         BCLog.logger.info("[energy.oilgen] Generating nether oil well (" + type.name().toLowerCase(Locale.ROOT) + ") at " + x + ", " + z);
      }

      List<OilGenStructure> structures = new ArrayList<>();
      int surfaceY = OilGenStructure.findTerrainUpper(level, x, z).getY() + 1;
      if (type == OilGenerator.GenType.LARGE) {
         structures.add(createSurfacePoolLarge(new BlockPos(x, surfaceY, z), rand));
      } else {
         structures.add(createSurfacePoolMedium(new BlockPos(x, surfaceY, z), rand));
      }

      int wellY = level.getMinY() + 25 + rand.nextInt(10);
      int wellRadius = type == OilGenerator.GenType.LARGE ? 8 + rand.nextInt(9) : 4 + rand.nextInt(4);
      structures.add(createSphere(new BlockPos(x, wellY, z), wellRadius));
      if (BCEnergyConfig.enableOilSpouts.get()) {
         OilGenerator.SpoutKind kind = type == OilGenerator.GenType.LARGE ? OilGenerator.SpoutKind.LARGE : OilGenerator.SpoutKind.FINITE;
         structures.add(createSpout(new BlockPos(x, wellY, z), kind.pickSegmentHeight(rand), kind.radius));
      }

      int tubeStart = level.getMinY() + 2;
      structures.add(createTube(new BlockPos(x, tubeStart, z), wellY - tubeStart, wellRadius, Axis.Y));
      if (BCEnergyConfig.spawnOilSprings.get()) {
         structures.add(createSpring(new BlockPos(x, level.getMinY(), z)));
      }

      return structures;
   }

   private static long decorationSeedForChunk(long worldSeed, int chunkX, int chunkZ) {
      RandomSource seed = RandomSource.create(worldSeed);
      long xScale = seed.nextLong() | 1L;
      long zScale = seed.nextLong() | 1L;
      return (long)(chunkX << 4) * xScale + (long)(chunkZ << 4) * zScale ^ worldSeed;
   }

   private static RandomSource createChunkPlacementRandom(ServerLevel level, int chunkX, int chunkZ) {
      return RandomSource.create(decorationSeedForChunk(level.getSeed(), chunkX, chunkZ) ^ OIL_PLACEMENT_SALT);
   }

   private static OilGenStructure createSpout(BlockPos start, int height, int radius) {
      return new OilGenStructure.Spout(start, OilGenStructure.ReplaceType.ALWAYS, radius, height);
   }

   public static OilGenStructure createSpring(BlockPos at) {
      return new OilGenStructure.Spring(at);
   }

   public static OilGenStructure createTube(BlockPos center, int length, int radius, Axis axis) {
      return createTube(center, length, radius, axis, OilGenStructure.ReplaceType.ALWAYS);
   }

   public static OilGenStructure createTube(BlockPos center, int length, int radius, Axis axis, OilGenStructure.ReplaceType replaceType) {
      int valForAxis = VecUtil.getValue(center, axis);
      BlockPos min = VecUtil.replaceValue(center.offset(-radius, -radius, -radius), axis, valForAxis);
      BlockPos max = VecUtil.replaceValue(center.offset(radius, radius, radius), axis, valForAxis + length);
      double radiusSq = radius * radius;
      int toReplace = valForAxis;
      Predicate<BlockPos> tester = p -> VecUtil.replaceValue(p, axis, toReplace).distSqr(center) <= radiusSq;
      int blockCount = estimateTubeBlocks(length, radius);
      return new OilGenStructure.GenByPredicate(new Box(min, max), replaceType, tester, blockCount);
   }

   public static OilGenStructure createSphere(BlockPos center, int radius) {
      Box box = new Box(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius));
      double radiusSq = radius * radius + 0.01;
      Predicate<BlockPos> tester = p -> p.distSqr(center) <= radiusSq;
      int blockCount = estimateSphereBlocks(radius);
      return new OilGenStructure.GenByPredicate(box, OilGenStructure.ReplaceType.ALWAYS, tester, blockCount);
   }

   private static int estimateSphereBlocks(int radius) {
      return (int)Math.round(4.0 / 3.0 * Math.PI * radius * radius * radius);
   }

   private static int estimateTubeBlocks(int length, int radius) {
      return (int)Math.round(Math.PI * radius * radius * (length + 1));
   }

   public static OilGenStructure createTendril(BlockPos center, int lakeRadius, int radius, RandomSource rand) {
      int clampedRadius = clampHorizontalReach(radius);
      int clampedLakeRadius = clampHorizontalReach(lakeRadius);
      BlockPos start = center.offset(-clampedRadius, 0, -clampedRadius);
      int diameter = clampedRadius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int px = clampedRadius;
      int pz = clampedRadius;

      for (int dx = -clampedLakeRadius; dx <= clampedLakeRadius; dx++) {
         for (int dz = -clampedLakeRadius; dz <= clampedLakeRadius; dz++) {
            pattern[px + dx][pz + dz] = dx * dx + dz * dz <= clampedLakeRadius * clampedLakeRadius;
         }
      }

      for (int w = 1; w < clampedRadius; w++) {
         float proba = (float)(clampedRadius - w + 4) / (clampedRadius + 4);
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

      int depth = rand.nextDouble() < 0.5 ? 1 : 2;
      return OilGenStructure.PatternTerrainHeight.create(start, pattern, depth, center.getY());
   }

   public static OilGenStructure createSurfacePoolMedium(BlockPos center, RandomSource rand) {
      return createSurfacePool(center, 5 + rand.nextInt(3), rand);
   }

   public static OilGenStructure createSurfacePoolLarge(BlockPos center, RandomSource rand) {
      return createSurfacePool(center, 8 + rand.nextInt(5), rand);
   }

   private static OilGenStructure createSurfacePool(BlockPos center, int baseRadius, RandomSource rand) {
      int cappedBaseRadius = clampHorizontalReach(baseRadius);
      int maxRadius = cappedBaseRadius + 1;
      int diameter = maxRadius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int centerIdx = maxRadius;

      for (int dx = -maxRadius; dx <= maxRadius; dx++) {
         for (int dz = -maxRadius; dz <= maxRadius; dz++) {
            int noise = rand.nextInt(3) - 1;
            int effectiveR = Math.max(0, cappedBaseRadius + noise);
            int distSq = dx * dx + dz * dz;
            pattern[centerIdx + dx][centerIdx + dz] = distSq <= effectiveR * effectiveR;
         }
      }

      int depth = rand.nextDouble() < 0.5 ? 1 : 2;
      BlockPos start = center.offset(-maxRadius, 0, -maxRadius);
      return OilGenStructure.SurfacePool.create(start, pattern, depth, center.getY());
   }

   private static void fillPatternIfProba(RandomSource rand, float proba, int x, int z, boolean[][] pattern) {
      if (rand.nextFloat() <= proba) {
         pattern[x][z] = isSet(pattern, x, z - 1) | isSet(pattern, x, z + 1) | isSet(pattern, x - 1, z) | isSet(pattern, x + 1, z);
      }
   }

   private static boolean isSet(boolean[][] pattern, int x, int z) {
      if (x < 0 || x >= pattern.length) {
         return false;
      } else {
         return z >= 0 && z < pattern[x].length ? pattern[x][z] : false;
      }
   }

   private static int randomIntInclusive(RandomSource rand, int min, int max) {
      if (max < min) {
         int t = max;
         max = min;
         min = t;
      }

      return min + rand.nextInt(max - min + 1);
   }

   private record ChunkSample(RandomSource rand, int x, int z, Holder<Biome> biome, Identifier biomeId) {
   }

   private record BiomeRoll(
      RandomSource rand, boolean richBiome, boolean oilBiome, boolean mountainousBiome, boolean isOcean, boolean richLand, double globalMultiplier
   ) {
      static BiomeRoll create(ChunkSample sample) {
         boolean richBiome = BCEnergyConfig.getRichSurfaceDepositBiomes().contains(sample.biomeId());
         boolean oilBiome = BCEnergyConfig.getSurfaceDepositBiomes().contains(sample.biomeId());
         boolean mountainousBiome = !richBiome && !oilBiome && BCEnergyConfig.getMountainousSurfaceDepositBiomes().contains(sample.biomeId());
         boolean isOcean = sample.biome().is(BiomeTags.IS_OCEAN);
         double multiplier = BCEnergyConfig.oilWellGenerationRate.get();
         if (BCEnergyConfig.getForceExcessiveOilBiomes().contains(sample.biomeId())) {
            multiplier *= 30.0;
         }

         return new BiomeRoll(sample.rand(), richBiome, oilBiome, mountainousBiome, isOcean, richBiome && !isOcean, multiplier);
      }

      @Nullable
      GenType pickType() {
         return this.richLand() ? this.pickRichLandType() : this.pickStandardType();
      }

      @Nullable
      private GenType pickRichLandType() {
         if (this.rand.nextDouble() <= RICH_LAND_LARGE_SPRING_PROB * this.globalMultiplier) {
            return GenType.LARGE;
         } else if (this.rand.nextDouble() <= RICH_LAND_MEDIUM_SPRING_PROB * this.globalMultiplier) {
            return GenType.MEDIUM;
         }

         return null;
      }

      @Nullable
      private GenType pickStandardType() {
         double effectiveRate = this.spawnBonus() * this.globalMultiplier;
         if (this.rand.nextDouble() <= BCEnergyConfig.largeOilGenProb.get() * effectiveRate) {
            return GenType.LARGE;
         } else if (this.rand.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * effectiveRate) {
            return GenType.MEDIUM;
         } else if (this.oilBiome && this.rand.nextDouble() <= BCEnergyConfig.smallOilGenProb.get() * effectiveRate) {
            return GenType.LAKE;
         }

         return null;
      }

      private double spawnBonus() {
         if (this.oilBiome || this.isOcean) {
            return OIL_BIOME_BONUS;
         } else if (this.mountainousBiome) {
            return MOUNTAINOUS_BIOME_BONUS;
         }

         return DEFAULT_BONUS;
      }
   }

   private enum GenType {
      LARGE,
      MEDIUM,
      LAKE
   }

   /** Finite (1-wide) vs large spring (3-wide base segment). */
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
         return randomIntInclusive(rand, this.minSegmentHeight(), this.maxSegmentHeight());
      }
   }
}
