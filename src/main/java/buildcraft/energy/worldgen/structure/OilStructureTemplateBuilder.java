package buildcraft.energy.worldgen.structure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Builds BC 8.0-style oil deposit structure templates as NBT for jigsaw pools. */
public final class OilStructureTemplateBuilder {
   private static final String OIL_BLOCK = "buildcraftenergy:oil";
   private static final String SPRING_BLOCK = "buildcraftcore:spring_oil";

   private OilStructureTemplateBuilder() {
   }

   public static void generateAll(Path structuresDir) throws IOException {
      writePatch(structuresDir.resolve("oil_lake_patch.nbt"), 0x51AF1001L, 6, 32, 2);
      writePatch(structuresDir.resolve("oil_lake_patch_b.nbt"), 0x51AF1002L, 6, 38, 1);
      writePatch(structuresDir.resolve("oil_lake_patch_c.nbt"), 0x51AF1003L, 6, 28, 2);

      writeWell(structuresDir.resolve("oil_well_medium.nbt"), 2, 12, 6, 34, 0, false);
      writeWell(structuresDir.resolve("oil_well_large.nbt"), 4, 35, 14, 40, 16, true);
      writeWell(structuresDir.resolve("oil_well_large_alt.nbt"), 4, 42, 11, 38, 12, true);
   }

   private static void writePatch(Path path, long seed, int lakeRadius, int tendrilRadius, int depth) throws IOException {
      List<OilStructureNbtWriter.BlockEntry> blocks = new ArrayList<>();
      boolean[][] pattern = bcTendrilPattern(lakeRadius, tendrilRadius, seed);
      blitSurfacePattern(blocks, pattern, depth);
      OilStructureNbtWriter.write(path, OilStructureDefaults.TEMPLATE_SIZE, depth, OilStructureDefaults.TEMPLATE_SIZE, blocks);
   }

   private static void writeWell(
      Path path,
      int lakeRadius,
      int tendrilRadius,
      int sphereRadius,
      int sphereDepth,
      int surfaceSpoutHeight,
      boolean withSpring
   ) throws IOException {
      List<OilStructureNbtWriter.BlockEntry> blocks = new ArrayList<>();
      long seed = tendrilRadius * 31L + lakeRadius * 17L + sphereRadius;
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

      int spoutTop = sphereCenterY + sphereRadius;
      for (int y = spoutTop + 1; y < 0; y++) {
         blocks.add(new OilStructureNbtWriter.BlockEntry(center, y, center, OIL_BLOCK));
      }

      if (surfaceSpoutHeight > 0) {
         blitSurfaceSpout(blocks, center, surfaceSpoutHeight);
      }

      if (withSpring && net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(net.minecraft.resources.Identifier.parse(SPRING_BLOCK))) {
         blocks.add(new OilStructureNbtWriter.BlockEntry(center, sphereCenterY - sphereRadius - 1, center, SPRING_BLOCK));
      }

      int undergroundDepth = sphereDepth + sphereRadius + 2;
      OilStructureNbtWriter.write(path, OilStructureDefaults.TEMPLATE_SIZE, undergroundDepth + 2, OilStructureDefaults.TEMPLATE_SIZE, blocks);
   }

   private static void blitSurfaceSpout(List<OilStructureNbtWriter.BlockEntry> blocks, int center, int height) {
      BlockPosWriter writer = new BlockPosWriter(blocks);
      for (int h = 1; h <= height; h++) {
         int radius = h >= height - 2 ? 0 : 1;
         writeCylinderY(writer, center, h, center, radius);
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

   /** Port of BC 8.0 {@code OilGenerator.createTendril} pattern fill. */
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

   private static void writeCylinderY(BlockPosWriter writer, int centerX, int y, int centerZ, int radius) {
      if (radius <= 0) {
         writer.add(centerX, y, centerZ, OIL_BLOCK);
         return;
      }
      int radiusSq = radius * radius;
      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            if (dx * dx + dz * dz <= radiusSq) {
               writer.add(centerX + dx, y, centerZ + dz, OIL_BLOCK);
            }
         }
      }
   }

   private static final class BlockPosWriter {
      private final List<OilStructureNbtWriter.BlockEntry> blocks;

      private BlockPosWriter(List<OilStructureNbtWriter.BlockEntry> blocks) {
         this.blocks = blocks;
      }

      private void add(int x, int y, int z, String blockId) {
         this.blocks.add(new OilStructureNbtWriter.BlockEntry(x, y, z, blockId));
      }
   }
}
