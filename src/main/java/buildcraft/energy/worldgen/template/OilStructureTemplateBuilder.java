package buildcraft.energy.worldgen.template;

import buildcraft.energy.worldgen.core.OilStructureDefaults;
import buildcraft.energy.worldgen.processor.OilWellProjectionProcessor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builds oil deposit NBT templates for jigsaw pools.
 *
 * <p>Well template Y convention ({@link OilWellProjectionProcessor} + gravity):
 * y=0 surface film, y&gt;0 spout, deposit and shaft use {@code heightmap - 1 + templateY}.
 * The sphere is clipped to {@link OilStructureDefaults#DEPOSIT_MAX_TEMPLATE_Y} through
 * {@link OilStructureDefaults#DEPOSIT_MIN_TEMPLATE_Y} so tall terrain never pulls the body to the surface.
 * Bedrock spring uses {@link OilStructureDefaults#SPRING_TEMPLATE_Y} and is pinned to {@code minY} after gravity.
 * Shaft width: BC 8.0 medium radius 0 (1 block), large radius 1 (3×3).
 */
public final class OilStructureTemplateBuilder {
   private static final String OIL_BLOCK = "buildcraftenergy:oil";
   private static final String SPRING_BLOCK = "buildcraftcore:spring_oil";
   /** Single-block surface film at template y=0 (wells, lakes, desert and ocean). */
   private static final int SURFACE_FILM_DEPTH = 1;

   private OilStructureTemplateBuilder() {
   }

   public static void generateAll(Path structuresDir) throws IOException {
      writeLake(structuresDir.resolve("oil_lake_patch.nbt"), 0x51AF1001L, 6, 26);
      writeLake(structuresDir.resolve("oil_lake_patch_b.nbt"), 0x51AF1002L, 6, 32);
      writeLake(structuresDir.resolve("oil_lake_patch_c.nbt"), 0x51AF1003L, 6, 38);
      writeLake(structuresDir.resolve("oil_lake_patch_d.nbt"), 0x51AF1004L, 6, 30);
      writeLake(structuresDir.resolve("oil_lake_patch_e.nbt"), 0x51AF1005L, 6, 42);

      writeWell(structuresDir.resolve("oil_well_medium_s.nbt"), 2, 5, 4, 6, 0, false);
      writeWell(structuresDir.resolve("oil_well_medium_alt.nbt"), 2, 9, 5, 8, 0, false);
      writeWell(structuresDir.resolve("oil_well_medium.nbt"), 2, 11, 6, 10, 0, false);
      writeWell(structuresDir.resolve("oil_well_medium_l.nbt"), 2, 14, 7, 12, 0, false);

      writeWell(structuresDir.resolve("oil_well_large_s.nbt"), 4, 28, 10, 12, 1, true);
      writeWell(structuresDir.resolve("oil_well_large_m.nbt"), 4, 31, 12, 14, 1, true);
      writeWell(structuresDir.resolve("oil_well_large.nbt"), 4, 35, 14, 18, 1, true);
      writeWell(structuresDir.resolve("oil_well_large_l.nbt"), 4, 42, 16, 20, 1, true);
   }

   private static void writeLake(Path path, long seed, int lakeRadius, int tendrilRadius) throws IOException {
      List<StructureNbtWriter.BlockEntry> blocks = new ArrayList<>();
      boolean[][] pattern = bcTendrilPattern(lakeRadius, tendrilRadius, seed);
      blitSurfacePattern(blocks, pattern);
      StructureNbtWriter.write(
         path,
         OilStructureDefaults.TEMPLATE_SIZE,
         StructureNbtWriter.computeSizeY(blocks, 4),
         OilStructureDefaults.TEMPLATE_SIZE,
         blocks
      );
   }

   private static void writeWell(
      Path path,
      int lakeRadius,
      int tendrilRadius,
      int sphereRadius,
      int surfaceSpoutHeight,
      int spoutRadius,
      boolean withSpring
   ) throws IOException {
      List<StructureNbtWriter.BlockEntry> blocks = new ArrayList<>();
      long seed = tendrilRadius * 31L + lakeRadius * 17L + sphereRadius * 13L;
      boolean[][] pattern = bcTendrilPattern(lakeRadius, tendrilRadius, seed);
      blitSurfacePattern(blocks, pattern);

      int center = OilStructureDefaults.TEMPLATE_CENTER;
      int sphereCenterY = OilStructureDefaults.SPHERE_TEMPLATE_CENTER_Y;
      int depositMinY = OilStructureDefaults.DEPOSIT_MIN_TEMPLATE_Y;
      int depositMaxY = OilStructureDefaults.DEPOSIT_MAX_TEMPLATE_Y;

      for (int dx = -sphereRadius; dx <= sphereRadius; dx++) {
         for (int dy = -sphereRadius; dy <= sphereRadius; dy++) {
            for (int dz = -sphereRadius; dz <= sphereRadius; dz++) {
               if (dx * dx + dy * dy + dz * dz > sphereRadius * sphereRadius) {
                  continue;
               }
               int y = sphereCenterY + dy;
               if (y < depositMinY || y > depositMaxY) {
                  continue;
               }
               blocks.add(new StructureNbtWriter.BlockEntry(center + dx, y, center + dz, OIL_BLOCK));
            }
         }
      }

      int sphereTop = Math.min(sphereCenterY + sphereRadius, depositMaxY);
      int sphereBottom = Math.max(sphereCenterY - sphereRadius, depositMinY);

      if (sphereTop < depositMaxY) {
         blitShaftColumn(blocks, center, sphereTop + 1, depositMaxY, spoutRadius);
      }

      if (withSpring) {
         blitShaftColumn(blocks, center, OilStructureDefaults.SPRING_TEMPLATE_Y + 1, sphereBottom - 1, spoutRadius);
         blocks.add(new StructureNbtWriter.BlockEntry(center, OilStructureDefaults.SPRING_TEMPLATE_Y, center, SPRING_BLOCK));
      }

      if (surfaceSpoutHeight > 0) {
         blitSurfaceSpout(blocks, center, surfaceSpoutHeight, spoutRadius);
      }

      StructureNbtWriter.write(
         path,
         OilStructureDefaults.TEMPLATE_SIZE,
         StructureNbtWriter.computeSizeY(blocks, 64),
         OilStructureDefaults.TEMPLATE_SIZE,
         blocks
      );
   }

   private static void blitShaftColumn(
      List<StructureNbtWriter.BlockEntry> blocks,
      int center,
      int yFrom,
      int yTo,
      int shaftRadius
   ) {
      if (yFrom > yTo) {
         return;
      }
      for (int y = yFrom; y <= yTo; y++) {
         writeCylinderY(blocks, center, y, center, shaftRadius);
      }
   }

   private static void blitSurfaceSpout(List<StructureNbtWriter.BlockEntry> blocks, int center, int height, int maxRadius) {
      for (int h = 1; h <= height; h++) {
         int radius = h >= height - 1 ? 0 : maxRadius;
         writeCylinderY(blocks, center, h, center, radius);
      }
   }

   private static void blitSurfacePattern(List<StructureNbtWriter.BlockEntry> blocks, boolean[][] pattern) {
      int center = OilStructureDefaults.TEMPLATE_CENTER;
      int half = pattern.length / 2;
      for (int x = 0; x < pattern.length; x++) {
         for (int z = 0; z < pattern[x].length; z++) {
            if (!pattern[x][z]) {
               continue;
            }
            int worldX = center - half + x;
            int worldZ = center - half + z;
            for (int d = 0; d < SURFACE_FILM_DEPTH; d++) {
               blocks.add(new StructureNbtWriter.BlockEntry(worldX, OilStructureDefaults.SURFACE_TEMPLATE_Y - d, worldZ, OIL_BLOCK));
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

   private static void writeCylinderY(List<StructureNbtWriter.BlockEntry> blocks, int centerX, int y, int centerZ, int radius) {
      if (radius <= 0) {
         blocks.add(new StructureNbtWriter.BlockEntry(centerX, y, centerZ, OIL_BLOCK));
         return;
      }
      int radiusSq = radius * radius;
      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            if (dx * dx + dz * dz <= radiusSq) {
               blocks.add(new StructureNbtWriter.BlockEntry(centerX + dx, y, centerZ + dz, OIL_BLOCK));
            }
         }
      }
   }
}
