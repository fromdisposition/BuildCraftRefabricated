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
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
public class OilGenerator {
   private static final long MAGIC_GEN_NUMBER = -3438862373895731249L;
   private static final int SEA_LEVEL = 62;
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

   private static Random createRandomForChunk(Level level, int chunkX, int chunkZ, long magicNumber) {
      long worldSeed = level instanceof ServerLevel sl ? sl.getSeed() : 0L;
      Random worldRandom = new Random(worldSeed);
      long xSeed = worldRandom.nextLong() >> 3;
      long zSeed = worldRandom.nextLong() >> 3;
      long chunkSeed = xSeed * chunkX + zSeed * chunkZ ^ worldSeed;
      chunkSeed ^= magicNumber;
      return new Random(chunkSeed);
   }

   public static boolean canGenerateOilIn(ServerLevel level) {
      if (level.getChunkSource().getGenerator() instanceof FlatLevelSource) {
         return false;
      }

      return level.dimension() == Level.NETHER && BCEnergyConfig.enableNetherOilGeneration.get() || !isDimensionExcluded(level.dimension());
   }

   public static boolean wouldGenerateOilForOriginChunk(ServerLevel level, int chunkX, int chunkZ) {
      return canGenerateOilIn(level) && !getStructures(level, chunkX, chunkZ).isEmpty();
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return BCEnergyConfig.getRichSurfaceDepositBiomes().contains(biomeId) || BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId);
   }

   private static ChunkSample sampleChunk(Level level, int cx, int cz) {
      return sampleAt(level, cx, cz, (cx << 4) + 8, (cz << 4) + 8);
   }

   private static ChunkSample sampleAt(Level level, int cx, int cz, int x, int z) {
      Random rand = createRandomForChunk(level, cx, cz, MAGIC_GEN_NUMBER);
      Holder<Biome> biome = level.getBiome(new BlockPos(x, BIOME_SAMPLE_Y, z));
      Identifier biomeId = Identifier.parse(biome.getRegisteredName());
      return new ChunkSample(rand, x, z, biome, biomeId);
   }

   private static Identifier sampleBiomeForChunkRoll(Level level, int cx, int cz) {
      return sampleChunk(level, cx, cz).biomeId();
   }

   public static boolean isOilDesignBiomeAt(Level level, int chunkX, int chunkZ) {
      return isOilDesignBiome(sampleBiomeForChunkRoll(level, chunkX, chunkZ));
   }

   /** One origin chunk, one pass — placement origin comes from the configured feature modifiers. */
   public static boolean placeForChunk(WorldGenLevel level, BlockPos origin) {
      if (!canGenerateOilIn(level.getLevel())) {
         return false;
      }

      int chunkX = origin.getX() >> 4;
      int chunkZ = origin.getZ() >> 4;
      List<OilGenStructure> structures = getStructures(level.getLevel(), chunkX, chunkZ, origin.getX(), origin.getZ(), true);
      if (structures.isEmpty()) {
         return false;
      }

      OilGenStructure.Spring spring = generateStructures(level, structures);
      if (spring != null) {
         spring.generate(level, countOilSources(structures));
      }

      return true;
   }

   private static OilGenStructure.Spring generateStructures(WorldGenLevel level, List<OilGenStructure> structures) {
      OilGenStructure.Spring spring = null;

      for (OilGenStructure struct : structures) {
         struct.generate(level, struct.box);
         if (struct instanceof OilGenStructure.Spring found) {
            spring = found;
         }
      }

      return spring;
   }

   private static int countOilSources(List<OilGenStructure> structures) {
      int count = 0;

      for (OilGenStructure struct : structures) {
         if (!(struct instanceof OilGenStructure.Spring)) {
            count += struct.countOilBlocks();
         }
      }

      return count;
   }

   private static boolean isDimensionExcluded(ResourceKey<Level> dimKey) {
      boolean inList = BCEnergyConfig.getExcludedDimensions().contains(RegistryKeyUtil.id(dimKey));
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }

   public static List<OilGenStructure> getStructures(Level level, int cx, int cz) {
      return getStructures(level, cx, cz, false);
   }

   private static List<OilGenStructure> getStructures(Level level, int cx, int cz, boolean log) {
      return getStructures(level, cx, cz, (cx << 4) + 8, (cz << 4) + 8, log);
   }

   private static List<OilGenStructure> getStructures(Level level, int cx, int cz, int x, int z, boolean log) {
      ChunkSample sample = sampleAt(level, cx, cz, x, z);
      boolean isExcludedBiome = BCEnergyConfig.getExcludedBiomes().contains(sample.biomeId());
      boolean biomeBlacklisted = BCEnergyConfig.biomeListMode.get() == BCEnergyConfig.ListMode.BLACKLIST;
      if (isExcludedBiome == biomeBlacklisted) {
         if (DEBUG_OILGEN_BASIC && log) {
            BCLog.logger.info("[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because the biome (" + sample.biomeId() + ") is excluded!");
         }

         return ImmutableList.of();
      } else if (!sample.biome().is(BiomeTags.IS_END) || Math.abs(sample.x()) >= 1200 && Math.abs(sample.z()) >= 1200) {
         if (level.dimension() == Level.NETHER) {
            return getNetherStructures(level, log, sample.rand(), sample.x(), sample.z());
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
                     + sample.x()
                     + ", "
                     + sample.z()
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

   private static List<OilGenStructure> createOverworldStructures(Level level, ChunkSample sample, BiomeRoll roll, GenType type) {
      List<OilGenStructure> structures = new ArrayList<>();
      BlockPos surfaceCenter = new BlockPos(sample.x(), SEA_LEVEL, sample.z());

      if (type == OilGenerator.GenType.LAKE) {
         structures.add(createTendril(surfaceCenter, 6, 25 + sample.rand().nextInt(20), sample.rand()));
         return structures;
      }

      addSurfaceStructure(structures, sample.rand(), surfaceCenter, roll, type);

      int wellY = level.getMinY() + 25 + sample.rand().nextInt(10);
      int wellRadius = type == OilGenerator.GenType.LARGE ? 8 + sample.rand().nextInt(9) : 4 + sample.rand().nextInt(4);
      BlockPos wellCenter = new BlockPos(sample.x(), wellY, sample.z());
      structures.add(createSphere(wellCenter, wellRadius));

      boolean hasSpring = roll.richBiome() && (type == OilGenerator.GenType.LARGE || type == OilGenerator.GenType.MEDIUM);
      addSpoutIfEnabled(structures, sample.rand(), wellCenter, hasSpring);
      addSpringIfEnabled(level, structures, sample, wellY, wellRadius, hasSpring);

      return structures;
   }

   private static void addSurfaceStructure(List<OilGenStructure> structures, Random rand, BlockPos center, BiomeRoll roll, GenType type) {
      if (roll.richLand()) {
         structures.add(type == OilGenerator.GenType.LARGE ? createSurfacePoolLarge(center, rand) : createSurfacePoolMedium(center, rand));
      } else if (roll.isOcean()) {
         int lakeRadius = type == OilGenerator.GenType.LARGE ? 4 : 2;
         int tendrilRadius = type == OilGenerator.GenType.LARGE ? 25 + rand.nextInt(20) : 5 + rand.nextInt(10);
         structures.add(createTendril(center, lakeRadius, tendrilRadius, rand));
      }
   }

   private static void addSpoutIfEnabled(List<OilGenStructure> structures, Random rand, BlockPos wellCenter, boolean hasSpring) {
      if (!BCEnergyConfig.enableOilSpouts.get()) {
         return;
      }

      OilGenerator.SpoutKind kind = hasSpring ? OilGenerator.SpoutKind.LARGE : OilGenerator.SpoutKind.FINITE;
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

   private static List<OilGenStructure> getNetherStructures(Level level, boolean log, Random rand, int x, int z) {
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
         structures.add(createSpout(new BlockPos(x, wellY, z), OilGenerator.SpoutKind.LARGE.pickSegmentHeight(rand), OilGenerator.SpoutKind.LARGE.radius));
      }

      int tubeStart = level.getMinY() + 2;
      structures.add(createTube(new BlockPos(x, tubeStart, z), wellY - tubeStart, wellRadius, Axis.Y));
      if (BCEnergyConfig.spawnOilSprings.get()) {
         structures.add(createSpring(new BlockPos(x, level.getMinY(), z)));
      }

      return structures;
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

   public static OilGenStructure createTendril(BlockPos center, int lakeRadius, int radius, Random rand) {
      BlockPos start = center.offset(-radius, 0, -radius);
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

      int depth = rand.nextDouble() < 0.5 ? 1 : 2;
      return OilGenStructure.PatternTerrainHeight.create(start, pattern, depth, center.getY());
   }

   public static OilGenStructure createSurfacePoolMedium(BlockPos center, Random rand) {
      return createSurfacePool(center, 5 + rand.nextInt(3), rand);
   }

   public static OilGenStructure createSurfacePoolLarge(BlockPos center, Random rand) {
      return createSurfacePool(center, 8 + rand.nextInt(5), rand);
   }

   private static OilGenStructure createSurfacePool(BlockPos center, int baseRadius, Random rand) {
      int maxRadius = baseRadius + 1;
      int diameter = maxRadius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int centerIdx = maxRadius;

      for (int dx = -maxRadius; dx <= maxRadius; dx++) {
         for (int dz = -maxRadius; dz <= maxRadius; dz++) {
            int noise = rand.nextInt(3) - 1;
            int effectiveR = Math.max(0, baseRadius + noise);
            int distSq = dx * dx + dz * dz;
            pattern[centerIdx + dx][centerIdx + dz] = distSq <= effectiveR * effectiveR;
         }
      }

      int depth = rand.nextDouble() < 0.5 ? 1 : 2;
      BlockPos start = center.offset(-maxRadius, 0, -maxRadius);
      return OilGenStructure.SurfacePool.create(start, pattern, depth, center.getY());
   }

   private static void fillPatternIfProba(Random rand, float proba, int x, int z, boolean[][] pattern) {
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

   private static int randomIntInclusive(Random rand, int min, int max) {
      if (max < min) {
         int t = max;
         max = min;
         min = t;
      }

      return min + rand.nextInt(max - min + 1);
   }

   private record ChunkSample(Random rand, int x, int z, Holder<Biome> biome, Identifier biomeId) {
   }

   private record BiomeRoll(
      Random rand, boolean richBiome, boolean oilBiome, boolean mountainousBiome, boolean isOcean, boolean richLand, double globalMultiplier
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
         if (this.oilBiome) {
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

   /** Two spout profiles — BC 8.0 finite (1-wide) vs large spring (3-wide base segment). */
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

      int pickSegmentHeight(Random rand) {
         return randomIntInclusive(rand, this.minSegmentHeight(), this.maxSegmentHeight());
      }
   }
}
