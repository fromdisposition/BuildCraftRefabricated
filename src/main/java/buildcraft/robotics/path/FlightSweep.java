/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.path;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Exact swept-volume test for a robot flying in a straight line: the robot's box, moved from one feet position to
 * another, must not enter any cell that has a collision shape. Cells the box already overlaps at the start are
 * exempt (the robot can always leave a block it is in), and one cell may be named as pass-through for docking.
 */
public final class FlightSweep {
   public static final double HALF_WIDTH = 0.25;
   public static final double HALF_HEIGHT = 0.25;
   private static final double EPS = 1.0E-7;
   private static final int RANGE_SCAN_LIMIT = 64;

   public interface CellTest {
      boolean isSoft(int x, int y, int z);
   }

   private FlightSweep() {
   }

   public static boolean isClear(Level level, Vec3 fromFeet, Vec3 toFeet, BlockPos passMin, BlockPos passMax) {
      return isClear((x, y, z) -> PathFinding.isSoftBlock(level, x, y, z), fromFeet, toFeet, passMin, passMax);
   }

   public static boolean isClear(CellTest soft, Vec3 fromFeet, Vec3 toFeet) {
      return isClear(soft, fromFeet, toFeet, null, null);
   }

   /** passMin/passMax (any corner order, both null for none) bound the cells the box may enter: a docking station's pipe and the cell in front of it. */
   public static boolean isClear(CellTest soft, Vec3 fromFeet, Vec3 toFeet, BlockPos passMin, BlockPos passMax) {
      double ox = fromFeet.x;
      double oy = fromFeet.y + HALF_HEIGHT;
      double oz = fromFeet.z;
      double dx = toFeet.x - fromFeet.x;
      double dy = toFeet.y - fromFeet.y;
      double dz = toFeet.z - fromFeet.z;

      int minX = Mth.floor(Math.min(ox, ox + dx) - HALF_WIDTH);
      int maxX = Mth.floor(Math.max(ox, ox + dx) + HALF_WIDTH);
      int minY = Mth.floor(Math.min(oy, oy + dy) - HALF_HEIGHT);
      int maxY = Mth.floor(Math.max(oy, oy + dy) + HALF_HEIGHT);
      int minZ = Mth.floor(Math.min(oz, oz + dz) - HALF_WIDTH);
      int maxZ = Mth.floor(Math.max(oz, oz + dz) + HALF_WIDTH);
      long volume = (long)(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);

      if (volume <= RANGE_SCAN_LIMIT) {
         for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
               for (int z = minZ; z <= maxZ; z++) {
                  if (blocks(soft, x, y, z, ox, oy, oz, dx, dy, dz, passMin, passMax)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }

      LongOpenHashSet seen = new LongOpenHashSet();
      int x = Mth.floor(ox);
      int y = Mth.floor(oy);
      int z = Mth.floor(oz);
      int endX = Mth.floor(ox + dx);
      int endY = Mth.floor(oy + dy);
      int endZ = Mth.floor(oz + dz);
      int stepX = dx > 0 ? 1 : dx < 0 ? -1 : 0;
      int stepY = dy > 0 ? 1 : dy < 0 ? -1 : 0;
      int stepZ = dz > 0 ? 1 : dz < 0 ? -1 : 0;
      double tMaxX = stepX == 0 ? Double.POSITIVE_INFINITY : ((stepX > 0 ? x + 1 : x) - ox) / dx;
      double tMaxY = stepY == 0 ? Double.POSITIVE_INFINITY : ((stepY > 0 ? y + 1 : y) - oy) / dy;
      double tMaxZ = stepZ == 0 ? Double.POSITIVE_INFINITY : ((stepZ > 0 ? z + 1 : z) - oz) / dz;
      double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
      double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
      double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);
      int remaining = Math.abs(endX - x) + Math.abs(endY - y) + Math.abs(endZ - z) + 1;

      while (remaining-- > 0) {
         for (int nx = x - 1; nx <= x + 1; nx++) {
            for (int ny = y - 1; ny <= y + 1; ny++) {
               for (int nz = z - 1; nz <= z + 1; nz++) {
                  if (seen.add(BlockPos.asLong(nx, ny, nz)) && blocks(soft, nx, ny, nz, ox, oy, oz, dx, dy, dz, passMin, passMax)) {
                     return false;
                  }
               }
            }
         }

         if (x == endX && y == endY && z == endZ) {
            break;
         }

         if (tMaxX < tMaxY && tMaxX < tMaxZ) {
            x += stepX;
            tMaxX += tDeltaX;
         } else if (tMaxY < tMaxZ) {
            y += stepY;
            tMaxY += tDeltaY;
         } else {
            z += stepZ;
            tMaxZ += tDeltaZ;
         }
      }

      return true;
   }

   private static boolean blocks(CellTest soft, int cx, int cy, int cz, double ox, double oy, double oz, double dx, double dy, double dz, BlockPos passMin, BlockPos passMax) {
      double tEnter = Double.NEGATIVE_INFINITY;
      double tExit = Double.POSITIVE_INFINITY;

      double lo = cx - HALF_WIDTH;
      double hi = cx + 1 + HALF_WIDTH;
      if (Math.abs(dx) < EPS) {
         if (ox <= lo || ox >= hi) {
            return false;
         }
      } else {
         double t1 = (lo - ox) / dx;
         double t2 = (hi - ox) / dx;
         tEnter = Math.max(tEnter, Math.min(t1, t2));
         tExit = Math.min(tExit, Math.max(t1, t2));
      }

      lo = cy - HALF_HEIGHT;
      hi = cy + 1 + HALF_HEIGHT;
      if (Math.abs(dy) < EPS) {
         if (oy <= lo || oy >= hi) {
            return false;
         }
      } else {
         double t1 = (lo - oy) / dy;
         double t2 = (hi - oy) / dy;
         tEnter = Math.max(tEnter, Math.min(t1, t2));
         tExit = Math.min(tExit, Math.max(t1, t2));
      }

      lo = cz - HALF_WIDTH;
      hi = cz + 1 + HALF_WIDTH;
      if (Math.abs(dz) < EPS) {
         if (oz <= lo || oz >= hi) {
            return false;
         }
      } else {
         double t1 = (lo - oz) / dz;
         double t2 = (hi - oz) / dz;
         tEnter = Math.max(tEnter, Math.min(t1, t2));
         tExit = Math.min(tExit, Math.max(t1, t2));
      }

      if (tEnter >= tExit || tExit <= 0.0 || tEnter >= 1.0 || tEnter < 0.0) {
         return false;
      }

      if (passMin != null && inRange(cx, passMin.getX(), passMax.getX()) && inRange(cy, passMin.getY(), passMax.getY()) && inRange(cz, passMin.getZ(), passMax.getZ())) {
         return false;
      }

      return !soft.isSoft(cx, cy, cz);
   }

   private static boolean inRange(int v, int a, int b) {
      return v >= Math.min(a, b) && v <= Math.max(a, b);
   }
}
