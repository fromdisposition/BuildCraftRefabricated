/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation.core;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.lib.misc.data.Box;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public final class OilGenerator {
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
      return !OilGenSettings.current().isDimensionExcluded(level.dimension());
   }

   public static boolean isOilDesignBiome(Identifier biomeId) {
      return OilGenSettings.current().designBiomes().contains(biomeId);
   }

   public static boolean isOilDesignBiomeAt(Level level, int chunkX, int chunkZ) {
      int x = chunkX * 16 + 8;
      int z = chunkZ * 16 + 8;
      if (level instanceof ServerLevel serverLevel) {
         Holder<Biome> biome = level.getBiome(new BlockPos(x, 0, z));
         Identifier id = OilBiomePatches.effectiveBiomeId(serverLevel, x, z, biome, biomeId(biome));
         return OilGenSettings.current().isDesignBiome(biome, id);
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
   public static boolean placeForChunk(WorldGenLevel level, BlockPos origin, OilGenSettings config) {
      if (!canGenerateOilIn(level.getLevel())) {
         return false;
      }

      int chunkX = origin.getX() >> 4;
      int chunkZ = origin.getZ() >> 4;
      ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
      BlockPos min = new BlockPos(chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ());
      Box box = new Box(min, new BlockPos(chunkPos.getMaxBlockX(), level.getMaxY(), chunkPos.getMaxBlockZ()));
      // #region agent log
      {
         int chunkMinX = chunkPos.getMinBlockX();
         int chunkMaxX = chunkPos.getMaxBlockX();
         int chunkMinZ = chunkPos.getMinBlockZ();
         int chunkMaxZ = chunkPos.getMaxBlockZ();
         boolean boxCoversFullChunk = box.min().getX() == chunkMinX && box.max().getX() == chunkMaxX
            && box.min().getZ() == chunkMinZ && box.max().getZ() == chunkMaxZ;
         if (Math.abs(chunkX) <= 2 && Math.abs(chunkZ) <= 2) {
            OilGenDebugLog.log(
               "H1",
               "OilGenerator.placeForChunk:box",
               "decorate_box_vs_chunk",
               java.util.Map.of(
                  "chunkX", chunkX,
                  "chunkZ", chunkZ,
                  "chunkMinX", chunkMinX,
                  "chunkMaxX", chunkMaxX,
                  "boxMinX", box.min().getX(),
                  "boxMaxX", box.max().getX(),
                  "boxMinZ", box.min().getZ(),
                  "boxMaxZ", box.max().getZ(),
                  "boxCoversFullChunk", boxCoversFullChunk,
                  "runId", "post-fix"
               )
            );
         }
      }
      // #endregion
      boolean placed = false;
      long worldSeed = level.getSeed();

      for (int cdx = -config.scanRadius(); cdx <= config.scanRadius(); cdx++) {
         for (int cdz = -config.scanRadius(); cdz <= config.scanRadius(); cdz++) {
            int cx = chunkX + cdx;
            int cz = chunkZ + cdz;
            if (!couldStructuresReachBox(cx, cz, box)) {
               continue;
            }

            final boolean logOrigin = cdx == 0 && cdz == 0;
            int cacheScope = OilGenCaches.cacheScope(config);
            List<OilGenStructure> structures = OilGenCaches.structure(
               worldSeed,
               cx,
               cz,
               cacheScope,
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
         return OilGenCaches.structure(
            seed, cx, cz, OilGenCaches.cacheScope(config), () -> computeStructures(level, cx, cz, log, config)
         );
      }
      return computeStructures(level, cx, cz, log, config);
   }

   private static List<OilGenStructure> computeStructures(LevelAccessor level, int cx, int cz, boolean log, OilGenSettings config) {
      long seed = level instanceof ServerLevel serverLevel ? serverLevel.getSeed() : level instanceof WorldGenLevel wg ? wg.getSeed() : 0L;
      BcChunkRandom rand = BcChunkRandom.forOilOriginChunk(seed, cx, cz);
      int x = cx * 16 + 8 + rand.nextInt(16);
      int z = cz * 16 + 8 + rand.nextInt(16);
      Holder<Biome> biome = resolveBiome(level, x, z);
      Identifier vanillaBiomeId = biomeId(biome);
      OilPatchSampler.Sample biomeSample = sampleBiome(level, x, z, biome, vanillaBiomeId);
      Identifier biomeId = biomeSample.effectiveBiomeId();
      OilPatchKind patchKind = biomeSample.patchKind();

      if (config.isBiomeExcluded(biome, biomeId)) {
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

      if (!config.enableOilOnWater() && OilSpawnRoll.isOceanOil(biome, patchKind)) {
         return List.of();
      }

      OilSpawnRoll.Tier tier = resolveTier(config, biome, patchKind, vanillaBiomeId, biomeId);
      if (tier == null) {
         return List.of();
      }
      // Rarity filter gates the decorating chunk only; neighbor origins in scan still roll per-origin.
      boolean skipSpawnRoll = config.useDatapackSpawnChance() && log;
      // #region agent log
      if (log && Math.abs(cx) <= 2 && Math.abs(cz) <= 2) {
         OilGenDebugLog.log(
            "H36",
            "OilGenerator.computeStructures",
            "spawn_roll_gate",
            java.util.Map.of(
               "originChunkX", cx,
               "originChunkZ", cz,
               "tier", tier.name(),
               "skipSpawnRoll", skipSpawnRoll,
               "useDatapackSpawnChance", config.useDatapackSpawnChance(),
               "runId", "post-fix3"
            )
         );
      }
      // #endregion
      if (!skipSpawnRoll) {
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
      int largeSpread = Math.max(1, config.tendrilSpreadLarge());
      int mediumSpread = Math.max(1, config.tendrilSpreadMedium());
      if (type == GenType.LARGE) {
         lakeRadius = config.lakeRadiusLarge();
         tendrilRadius = config.tendrilBaseLarge() + rand.nextInt(largeSpread);
      } else if (type == GenType.LAKE) {
         lakeRadius = config.lakeRadiusLake();
         tendrilRadius = config.tendrilBaseLarge() + rand.nextInt(largeSpread);
      } else {
         lakeRadius = config.lakeRadiusMedium();
         tendrilRadius = config.tendrilBaseMedium() + rand.nextInt(mediumSpread);
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

   static OilGenStructure createTendril(BlockPos center, int lakeRadius, int radius, BcChunkRandom rand) {
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

   private static void fillPatternIfProba(BcChunkRandom rand, float proba, int x, int z, boolean[][] pattern) {
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

   private static OilSpawnRoll.Tier resolveTier(
      OilGenSettings config, Holder<Biome> biome, OilPatchKind patchKind, Identifier vanillaBiomeId, Identifier effectiveBiomeId
   ) {
      return switch (config.forcedTier()) {
         case AUTO -> OilSpawnRoll.resolveTier(
            biome.is(BCEnergyBiomeTags.OIL_RICH_BIOME), patchKind, vanillaBiomeId, effectiveBiomeId, config
         );
         case NORMAL -> patchKind.isPatch() ? null : OilSpawnRoll.Tier.NORMAL;
         case RICH -> patchKind.isPatch() ? null : OilSpawnRoll.Tier.RICH;
         case PATCH -> patchKind.isPatch() ? OilSpawnRoll.Tier.OIL_PATCH : null;
      };
   }

   static Holder<Biome> resolveBiome(LevelAccessor level, int x, int z) {
      if (level instanceof WorldGenLevel worldGenLevel) {
         return worldGenLevel.getUncachedNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(0), QuartPos.fromBlock(z));
      }
      return level.getBiome(new BlockPos(x, 0, z));
   }

   private static OilPatchSampler.Sample sampleBiome(
      LevelAccessor level, int x, int z, Holder<Biome> biome, Identifier vanillaBiomeId
   ) {
      if (level instanceof WorldGenLevel worldGenLevel) {
         return OilPatchSampler.sample(worldGenLevel.getLevel(), x, z, biome, vanillaBiomeId);
      }
      if (level instanceof ServerLevel serverLevel) {
         return OilPatchSampler.sample(serverLevel, x, z, biome, vanillaBiomeId);
      }
      return new OilPatchSampler.Sample(vanillaBiomeId, OilPatchKind.NONE);
   }

}
