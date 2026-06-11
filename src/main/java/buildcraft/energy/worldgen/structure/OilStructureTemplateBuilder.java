package buildcraft.energy.worldgen.structure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builds oil deposit NBT templates for jigsaw pools.
 *
 * <p>Template Y convention (processed by vanilla {@code terrain_matching} / {@code GravityProcessor}):
 * y=0 is the surface oil layer, y&lt;0 is underground, y&gt;0 is the surface spout fountain.
 */
public final class OilStructureTemplateBuilder {
   private static final String OIL_BLOCK = "buildcraftenergy:oil";
   private static final String SPRING_BLOCK = "buildcraftcore:spring_oil";

   private OilStructureTemplateBuilder() {
   }

   public static void generateAll(Path structuresDir) throws IOException {
      writeLake(structuresDir.resolve("oil_lake_patch.nbt"), 0x51AF1001L, 6, 26);
      writeLake(structuresDir.resolve("oil_lake_patch_b.nbt"), 0x51AF1002L, 6, 32);
      writeLake(structuresDir.resolve("oil_lake_patch_c.nbt"), 0x51AF1003L, 6, 38);
      writeLake(structuresDir.resolve("oil_lake_patch_d.nbt"), 0x51AF1004L, 6, 30);
      writeLake(structuresDir.resolve("oil_lake_patch_e.nbt"), 0x51AF1005L, 6, 42);

      writeWell(structuresDir.resolve("oil_well_medium_s.nbt"), 2, 5, 4, 32, 6, 0, false);
      writeWell(structuresDir.resolve("oil_well_medium_alt.nbt"), 2, 9, 5, 33, 8, 0, false);
      writeWell(structuresDir.resolve("oil_well_medium.nbt"), 2, 11, 6, 34, 10, 0, false);
      writeWell(structuresDir.resolve("oil_well_medium_l.nbt"), 2, 14, 7, 34, 12, 0, false);

      writeWell(structuresDir.resolve("oil_well_large_s.nbt"), 4, 28, 10, 38, 12, 1, true);
      writeWell(structuresDir.resolve("oil_well_large_m.nbt"), 4, 31, 12, 39, 14, 1, true);
      writeWell(structuresDir.resolve("oil_well_large.nbt"), 4, 35, 14, 40, 18, 1, true);
      writeWell(structuresDir.resolve("oil_well_large_l.nbt"), 4, 42, 16, 42, 20, 1, true);
   }

   private static void writeLake(Path path, long seed, int lakeRadius, int tendrilRadius) throws IOException {
      List<OilStructureNbtWriter.BlockEntry> blocks = new ArrayList<>();
      boolean[][] pattern = bcTendrilPattern(lakeRadius, tendrilRadius, seed);
      blitSurfacePattern(blocks, pattern, depthFromSeed(seed));
      OilStructureNbtWriter.write(
         path,
         OilStructureDefaults.TEMPLATE_SIZE,
         OilStructureNbtWriter.computeSizeY(blocks, 4),
         OilStructureDefaults.TEMPLATE_SIZE,
         blocks
      );
   }

   private static void writeWell(
      Path path,
      int lakeRadius,
      int tendrilRadius,
      int sphereRadius,
      int sphereDepth,
      int surfaceSpoutHeight,
      int spoutRadius,
      boolean withSpring
   ) throws IOException {
      List<OilStructureNbtWriter.BlockEntry> blocks = new ArrayList<>();
      long seed = tendrilRadius * 31L + lakeRadius * 17L + sphereRadius * 13L;
      boolean[][] pattern = bcTendrilPattern(lakeRadius, tendrilRadius, seed);
      blitSurfacePattern(blocks, pattern, 2);

      int center = OilStructureDefaults.TEMPLATE_CENTER;
      int sphereCenterY = -sphereDepth;
      for (int dx = -sphereRadius; dx <= sphereRadius; dx++) {
         for (int dy = -sphereRadius; dy <= sphereRadius; dy++) {
            for (int dz = -sphereRadius; dz <= sphereRadius; dz++) {
               if (dx * dx + dy * dy + dz * dz <= sphereRadius * sphereRadius) {
                  blocks.add(new OilStructureNbtWriter.BlockEntry(center + dx, sphereCenterY + dy, center + dz, OIL_BLOCK));
               }
            }
         }
      }

      int shaftBottom = -sphereDepth - 14;
      int shaftTop = sphereCenterY - sphereRadius - 1;
      for (int y = shaftBottom; y <= shaftTop; y++) {
         blocks.add(new OilStructureNbtWriter.BlockEntry(center, y, center, OIL_BLOCK));
      }

      int sphereTop = sphereCenterY + sphereRadius;
      for (int y = sphereTop + 1; y < 0; y++) {
         blocks.add(new OilStructureNbtWriter.BlockEntry(center, y, center, OIL_BLOCK));
      }

      if (surfaceSpoutHeight > 0) {
         blitSurfaceSpout(blocks, center, surfaceSpoutHeight, spoutRadius);
      }

      if (withSpring) {
         blocks.add(new OilStructureNbtWriter.BlockEntry(center, shaftBottom - 1, center, SPRING_BLOCK));
      }

      OilStructureNbtWriter.write(
         path,
         OilStructureDefaults.TEMPLATE_SIZE,
         OilStructureNbtWriter.computeSizeY(blocks, sphereDepth + surfaceSpoutHeight + 20),
         OilStructureDefaults.TEMPLATE_SIZE,
         blocks
      );
   }

   private static int depthFromSeed(long seed) {
      return (seed & 1L) == 0L ? 2 : 1;
   }

   private static void blitSurfaceSpout(List<OilStructureNbtWriter.BlockEntry> blocks, int center, int height, int maxRadius) {
      for (int h = 1; h <= height; h++) {
         int radius = h >= height - 1 ? 0 : maxRadius;
         writeCylinderY(blocks, center, h, center, radius);
      }
   }

   private static void blitSurfacePattern(List<OilStructureNbtWriter.BlockEntry> blocks, boolean[][] pattern, int depth) {
      int center = OilStructureDefaults.TEMPLATE_CENTER;
      int half = pattern.length / 2;
      for (int x = 0; x < pattern.length; x++) {
         for (int z = 0; z < pattern[x].length; z++) {
            if (!pattern[x][z]) {
               continue;
            }
            int worldX = center - half + x;
            int worldZ = center - half + z;
            blocks.add(new OilStructureNbtWriter.BlockEntry(worldX, 0, worldZ, OIL_BLOCK));
            if (depth >= 2) {
               blocks.add(new OilStructureNbtWriter.BlockEntry(worldX, -1, worldZ, OIL_BLOCK));
            }
         }
      }
   }

   /** Port of BC 8.0 {@code OilGenerator.createTendril}. */
   static boolean[][] bcTendrilPattern(int lakeRadius, int tendrilRadius, long seed) {
      int diameter = tendrilRadius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int x = tendrilRadius;
      int z = tendrilRadius;
      Random rand = new Random(seed);

      for (int dx = -lakeRadius; dx <= lakeRadius; dx++) {
         for (int dz = -lakeRadius; dz <= lakeRadius; dz++) {
            pattern[x + dx][z + dz] = dx * dx + dz * dz <= lakeRadius * lakeRadius;
         }
      }

      for (int w = 1; w < tendrilRadius; w++) {
         float proba = (float)(tendrilRadius - w + 4) / (tendrilRadius + 4);
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
      return pattern;
   }

   private static void fillPatternIfProba(Random rand, float proba, int x, int z, boolean[][] pattern) {
      if (x < 0 || x >= pattern.length || z < 0 || z >= pattern[x].length) {
         return;
      }
      if (rand.nextFloat() <= proba) {
         pattern[x][z] = isSet(pattern, x, z - 1)
            | isSet(pattern, x, z + 1)
            | isSet(pattern, x - 1, z)
            | isSet(pattern, x + 1, z);
      }
   }

   private static boolean isSet(boolean[][] pattern, int x, int z) {
      if (x < 0 || x >= pattern.length || z < 0 || z >= pattern[x].length) {
         return false;
      }
      return pattern[x][z];
   }

   private static void writeCylinderY(List<OilStructureNbtWriter.BlockEntry> blocks, int centerX, int y, int centerZ, int radius) {
      if (radius <= 0) {
         blocks.add(new OilStructureNbtWriter.BlockEntry(centerX, y, centerZ, OIL_BLOCK));
         return;
      }
      int radiusSq = radius * radius;
      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            if (dx * dx + dz * dz <= radiusSq) {
               blocks.add(new OilStructureNbtWriter.BlockEntry(centerX + dx, y, centerZ + dz, OIL_BLOCK));
            }
         }
      }
   }
}
