/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.lib.misc.RandUtil;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public class OilGenerator {
   private static final long MAGIC_GEN_NUMBER = -3438862373895731249L;
   private static final int MAX_CHUNK_RADIUS = 5;
   public static final boolean DEBUG_OILGEN_BASIC = BCDebugging.shouldDebugLog("energy.oilgen");
   public static final boolean DEBUG_OILGEN_ALL = BCDebugging.shouldDebugComplex("energy.oilgen");
   private static final double RICH_LAND_LARGE_SPRING_PROB = 6.0E-4;
   private static final double RICH_LAND_MEDIUM_SPRING_PROB = 0.0025;

   private OilGenerator() {
   }

   public static boolean canGenerateOilIn(ServerLevel level) {
      if (level.getChunkSource().getGenerator() instanceof FlatLevelSource) {
         return false;
      } else {
         return level.dimension() == Level.NETHER && BCEnergyConfig.enableNetherOilGeneration.get() ? true : !isDimensionExcluded(level.dimension());
      }
   }

   public static boolean wouldGenerateOilForOriginChunk(ServerLevel level, int chunkX, int chunkZ) {
      return canGenerateOilIn(level) && isChunkLoaded(level, chunkX, chunkZ) && !getStructures(level, chunkX, chunkZ).isEmpty();
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return BCEnergyConfig.getRichSurfaceDepositBiomes().contains(biomeId) || BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId);
   }

   public static boolean wouldGenerateOilForOriginChunkInOilBiome(ServerLevel level, int chunkX, int chunkZ) {
      if (!canGenerateOilIn(level)) {
         return false;
      } else if (!isChunkLoaded(level, chunkX, chunkZ)) {
         return false;
      } else {
         return !isOilDesignBiome(sampleBiomeForChunkRoll(level, chunkX, chunkZ)) ? false : !getStructures(level, chunkX, chunkZ).isEmpty();
      }
   }

   private static Identifier sampleBiomeForChunkRoll(Level level, int cx, int cz) {
      Random rand = RandUtil.createRandomForChunk(level, cx, cz, -3438862373895731249L);
      int x = cx * 16 + 8 + rand.nextInt(16);
      int z = cz * 16 + 8 + rand.nextInt(16);
      return Identifier.parse(level.getBiome(new BlockPos(x, 64, z)).getRegisteredName());
   }

   public static boolean isOilDesignBiomeAt(Level level, int chunkX, int chunkZ) {
      return level instanceof ServerLevel server && !isChunkLoaded(server, chunkX, chunkZ)
         ? false
         : isOilDesignBiome(sampleBiomeForChunkRoll(level, chunkX, chunkZ));
   }

   public static void generateForChunk(ServerLevel level, int chunkX, int chunkZ) {
      if (!canGenerateOilIn(level)) {
         if (DEBUG_OILGEN_BASIC) {
            String reason = level.getChunkSource().getGenerator() instanceof FlatLevelSource ? "the world is FLAT" : "dimension is excluded";
            BCLog.logger.info("[energy.oilgen] Not generating oil in chunk " + chunkX + ", " + chunkZ + " because " + reason + ".");
         }
      } else {
         int x = chunkX * 16 + 8;
         int z = chunkZ * 16 + 8;
         BlockPos min = new BlockPos(x, level.getMinY(), z);
         BlockPos maxPos = new BlockPos(x + 15, level.getMaxY(), z + 15);
         Box box = new Box(min, maxPos);

         for (int cdx = -5; cdx <= 5; cdx++) {
            for (int cdz = -5; cdz <= 5; cdz++) {
               int cx = chunkX + cdx;
               int cz = chunkZ + cdz;
               if (isChunkLoaded(level, cx, cz)) {
                  List<OilGenStructure> structures = getStructures(level, cx, cz, cdx == 0 && cdz == 0);
                  OilGenStructure.Spring spring = null;

                  for (OilGenStructure struct : structures) {
                     struct.generate(level, box);
                     if (struct instanceof OilGenStructure.Spring) {
                        spring = (OilGenStructure.Spring)struct;
                     }
                  }

                  if (spring != null && box.contains(spring.pos)) {
                     int count = 0;

                     for (OilGenStructure struct : structures) {
                        count += struct.countOilBlocks();
                     }

                     spring.generate(level, count);
                  }
               }
            }
         }
      }
   }

   private static boolean isDimensionExcluded(ResourceKey<Level> dimKey) {
      boolean inList = BCEnergyConfig.getExcludedDimensions().contains(RegistryKeyUtil.id(dimKey));
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }

   private static boolean isChunkLoaded(ServerLevel level, int chunkX, int chunkZ) {
      return Mc26Compat.isChunkLoaded(level, chunkX, chunkZ);
   }

   public static List<OilGenStructure> getStructures(Level level, int cx, int cz) {
      return getStructures(level, cx, cz, false);
   }

   private static List<OilGenStructure> getStructures(Level level, int cx, int cz, boolean log) {
      Random rand = RandUtil.createRandomForChunk(level, cx, cz, -3438862373895731249L);
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
            if (rand.nextDouble() <= 6.0E-4 * globalMul) {
               type = OilGenerator.GenType.LARGE;
            } else {
               if (!(rand.nextDouble() <= 0.0025 * globalMul)) {
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
            } else {
               if (!(rand.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * effectiveRate)) {
                  if (DEBUG_OILGEN_ALL && log) {
                     BCLog.logger
                        .info(
                           "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz + " because none of the random numbers were above the thresholds."
                        );
                  }

                  return ImmutableList.of();
               }

               if (lightOceanBiome && rand.nextDouble() < 0.5) {
                  type = OilGenerator.GenType.LAKE;
               } else {
                  type = OilGenerator.GenType.MEDIUM;
               }
            }
         }

         if (DEBUG_OILGEN_BASIC && log) {
            BCLog.logger
               .info("[energy.oilgen] Generating an oil well (" + type.name().toLowerCase(Locale.ROOT) + ") in chunk " + cx + ", " + cz + " at " + x + ", " + z);
         }

         List<OilGenStructure> structures = new ArrayList<>();
         if (type == OilGenerator.GenType.LAKE) {
            structures.add(createTendril(new BlockPos(x, 62, z), 2, 5 + rand.nextInt(10), rand));
            return structures;
         }

         if (richLand && type == OilGenerator.GenType.LARGE) {
            structures.add(createSurfacePoolLarge(new BlockPos(x, 62, z), rand));
         } else if (richLand && type == OilGenerator.GenType.MEDIUM) {
            structures.add(createSurfacePoolMedium(new BlockPos(x, 62, z), rand));
         } else if (isOcean) {
            int lakeRadius = type == OilGenerator.GenType.LARGE ? 4 : 2;
            int tendrilRadius = type == OilGenerator.GenType.LARGE ? 25 + rand.nextInt(20) : 5 + rand.nextInt(10);
            structures.add(createTendril(new BlockPos(x, 62, z), lakeRadius, tendrilRadius, rand));
         }

         int wellY = level.getMinY() + 25 + rand.nextInt(10);
         int radius;
         if (type == OilGenerator.GenType.LARGE) {
            radius = 8 + rand.nextInt(9);
         } else {
            radius = 4 + rand.nextInt(4);
         }

         structures.add(createSphere(new BlockPos(x, wellY, z), radius));
         boolean hasSpring = richBiome && (type == OilGenerator.GenType.LARGE || type == OilGenerator.GenType.MEDIUM);
         if (BCEnergyConfig.enableOilSpouts.get()) {
            int maxHeight;
            int minHeight;
            if (hasSpring) {
               minHeight = BCEnergyConfig.largeSpoutMinHeight.get();
               maxHeight = BCEnergyConfig.largeSpoutMaxHeight.get();
               radius = 1;
            } else {
               minHeight = BCEnergyConfig.finiteSpoutMinHeight.get();
               maxHeight = BCEnergyConfig.finiteSpoutMaxHeight.get();
               radius = 0;
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

            structures.add(createSpout(new BlockPos(x, wellY, z), height, radius));
         }

         if (hasSpring) {
            int tubeStart = level.getMinY() + 2;
            int tubeLength = wellY - tubeStart;
            structures.add(createTube(new BlockPos(x, tubeStart, z), tubeLength, radius, Axis.Y));
            if (BCCoreBlocks.SPRING_OIL != null) {
               structures.add(createSpring(new BlockPos(x, level.getMinY(), z)));
            }
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
      if (type == OilGenerator.GenType.LARGE) {
         structures.add(createSurfacePoolLarge(new BlockPos(x, surfaceY, z), rand));
      } else {
         structures.add(createSurfacePoolMedium(new BlockPos(x, surfaceY, z), rand));
      }

      int wellY = level.getMinY() + 25 + rand.nextInt(10);
      int radius = type == OilGenerator.GenType.LARGE ? 8 + rand.nextInt(9) : 4 + rand.nextInt(4);
      structures.add(createSphere(new BlockPos(x, wellY, z), radius));
      if (BCEnergyConfig.enableOilSpouts.get()) {
         int minHeight = BCEnergyConfig.largeSpoutMinHeight.get();
         int maxHeight = BCEnergyConfig.largeSpoutMaxHeight.get();
         int height = maxHeight == minHeight ? maxHeight : minHeight + rand.nextInt(Math.max(1, maxHeight - minHeight));
         structures.add(createSpout(new BlockPos(x, wellY, z), height, 1));
      }

      int tubeStart = level.getMinY() + 2;
      int tubeLength = wellY - tubeStart;
      structures.add(createTube(new BlockPos(x, tubeStart, z), tubeLength, 1, Axis.Y));
      if (BCCoreBlocks.SPRING_OIL != null) {
         structures.add(createSpring(new BlockPos(x, level.getMinY(), z)));
      }

      return structures;
   }

   private static OilGenStructure createSpout(BlockPos start, int height, int radius) {
      return new OilGenStructure.Spout(start, OilGenStructure.ReplaceType.ALWAYS, radius, height);
   }

   public static OilGenStructure createTubeY(BlockPos base, int height, int radius) {
      return createTube(base, height, radius, Axis.Y);
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

   public static OilGenStructure createTendril(BlockPos center, int lakeRadius, int radius, Random rand) {
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
      return OilGenStructure.PatternTerrainHeight.create(start, OilGenStructure.ReplaceType.IS_FOR_LAKE, pattern, depth);
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
      return OilGenStructure.SurfacePool.create(start, OilGenStructure.ReplaceType.IS_FOR_LAKE, pattern, depth);
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
      LAKE,
      NONE;
   }
}
