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
import buildcraft.energy.BCEnergyWorldGen;
import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.lib.misc.RandUtil;
import buildcraft.lib.misc.RegistryKeyUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public final class OilGenerator {
   private static final long MAGIC_GEN_NUMBER = 0xD0_46_B4_E4_0C_7D_07_CFL;
   private static final int MAX_CHUNK_RADIUS = 5;
   public static final boolean DEBUG_OILGEN_BASIC = BCDebugging.shouldDebugLog("energy.oilgen");
   public static final boolean DEBUG_OILGEN_ALL = BCDebugging.shouldDebugComplex("energy.oilgen");

   private enum GenType {
      LARGE,
      MEDIUM,
      LAKE,
      NONE
   }

   private OilGenerator() {
   }

   public static boolean canGenerateOilIn(ServerLevel level) {
      if (level.getChunkSource().getGenerator() instanceof FlatLevelSource) {
         return false;
      }
      return !isDimensionExcluded(level.dimension());
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return BCEnergyConfig.getSurfaceDepositBiomes().contains(biomeId) || BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId);
   }

   public static boolean isOilDesignBiomeAt(Level level, int chunkX, int chunkZ) {
      if (level instanceof ServerLevel serverLevel) {
         Holder<Biome> biome = level.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
         Identifier id = BCEnergyWorldGen.effectiveBiomeId(
            serverLevel,
            chunkX * 16 + 8,
            chunkZ * 16 + 8,
            biome,
            biomeId(biome)
         );
         return isOilDesignBiome(id);
      }
      return isOilDesignBiome(biomeId(level.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8))));
   }

   public static boolean wouldGenerateOilForOriginChunk(ServerLevel level, int chunkX, int chunkZ) {
      if (!canGenerateOilIn(level)) {
         return false;
      }
      return !getStructures(level, chunkX, chunkZ, false).isEmpty();
   }

   public static boolean placeForChunk(WorldGenLevel level, BlockPos origin) {
      if (!canGenerateOilIn(level.getLevel())) {
         return false;
      }

      int chunkX = origin.getX() >> 4;
      int chunkZ = origin.getZ() >> 4;
      int x = chunkX * 16 + 8;
      int z = chunkZ * 16 + 8;
      BlockPos min = new BlockPos(x, level.getMinY(), z);
      Box box = new Box(min, min.offset(15, level.getMaxY(), 15));
      boolean placed = false;

      for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++) {
         for (int cdz = -MAX_CHUNK_RADIUS; cdz <= MAX_CHUNK_RADIUS; cdz++) {
            int cx = chunkX + cdx;
            int cz = chunkZ + cdz;
            List<OilGenStructure> structures = getStructures(level.getLevel(), cx, cz, cdx == 0 && cdz == 0);
            OilGenStructure.Spring spring = null;
            for (OilGenStructure struct : structures) {
               struct.generate(level, box);
               if (struct instanceof OilGenStructure.Spring s) {
                  spring = s;
               }
               if (struct.box.getIntersect(box) != null) {
                  placed = true;
               }
            }
            if (spring != null && box.contains(spring.pos)) {
               int count = 0;
               for (OilGenStructure struct : structures) {
                  count += struct.countOilBlocks();
               }
               spring.generate(level, count);
               placed = true;
            }
         }
      }

      return placed;
   }

   public static List<OilGenStructure> getStructures(LevelAccessor level, int cx, int cz) {
      return getStructures(level, cx, cz, false);
   }

   private static List<OilGenStructure> getStructures(LevelAccessor level, int cx, int cz, boolean log) {
      long seed = level instanceof ServerLevel serverLevel ? serverLevel.getSeed() : 0L;
      Random rand = RandUtil.createRandomForChunk(seed, cx, cz, MAGIC_GEN_NUMBER);
      int x = cx * 16 + 8 + rand.nextInt(16);
      int z = cz * 16 + 8 + rand.nextInt(16);
      Holder<Biome> biome = level.getBiome(new BlockPos(x, 0, z));
      Identifier biomeId = biomeId(biome);
      if (level instanceof ServerLevel serverLevel) {
         biomeId = BCEnergyWorldGen.effectiveBiomeId(serverLevel, x, z, biome, biomeId);
      }

      if (isBiomeExcluded(biomeId)) {
         if (DEBUG_OILGEN_BASIC && log) {
            BCLog.logger.info(
               "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz
                  + " because the biome we found (" + biomeId + ") is disabled!"
            );
         }
         return List.of();
      }

      if (biome.is(BiomeTags.IS_END) && (Math.abs(x) < 1200 || Math.abs(z) < 1200)) {
         return List.of();
      }

      boolean oilBiome = BCEnergyConfig.getSurfaceDepositBiomes().contains(biomeId);
      double bonus = oilBiome ? 3.0 : 1.0;
      bonus *= BCEnergyConfig.oilWellGenerationRate.get();
      if (BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId)) {
         bonus *= 30.0;
      }

      final GenType type;
      if (rand.nextDouble() <= BCEnergyConfig.largeOilGenProb.get() * bonus) {
         type = GenType.LARGE;
      } else if (rand.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * bonus) {
         type = GenType.MEDIUM;
      } else if (oilBiome && rand.nextDouble() <= BCEnergyConfig.smallOilGenProb.get() * bonus) {
         type = GenType.LAKE;
      } else {
         if (DEBUG_OILGEN_ALL && log) {
            BCLog.logger.info(
               "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz
                  + " because none of the random numbers were above the thresholds for generation"
            );
         }
         return List.of();
      }

      if (DEBUG_OILGEN_BASIC && log) {
         BCLog.logger.info(
            "[energy.oilgen] Generating an oil well (" + type.name().toLowerCase(Locale.ROOT)
               + ") in chunk " + cx + ", " + cz + " at " + x + ", " + z
         );
      }

      List<OilGenStructure> structures = new ArrayList<>();
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
      structures.add(createTendril(new BlockPos(x, 62, z), lakeRadius, tendrilRadius, rand));

      if (type != GenType.LAKE) {
         int wellY = level.getMinY() + 20 + rand.nextInt(10);
         int radius;
         if (type == GenType.LARGE) {
            radius = 8 + rand.nextInt(9);
         } else {
            radius = 4 + rand.nextInt(4);
         }
         structures.add(createSphere(new BlockPos(x, wellY, z), radius));

         if (BCEnergyConfig.enableOilSpouts.get()) {
            int maxHeight;
            int minHeight;
            if (type == GenType.LARGE) {
               minHeight = BCEnergyConfig.largeSpoutMinHeight.get();
               maxHeight = BCEnergyConfig.largeSpoutMaxHeight.get();
               radius = 1;
            } else {
               minHeight = BCEnergyConfig.smallSpoutMinHeight.get();
               maxHeight = BCEnergyConfig.smallSpoutMaxHeight.get();
               radius = 0;
            }
            final int height;
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

         if (type == GenType.LARGE) {
            structures.add(createTube(new BlockPos(x, level.getMinY() + 1, z), wellY, radius, Axis.Y));
            if (BCCoreBlocks.SPRING_OIL != null) {
               structures.add(createSpring(new BlockPos(x, level.getMinY(), z)));
            }
         }
      }
      return structures;
   }

   public static OilGenStructure createSpout(BlockPos start, int height, int radius) {
      return new OilGenStructure.Spout(start, OilGenStructure.ReplaceType.ALWAYS, radius, height);
   }

   public static OilGenStructure createSpring(BlockPos at) {
      return new OilGenStructure.Spring(at);
   }

   public static OilGenStructure createTube(BlockPos center, int length, int radius, Axis axis) {
      int valForAxis = VecUtil.getValue(center, axis);
      BlockPos min = VecUtil.replaceValue(center.offset(-radius, -radius, -radius), axis, valForAxis);
      BlockPos max = VecUtil.replaceValue(center.offset(radius, radius, radius), axis, valForAxis + length);
      double radiusSq = radius * radius;
      int toReplace = valForAxis;
      Predicate<BlockPos> tester = p -> VecUtil.replaceValue(p, axis, toReplace).distSqr(center) <= radiusSq;
      return new OilGenStructure.GenByPredicate(new Box(min, max), OilGenStructure.ReplaceType.ALWAYS, tester);
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

   private static void fillPatternIfProba(Random rand, float proba, int x, int z, boolean[][] pattern) {
      if (rand.nextFloat() <= proba) {
         pattern[x][z] = isSet(pattern, x, z - 1)
            | isSet(pattern, x, z + 1)
            | isSet(pattern, x - 1, z)
            | isSet(pattern, x + 1, z);
      }
   }

   private static boolean isSet(boolean[][] pattern, int x, int z) {
      if (x < 0 || x >= pattern.length) {
         return false;
      }
      if (z < 0 || z >= pattern[x].length) {
         return false;
      }
      return pattern[x][z];
   }

   private static boolean isDimensionExcluded(ResourceKey<Level> dimKey) {
      boolean inList = BCEnergyConfig.getExcludedDimensions().contains(RegistryKeyUtil.id(dimKey));
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }

   private static boolean isBiomeExcluded(Identifier biomeId) {
      boolean inList = BCEnergyConfig.getExcludedBiomes().contains(biomeId);
      return BCEnergyConfig.biomeListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }

   private static Identifier biomeId(Holder<Biome> biome) {
      return Mc26Compat.biomeId(biome);
   }
}
