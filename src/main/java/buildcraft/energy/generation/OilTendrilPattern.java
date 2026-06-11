/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.util.RandomSource;

/** BC 8.0 fractal surface tendril (central lake disc + noisy arms). */
final class OilTendrilPattern {
   private OilTendrilPattern() {
   }

   static boolean[][] build(int lakeRadius, int radius, RandomSource rand) {
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
         fillIfProba(rand, proba, px, pz + w, pattern);
         fillIfProba(rand, proba, px, pz - w, pattern);
         fillIfProba(rand, proba, px + w, pz, pattern);
         fillIfProba(rand, proba, px - w, pz, pattern);

         for (int i = 1; i <= w; i++) {
            fillIfProba(rand, proba, px + i, pz + w, pattern);
            fillIfProba(rand, proba, px + i, pz - w, pattern);
            fillIfProba(rand, proba, px + w, pz + i, pattern);
            fillIfProba(rand, proba, px - w, pz + i, pattern);
            fillIfProba(rand, proba, px - i, pz + w, pattern);
            fillIfProba(rand, proba, px - i, pz - w, pattern);
            fillIfProba(rand, proba, px + w, pz - i, pattern);
            fillIfProba(rand, proba, px - w, pz - i, pattern);
         }
      }

      return pattern;
   }

   static int countCells(boolean[][] pattern) {
      int count = 0;
      for (boolean[] row : pattern) {
         for (boolean cell : row) {
            if (cell) {
               count++;
            }
         }
      }
      return count;
   }

   private static void fillIfProba(RandomSource rand, float proba, int x, int z, boolean[][] pattern) {
      if (rand.nextFloat() <= proba) {
         pattern[x][z] = isSet(pattern, x, z - 1) | isSet(pattern, x, z + 1) | isSet(pattern, x - 1, z) | isSet(pattern, x + 1, z);
      }
   }

   private static boolean isSet(boolean[][] pattern, int x, int z) {
      return x >= 0 && x < pattern.length && z >= 0 && z < pattern[x].length && pattern[x][z];
   }
}
