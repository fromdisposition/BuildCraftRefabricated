package buildcraft.energy.worldgen.structure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Builds BC-style oil deposit structure templates as NBT for jigsaw pools. */
public final class OilStructureTemplateBuilder {
   private static final String OIL_BLOCK = "buildcraftenergy:oil";
   private static final String SPRING_BLOCK = "buildcraftcore:spring_oil";
   private static final String AIR_BLOCK = "minecraft:air";

   private OilStructureTemplateBuilder() {
   }

   public static void generateAll(Path structuresDir) throws IOException {
      writeLake(structuresDir.resolve("oil_lake_patch.nbt"), 21, 6);
      writeWell(structuresDir.resolve("oil_well_medium.nbt"), 41, 2, 5, 20, false);
      writeWell(structuresDir.resolve("oil_well_large.nbt"), 91, 4, 12, 30, true);
      writeWell(structuresDir.resolve("oil_well_large_alt.nbt"), 91, 4, 10, 28, true);
   }

   private static void writeLake(Path path, int diameter, int lakeRadius) throws IOException {
      int center = diameter / 2;
      List<OilStructureNbtWriter.BlockEntry> blocks = new ArrayList<>();
      boolean[][] pattern = lakePattern(diameter, lakeRadius);
      for (int x = 0; x < diameter; x++) {
         for (int z = 0; z < diameter; z++) {
            if (pattern[x][z]) {
               blocks.add(new OilStructureNbtWriter.BlockEntry(x, 0, z, OIL_BLOCK));
               blocks.add(new OilStructureNbtWriter.BlockEntry(x, -1, z, OIL_BLOCK));
            }
         }
      }
      OilStructureNbtWriter.write(path, diameter, 2, diameter, blocks);
   }

   private static void writeWell(
      Path path, int diameter, int lakeRadius, int sphereRadius, int sphereDepth, boolean withSpring
   ) throws IOException {
      int center = diameter / 2;
      List<OilStructureNbtWriter.BlockEntry> blocks = new ArrayList<>();
      boolean[][] surface = tendrilPattern(diameter, lakeRadius, diameter / 2 - lakeRadius - 2, 42L);
      for (int x = 0; x < diameter; x++) {
         for (int z = 0; z < diameter; z++) {
            if (surface[x][z]) {
               blocks.add(new OilStructureNbtWriter.BlockEntry(x, 0, z, OIL_BLOCK));
               blocks.add(new OilStructureNbtWriter.BlockEntry(x, -1, z, OIL_BLOCK));
            }
         }
      }

      for (int dx = -sphereRadius; dx <= sphereRadius; dx++) {
         for (int dy = -sphereRadius; dy <= sphereRadius; dy++) {
            for (int dz = -sphereRadius; dz <= sphereRadius; dz++) {
               if (dx * dx + dy * dy + dz * dz <= sphereRadius * sphereRadius) {
                  blocks.add(new OilStructureNbtWriter.BlockEntry(center + dx, sphereDepth + dy, center + dz, OIL_BLOCK));
               }
            }
         }
      }

      for (int y = sphereDepth; y <= sphereDepth + 14; y++) {
         blocks.add(new OilStructureNbtWriter.BlockEntry(center, y, center, OIL_BLOCK));
      }

      if (withSpring && net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(net.minecraft.resources.Identifier.parse(SPRING_BLOCK))) {
         blocks.add(springBlock(center, sphereDepth - 1, center));
      }

      OilStructureNbtWriter.write(path, diameter, sphereDepth + 20, diameter, blocks);
   }

   private static OilStructureNbtWriter.BlockEntry springBlock(int x, int y, int z) {
      return new OilStructureNbtWriter.BlockEntry(x, y, z, SPRING_BLOCK);
   }

   private static boolean[][] lakePattern(int diameter, int lakeRadius) {
      boolean[][] pattern = new boolean[diameter][diameter];
      int center = diameter / 2;
      for (int x = 0; x < diameter; x++) {
         for (int z = 0; z < diameter; z++) {
            int dx = x - center;
            int dz = z - center;
            pattern[x][z] = dx * dx + dz * dz <= lakeRadius * lakeRadius;
         }
      }
      return pattern;
   }

   /** Deterministic BC-style tendril flood fill (fixed seed). */
   private static boolean[][] tendrilPattern(int diameter, int lakeRadius, int tendrilRadius, long seed) {
      boolean[][] pattern = new boolean[diameter][diameter];
      int center = diameter / 2;
      Random rand = new Random(seed);
      for (int dx = -lakeRadius; dx <= lakeRadius; dx++) {
         for (int dz = -lakeRadius; dz <= lakeRadius; dz++) {
            if (dx * dx + dz * dz <= lakeRadius * lakeRadius) {
               pattern[center + dx][center + dz] = true;
            }
         }
      }

      int largeSpread = Math.max(1, tendrilRadius / 2);
      int radius = lakeRadius + tendrilRadius / 2 + rand.nextInt(largeSpread);
      int x = center;
      int z = center;
      for (int w = 1; w < radius && w < diameter / 2; w++) {
         float proba = (float)(radius - w + 4) / (radius + 4);
         fillIfProba(rand, proba, x, z + w, pattern);
         fillIfProba(rand, proba, x, z - w, pattern);
         fillIfProba(rand, proba, x + w, z, pattern);
         fillIfProba(rand, proba, x - w, z, pattern);
      }
      return pattern;
   }

   private static void fillIfProba(Random rand, float proba, int x, int z, boolean[][] pattern) {
      if (x < 0 || x >= pattern.length || z < 0 || z >= pattern[x].length) {
         return;
      }
      if (rand.nextFloat() <= proba) {
         pattern[x][z] = isSet(pattern, x, z - 1) | isSet(pattern, x, z + 1) | isSet(pattern, x - 1, z) | isSet(pattern, x + 1, z);
      }
   }

   private static boolean isSet(boolean[][] pattern, int x, int z) {
      if (x < 0 || x >= pattern.length || z < 0 || z >= pattern[x].length) {
         return false;
      }
      return pattern[x][z];
   }
}
