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
import buildcraft.lib.misc.data.Box;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public final class OilGenerator {
   private static final long MAGIC_GEN_NUMBER = 0xD0_46_B4_E4_0C_7D_07_CFL;
   /** BC 8.0 scans ±5 chunks so overlapping wells are stitched into the decorating chunk. */
   private static final int MAX_CHUNK_RADIUS = 5;
   /** Max horizontal reach: tendril radius 44 + in-chunk offset 8. */
   private static final int MAX_HORIZONTAL_REACH = 52;
   public static final boolean DEBUG_OILGEN_BASIC = BCDebugging.shouldDebugLog("energy.oilgen");
   public static final boolean DEBUG_OILGEN_ALL = BCDebugging.shouldDebugComplex("energy.oilgen");

   private enum GenType {
      LARGE,
      MEDIUM,
      LAKE
   }

   private OilGenerator() {
   }

   public static void invalidateCaches() {
      OilGenCaches.invalidateAll();
   }

   public static boolean canGenerateOilIn(ServerLevel level) {
      if (level.getChunkSource().getGenerator() instanceof FlatLevelSource) {
         return false;
      }
      return !isDimensionExcluded(level.dimension());
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return OilGenSettings.current().isDesignBiome(biomeId);
   }

   public static boolean isOilDesignBiomeAt(Level level, int chunkX, int chunkZ) {
      int x = chunkX * 16 + 8;
      int z = chunkZ * 16 + 8;
      if (level instanceof ServerLevel serverLevel) {
         Holder<Biome> biome = level.getBiome(new BlockPos(x, 0, z));
         Identifier id = OilBiomePatches.effectiveBiomeId(serverLevel, x, z, biome, biomeId(biome));
         return isOilDesignBiome(id);
      }
      return isOilDesignBiome(biomeId(level.getBiome(new BlockPos(x, 0, z))));
   }

   public static boolean wouldGenerateOilForOriginChunk(ServerLevel level, int chunkX, int chunkZ) {
      if (!canGenerateOilIn(level)) {
         return false;
      }
      return !getStructures(level, chunkX, chunkZ).isEmpty();
   }

   /**
    * BC 8.0 {@code onPopulatePre} equivalent for Fabric features. {@code origin} is the decorating chunk min corner
    * (vanilla {@link net.minecraft.core.SectionPos#origin()}).
    */
   public static boolean placeForChunk(WorldGenLevel level, BlockPos origin) {
      if (!canGenerateOilIn(level.getLevel())) {
         return false;
      }

      int chunkX = origin.getX() >> 4;
      int chunkZ = origin.getZ() >> 4;
      ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
      BlockPos min = new BlockPos(chunkPos.getMinBlockX() + 8, level.getMinY(), chunkPos.getMinBlockZ() + 8);
      Box box = new Box(min, min.offset(15, level.getMaxY(), 15));
      boolean placed = false;
      OilGenSettings config = OilGenSettings.current();
      long worldSeed = level.getSeed();

      for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++) {
         for (int cdz = -MAX_CHUNK_RADIUS; cdz <= MAX_CHUNK_RADIUS; cdz++) {
            int cx = chunkX + cdx;
            int cz = chunkZ + cdz;
            if (!couldStructuresReachBox(cx, cz, box)) {
               continue;
            }

            final boolean logOrigin = cdx == 0 && cdz == 0;
            List<OilGenStructure> structures = OilGenCaches.structure(
               worldSeed,
               cx,
               cz,
               () -> computeStructures(level, cx, cz, logOrigin, config)
            );
            if (structures.isEmpty()) {
               continue;
            }

            OilGenStructure.Spring spring = null;
            for (OilGenStructure struct : structures) {
               if (struct instanceof OilGenStructure.Spring s) {
                  spring = s;
               }
               if (struct.box.getIntersect(box) == null) {
                  continue;
               }
               struct.generate(level, box);
               placed = true;
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

   static List<OilGenStructure> getStructures(LevelAccessor level, int cx, int cz) {
      return getStructures(level, cx, cz, false);
   }

   private static List<OilGenStructure> getStructures(LevelAccessor level, int cx, int cz, boolean log) {
      OilGenSettings config = OilGenSettings.current();
      if (level instanceof WorldGenLevel worldGenLevel) {
         long seed = worldGenLevel.getSeed();
         return OilGenCaches.structure(seed, cx, cz, () -> computeStructures(level, cx, cz, log, config));
      }
      return computeStructures(level, cx, cz, log, config);
   }

   private static List<OilGenStructure> computeStructures(LevelAccessor level, int cx, int cz, boolean log, OilGenSettings config) {
      long seed = level instanceof ServerLevel serverLevel ? serverLevel.getSeed() : level instanceof WorldGenLevel wg ? wg.getSeed() : 0L;
      Random rand = RandUtil.createRandomForChunk(seed, cx, cz, MAGIC_GEN_NUMBER);
      int x = cx * 16 + 8 + rand.nextInt(16);
      int z = cz * 16 + 8 + rand.nextInt(16);
      Holder<Biome> biome = resolveBiome(level, x, z);
      Identifier vanillaBiomeId = biomeId(biome);
      Identifier biomeId = vanillaBiomeId;
      if (level instanceof WorldGenLevel worldGenLevel) {
         biomeId = OilBiomePatches.effectiveBiomeId(worldGenLevel.getLevel(), x, z, biome, biomeId);
      } else if (level instanceof ServerLevel serverLevel) {
         biomeId = OilBiomePatches.effectiveBiomeId(serverLevel, x, z, biome, biomeId);
      }

      if (config.isBiomeExcluded(biomeId)) {
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

      if (!config.enableOilOnWater() && OilSpawnRoll.isOceanOil(biome, biomeId)) {
         return List.of();
      }

      OilSpawnRoll.Tier tier = OilSpawnRoll.resolveTier(biomeId, vanillaBiomeId, config);
      double spawnChance = OilSpawnRoll.spawnChanceFraction(tier, config);
      if (rand.nextDouble() >= spawnChance) {
         if (DEBUG_OILGEN_ALL && log) {
            BCLog.logger.info(
               "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz
                  + " (tier=" + tier + ", chance=" + spawnChance * 100.0 + "%)"
            );
         }
         return List.of();
      }

      final GenType type = toGenType(OilSpawnRoll.rollDepositType(rand, tier, config));

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

         if (config.enableOilSpouts()) {
            int maxHeight;
            int minHeight;
            if (type == GenType.LARGE) {
               minHeight = config.largeSpoutMinHeight();
               maxHeight = config.largeSpoutMaxHeight();
               radius = 1;
            } else {
               minHeight = config.smallSpoutMinHeight();
               maxHeight = config.smallSpoutMaxHeight();
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
            structures.add(createTubeY(new BlockPos(x, level.getMinY() + 1, z), wellY, radius));
            if (config.spawnOilSprings() && BCCoreBlocks.SPRING_OIL != null) {
               structures.add(createSpring(new BlockPos(x, level.getMinY(), z)));
            }
         }
      }
      return structures;
   }

   static OilGenStructure createSpout(BlockPos start, int height, int radius) {
      return new OilGenStructure.Spout(start, OilGenStructure.ReplaceType.ALWAYS, radius, height);
   }

   static OilGenStructure createSpring(BlockPos at) {
      return new OilGenStructure.Spring(at);
   }

   static OilGenStructure createTubeY(BlockPos center, int length, int radius) {
      return new OilGenStructure.CylinderY(center, length, radius, OilGenStructure.ReplaceType.ALWAYS);
   }

   static OilGenStructure createSphere(BlockPos center, int radius) {
      return new OilGenStructure.Sphere(center, radius, OilGenStructure.ReplaceType.ALWAYS);
   }

   static OilGenStructure createTendril(BlockPos center, int lakeRadius, int radius, Random rand) {
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
      return OilGenStructure.PatternTerrainHeight.create(start, OilGenStructure.ReplaceType.ALWAYS, pattern, depth);
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

   private static boolean couldStructuresReachBox(int cx, int cz, Box target) {
      int extMinX = cx * 16 - MAX_HORIZONTAL_REACH;
      int extMaxX = cx * 16 + 15 + MAX_HORIZONTAL_REACH;
      int extMinZ = cz * 16 - MAX_HORIZONTAL_REACH;
      int extMaxZ = cz * 16 + 15 + MAX_HORIZONTAL_REACH;
      return target.max().getX() >= extMinX
         && target.min().getX() <= extMaxX
         && target.max().getZ() >= extMinZ
         && target.min().getZ() <= extMaxZ;
   }

   private static boolean isDimensionExcluded(ResourceKey<Level> dimKey) {
      boolean inList = BCEnergyConfig.getExcludedDimensions().contains(RegistryKeyUtil.id(dimKey));
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }

   private static Identifier biomeId(Holder<Biome> biome) {
      return Mc26Compat.biomeId(biome);
   }

   /**
    * MC 26.1.2: use {@link WorldGenLevel#getUncachedNoiseBiome} during feature placement — see {@code LevelReader#getNoiseBiome}
    * vs {@link net.minecraft.server.level.WorldGenRegion#getChunk} (throws instead of returning null).
    */
   private static GenType toGenType(OilSpawnRoll.DepositType deposit) {
      return switch (deposit) {
         case LARGE -> GenType.LARGE;
         case MEDIUM -> GenType.MEDIUM;
         case LAKE -> GenType.LAKE;
      };
   }

   static Holder<Biome> resolveBiome(LevelAccessor level, int x, int z) {
      if (level instanceof WorldGenLevel worldGenLevel) {
         return worldGenLevel.getUncachedNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(0), QuartPos.fromBlock(z));
      }
      return level.getBiome(new BlockPos(x, 0, z));
   }

}
