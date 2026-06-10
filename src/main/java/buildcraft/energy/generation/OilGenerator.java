/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.misc.RegistryKeyUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public class OilGenerator {
   private static final long MAGIC_GEN_NUMBER = -3438862373895731249L;
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
      } else {
         return level.dimension() == Level.NETHER && BCEnergyConfig.enableNetherOilGeneration.get() ? true : !isDimensionExcluded(level.dimension());
      }
   }

   public static boolean wouldGenerateOilForOriginChunk(ServerLevel level, int chunkX, int chunkZ) {
      return canGenerateOilIn(level) && !getStructures(level, chunkX, chunkZ).isEmpty();
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return BCEnergyConfig.getRichSurfaceDepositBiomes().contains(biomeId) || BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId);
   }

   public static boolean wouldGenerateOilForOriginChunkInOilBiome(ServerLevel level, int chunkX, int chunkZ) {
      if (!canGenerateOilIn(level)) {
         return false;
      }

      return isOilDesignBiome(sampleBiomeForChunkRoll(level, chunkX, chunkZ)) && !getStructures(level, chunkX, chunkZ).isEmpty();
   }

   private static Identifier sampleBiomeForChunkRoll(Level level, int cx, int cz) {
      Random rand = createRandomForChunk(level, cx, cz, MAGIC_GEN_NUMBER);
      int x = cx * 16 + 8 + rand.nextInt(16);
      int z = cz * 16 + 8 + rand.nextInt(16);
      return Identifier.parse(level.getBiome(new BlockPos(x, 64, z)).getRegisteredName());
   }

   public static boolean isOilDesignBiomeAt(Level level, int chunkX, int chunkZ) {
      return isOilDesignBiome(sampleBiomeForChunkRoll(level, chunkX, chunkZ));
   }

   public static boolean placeForChunk(WorldGenLevel level, int chunkX, int chunkZ) {
      if (!canGenerateOilIn(level.getLevel())) {
         return false;
      }

      List<OilGenStructure> structures = getStructures(level.getLevel(), chunkX, chunkZ, true);
      if (structures.isEmpty()) {
         return false;
      }

      for (OilGenStructure struct : structures) {
         struct.generate(level, struct.box);
      }

      finalizeSpringForOrigin(level, chunkX, chunkZ, structures);
      scheduleOilFluidTicks(level, structures);
      return true;
   }

   private static void scheduleOilFluidTicks(WorldGenLevel level, List<OilGenStructure> structures) {
      BlockState oil = BCEnergyFluidsFabric.oilSourceBlockStateForLevel(level.getLevel());
      if (oil == null) {
         return;
      }

      for (OilGenStructure struct : structures) {
         for (BlockPos pos : BlockPos.betweenClosed(struct.box.min(), struct.box.max())) {
            BlockState state = level.getBlockState(pos);
            if (state.is(oil.getBlock())) {
               FluidState fluidState = state.getFluidState();
               if (!fluidState.isEmpty()) {
                  level.scheduleTick(pos, fluidState.getType(), 0);
               }
            }
         }
      }
   }

   private static Box createChunkColumnBox(LevelAccessor level, int chunkX, int chunkZ) {
      int x = chunkX * 16 + 8;
      int z = chunkZ * 16 + 8;
      return new Box(new BlockPos(x, level.getMinY(), z), new BlockPos(x + 15, level.getMaxY(), z + 15));
   }

   private static void finalizeSpringForOrigin(LevelAccessor level, int originChunkX, int originChunkZ, List<OilGenStructure> structures) {
      OilGenStructure.Spring spring = null;

      for (OilGenStructure struct : structures) {
         if (struct instanceof OilGenStructure.Spring found) {
            spring = found;
            break;
         }
      }

      if (spring == null) {
         return;
      }

      Box originColumn = createChunkColumnBox(level, originChunkX, originChunkZ);

      for (OilGenStructure struct : structures) {
         if (struct instanceof OilGenStructure.Spout spout && !spout.hasPlacedOil()) {
            spout.generate(level, originColumn);
         }
      }

      int oilSourceCount = 0;

      for (OilGenStructure struct : structures) {
         if (!(struct instanceof OilGenStructure.Spring)) {
            oilSourceCount += struct.countOilBlocks();
         }
      }

      spring.generate(level, oilSourceCount);
   }

   private static boolean isDimensionExcluded(ResourceKey<Level> dimKey) {
      boolean inList = BCEnergyConfig.getExcludedDimensions().contains(RegistryKeyUtil.id(dimKey));
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }

   public static List<OilGenStructure> getStructures(Level level, int cx, int cz) {
      return getStructures(level, cx, cz, false);
   }

   private static List<OilGenStructure> getStructures(Level level, int cx, int cz, boolean log) {
      Random rand = createRandomForChunk(level, cx, cz, MAGIC_GEN_NUMBER);
      int x = cx * 16 + 8 + rand.nextInt(16);
      int z = cz * 16 + 8 + rand.nextInt(16);
      Holder<Biome> biomeHolder = level.getBiome(new BlockPos(x, 64, z));
      String registeredName = biomeHolder.getRegisteredName();
      Identifier biomeId = Identifier.parse(registeredName);
      boolean isExcludedBiome = BCEnergyConfig.getExcludedBiomes().contains(biomeId);
      boolean biomeBlacklisted = BCEnergyConfig.biomeListMode.get() == BCEnergyConfig.ListMode.BLACKLIST;
      if (isExcludedBiome == biomeBlacklisted) {
         if (DEBUG_OILGEN_BASIC && log) {
            BCLog.logger.info("[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because the biome (" + biomeId + ") is excluded!");
         }

         return ImmutableList.of();
      } else if (!biomeId.equals(Identifier.withDefaultNamespace("the_end")) || Math.abs(x) >= 1200 && Math.abs(z) >= 1200) {
         if (level.dimension() == Level.NETHER) {
            return getNetherStructures(level, cx, cz, log, rand, x, z);
         }

         boolean richBiome = BCEnergyConfig.getRichSurfaceDepositBiomes().contains(biomeId);
         boolean lightOceanBiome = !richBiome && BCEnergyConfig.getSurfaceDepositBiomes().contains(biomeId);
         boolean mountainousBiome = !richBiome && !lightOceanBiome && BCEnergyConfig.getMountainousSurfaceDepositBiomes().contains(biomeId);
         boolean standardBiome = !richBiome && !lightOceanBiome && !mountainousBiome && BCEnergyConfig.getStandardSurfaceDepositBiomes().contains(biomeId);
         boolean isOcean = biomeHolder.is(BiomeTags.IS_OCEAN);
         boolean richLand = richBiome && !isOcean;
         boolean richOcean = richBiome && isOcean;
         double globalMul = BCEnergyConfig.oilWellGenerationRate.get();
         if (BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId)) {
            globalMul *= 30.0;
         }

         OilGenerator.GenType type;
         if (richLand) {
            if (rand.nextDouble() <= RICH_LAND_LARGE_SPRING_PROB * globalMul) {
               type = OilGenerator.GenType.LARGE;
            } else {
               if (!(rand.nextDouble() <= RICH_LAND_MEDIUM_SPRING_PROB * globalMul)) {
                  if (DEBUG_OILGEN_ALL && log) {
                     BCLog.logger
                        .info(
                           "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because none of the random numbers were above the thresholds."
                        );
                  }

                  return ImmutableList.of();
               }

               type = OilGenerator.GenType.MEDIUM;
            }
         } else {
            double bonus;
            if (richOcean) {
               bonus = 1.5;
            } else if (lightOceanBiome) {
               bonus = 1.25;
            } else if (mountainousBiome) {
               bonus = 0.1;
            } else if (standardBiome) {
               bonus = 1.0;
            } else {
               bonus = 0.5;
            }

            double effectiveRate = bonus * globalMul;
            if (rand.nextDouble() <= BCEnergyConfig.largeOilGenProb.get() * effectiveRate) {
               type = OilGenerator.GenType.LARGE;
            } else if (rand.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * effectiveRate) {
               type = OilGenerator.GenType.MEDIUM;
            } else if (lightOceanBiome && rand.nextDouble() <= BCEnergyConfig.smallOilGenProb.get() * effectiveRate) {
               type = OilGenerator.GenType.LAKE;
            } else {
               if (DEBUG_OILGEN_ALL && log) {
                  BCLog.logger
                     .info(
                        "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because none of the random numbers were above the thresholds."
                     );
               }

               return ImmutableList.of();
            }
         }

         if (DEBUG_OILGEN_BASIC && log) {
            BCLog.logger
               .info("[energy.oilgen] Generating an oil well (" + type.name().toLowerCase(Locale.ROOT) + ") in chunk " + cx + ", " + cz + " at " + x + ", " + z);
         }

         List<OilGenStructure> structures = new ArrayList<>();
         int maxY = level.getMaxY();
         if (type == OilGenerator.GenType.LAKE) {
            structures.add(createTendril(new BlockPos(x, 62, z), 2, 5 + rand.nextInt(10), rand, maxY));
            return structures;
         }

         if (richLand && type == OilGenerator.GenType.LARGE) {
            structures.add(createSurfacePoolLarge(new BlockPos(x, 62, z), rand, maxY));
         } else if (richLand && type == OilGenerator.GenType.MEDIUM) {
            structures.add(createSurfacePoolMedium(new BlockPos(x, 62, z), rand, maxY));
         } else if (isOcean) {
            int lakeRadius = type == OilGenerator.GenType.LARGE ? 4 : 2;
            int tendrilRadius = type == OilGenerator.GenType.LARGE ? 25 + rand.nextInt(20) : 5 + rand.nextInt(10);
            structures.add(createTendril(new BlockPos(x, 62, z), lakeRadius, tendrilRadius, rand, maxY));
         }

         int wellY = level.getMinY() + 25 + rand.nextInt(10);
         int wellRadius = type == OilGenerator.GenType.LARGE ? 8 + rand.nextInt(9) : 4 + rand.nextInt(4);

         structures.add(createSphere(new BlockPos(x, wellY, z), wellRadius));
         boolean hasSpring = richBiome && (type == OilGenerator.GenType.LARGE || type == OilGenerator.GenType.MEDIUM);
         if (BCEnergyConfig.enableOilSpouts.get()) {
            int minHeight;
            int maxHeight;
            int spoutRadius;
            if (hasSpring) {
               minHeight = BCEnergyConfig.largeSpoutMinHeight.get();
               maxHeight = BCEnergyConfig.largeSpoutMaxHeight.get();
               spoutRadius = 1;
            } else {
               minHeight = BCEnergyConfig.finiteSpoutMinHeight.get();
               maxHeight = BCEnergyConfig.finiteSpoutMaxHeight.get();
               spoutRadius = 0;
            }

            int height;
            if (maxHeight == minHeight) {
               height = maxHeight;
            } else {
               if (maxHeight < minHeight) {
                  int t = maxHeight;
                  maxHeight = minHeight;
                  minHeight = t;
               }

               height = minHeight + rand.nextInt(maxHeight - minHeight);
            }

            structures.add(createSpout(new BlockPos(x, wellY, z), height, spoutRadius, level.getMaxY()));
         }

         if (hasSpring) {
            int tubeStart = level.getMinY() + 2;
            int tubeLength = wellY - tubeStart;
            structures.add(createTube(new BlockPos(x, tubeStart, z), tubeLength, wellRadius, Axis.Y));
            structures.add(createSpring(new BlockPos(x, level.getMinY(), z)));
         }

         return structures;
      } else {
         return ImmutableList.of();
      }
   }

   private static List<OilGenStructure> getNetherStructures(Level level, int cx, int cz, boolean log, Random rand, int x, int z) {
      double globalMul = BCEnergyConfig.oilWellGenerationRate.get() * BCEnergyConfig.netherOilGenRateMultiplier.get();
      OilGenerator.GenType type;
      if (rand.nextDouble() <= BCEnergyConfig.largeOilGenProb.get() * globalMul) {
         type = OilGenerator.GenType.LARGE;
      } else {
         if (!(rand.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * globalMul)) {
            if (DEBUG_OILGEN_ALL && log) {
               BCLog.logger
                  .info(
                     "[energy.oilgen] Not generating nether oil in chunk " + cx + ", " + cz + " because none of the random numbers were above the thresholds."
                  );
            }

            return ImmutableList.of();
         }

         type = OilGenerator.GenType.MEDIUM;
      }

      if (DEBUG_OILGEN_BASIC && log) {
         BCLog.logger
            .info(
               "[energy.oilgen] Generating nether oil well (" + type.name().toLowerCase(Locale.ROOT) + ") in chunk " + cx + ", " + cz + " at " + x + ", " + z
            );
      }

      List<OilGenStructure> structures = new ArrayList<>();
      BlockPos floor = OilGenStructure.findSolidSurfaceTop(level, x, z);
      int surfaceY = floor.getY() + 1;
      int maxY = level.getMaxY();
      if (type == OilGenerator.GenType.LARGE) {
         structures.add(createSurfacePoolLarge(new BlockPos(x, surfaceY, z), rand, maxY));
      } else {
         structures.add(createSurfacePoolMedium(new BlockPos(x, surfaceY, z), rand, maxY));
      }

      int wellY = level.getMinY() + 25 + rand.nextInt(10);
      int wellRadius = type == OilGenerator.GenType.LARGE ? 8 + rand.nextInt(9) : 4 + rand.nextInt(4);
      structures.add(createSphere(new BlockPos(x, wellY, z), wellRadius));
      if (BCEnergyConfig.enableOilSpouts.get()) {
         int minHeight = BCEnergyConfig.largeSpoutMinHeight.get();
         int maxHeight = BCEnergyConfig.largeSpoutMaxHeight.get();
         int height = maxHeight == minHeight ? maxHeight : minHeight + rand.nextInt(Math.max(1, maxHeight - minHeight));
         structures.add(createSpout(new BlockPos(x, wellY, z), height, 1, level.getMaxY()));
      }

      int tubeStart = level.getMinY() + 2;
      int tubeLength = wellY - tubeStart;
      structures.add(createTube(new BlockPos(x, tubeStart, z), tubeLength, wellRadius, Axis.Y));
      structures.add(createSpring(new BlockPos(x, level.getMinY(), z)));

      return structures;
   }

   private static OilGenStructure createSpout(BlockPos start, int height, int radius, int maxY) {
      return new OilGenStructure.Spout(start, OilGenStructure.ReplaceType.ALWAYS, radius, height, maxY);
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
      return new OilGenStructure.GenByPredicate(new Box(min, max), replaceType, tester);
   }

   public static OilGenStructure createSphere(BlockPos center, int radius) {
      Box box = new Box(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius));
      double radiusSq = radius * radius + 0.01;
      Predicate<BlockPos> tester = p -> p.distSqr(center) <= radiusSq;
      return new OilGenStructure.GenByPredicate(box, OilGenStructure.ReplaceType.ALWAYS, tester);
   }

   public static OilGenStructure createTendril(BlockPos center, int lakeRadius, int radius, Random rand, int maxY) {
      BlockPos start = center.offset(-radius, 0, -radius);
      int diameter = radius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int x = radius;
      int z = radius;

      for (int dx = -lakeRadius; dx <= lakeRadius; dx++) {
         for (int dz = -lakeRadius; dz <= lakeRadius; dz++) {
            pattern[x + dx][z + dz] = dx * dx + dz * dz <= lakeRadius * lakeRadius;
         }
      }

      for (int w = 1; w < radius; w++) {
         float proba = (float)(radius - w + 4) / (radius + 4);
         fillPatternIfProba(rand, proba, x, z + w, pattern);
         fillPatternIfProba(rand, proba, x, z - w, pattern);
         fillPatternIfProba(rand, proba, x + w, z, pattern);
         fillPatternIfProba(rand, proba, x - w, z, pattern);

         for (int i = 1; i <= w; i++) {
            fillPatternIfProba(rand, proba, x + i, z + w, pattern);
            fillPatternIfProba(rand, proba, x + i, z - w, pattern);
            fillPatternIfProba(rand, proba, x + w, z + i, pattern);
            fillPatternIfProba(rand, proba, x - w, z + i, pattern);
            fillPatternIfProba(rand, proba, x - i, z + w, pattern);
            fillPatternIfProba(rand, proba, x - i, z - w, pattern);
            fillPatternIfProba(rand, proba, x + w, z - i, pattern);
            fillPatternIfProba(rand, proba, x - w, z - i, pattern);
         }
      }

      int depth = rand.nextDouble() < 0.5 ? 1 : 2;
      return OilGenStructure.PatternTerrainHeight.create(start, OilGenStructure.ReplaceType.IS_FOR_LAKE, pattern, depth, maxY);
   }

   public static OilGenStructure createSurfacePoolMedium(BlockPos center, Random rand, int maxY) {
      return createSurfacePool(center, 5 + rand.nextInt(3), rand, maxY);
   }

   public static OilGenStructure createSurfacePoolLarge(BlockPos center, Random rand, int maxY) {
      return createSurfacePool(center, 8 + rand.nextInt(5), rand, maxY);
   }

   private static OilGenStructure createSurfacePool(BlockPos center, int baseRadius, Random rand, int maxY) {
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
      return OilGenStructure.SurfacePool.create(start, OilGenStructure.ReplaceType.IS_FOR_LAKE, pattern, depth, maxY);
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

   private enum GenType {
      LARGE,
      MEDIUM,
      LAKE
   }
}
