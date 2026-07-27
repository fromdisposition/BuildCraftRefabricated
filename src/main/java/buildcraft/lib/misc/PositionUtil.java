/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.misc.data.FaceDistance;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

public class PositionUtil {
   public static ChunkPos chunkContaining(BlockPos pos) {
      //? if >= 26.1 {
      return ChunkPos.containing(pos);
      //?} else {
      /*return new ChunkPos(pos);
      *///?}
   }

   public static int chunkX(ChunkPos cp) {
      //? if >= 26.1 {
      return cp.x();
      //?} else {
      /*return cp.x;
      *///?}
   }

   public static int chunkZ(ChunkPos cp) {
      //? if >= 26.1 {
      return cp.z();
      //?} else {
      /*return cp.z;
      *///?}
   }

   public static long chunkPack(ChunkPos cp) {
      //? if >= 26.1 {
      return cp.pack();
      //?} else {
      /*return cp.toLong();
      *///?}
   }

   @Nullable
   public static Direction getDirectFacingOffset(BlockPos from, BlockPos to) {
      BlockPos diff = to.subtract(from);
      boolean x = diff.getX() != 0;
      boolean y = diff.getY() != 0;
      boolean z = diff.getZ() != 0;
      if ((!x || !y) && (!x || !z) && (!y || !z)) {
         if (x) {
            return diff.getX() > 0 ? Direction.EAST : Direction.WEST;
         } else if (y) {
            return diff.getY() > 0 ? Direction.UP : Direction.DOWN;
         } else if (z) {
            return diff.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   public static Integer getDirectFacingDistance(BlockPos from, BlockPos to) {
      BlockPos diff = to.subtract(from);
      boolean x = diff.getX() != 0;
      boolean y = diff.getY() != 0;
      boolean z = diff.getZ() != 0;
      if ((!x || !y) && (!x || !z) && (!y || !z)) {
         if (x) {
            return diff.getX();
         } else if (y) {
            return diff.getY();
         } else {
            return z ? diff.getZ() : null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   public static FaceDistance getDirectOffset(BlockPos from, BlockPos to) {
      BlockPos diff = to.subtract(from);
      boolean x = diff.getX() != 0;
      boolean y = diff.getY() != 0;
      boolean z = diff.getZ() != 0;
      if ((!x || !y) && (!x || !z) && (!y || !z)) {
         if (x) {
            return new FaceDistance(Axis.X, diff.getX());
         } else if (y) {
            return new FaceDistance(Axis.Y, diff.getY());
         } else {
            return z ? new FaceDistance(Axis.Z, diff.getZ()) : null;
         }
      } else {
         return null;
      }
   }

   public static Set<BlockPos> getCorners(BlockPos min, BlockPos max) {
      if (min == null || max == null) {
         return ImmutableSet.of();
      }

      if (min.equals(max)) {
         return ImmutableSet.of(min);
      }

      Builder<BlockPos> set = ImmutableSet.builder();
      set.add(min);
      set.add(new BlockPos(max.getX(), min.getY(), min.getZ()));
      set.add(new BlockPos(min.getX(), max.getY(), min.getZ()));
      set.add(new BlockPos(max.getX(), max.getY(), min.getZ()));
      set.add(new BlockPos(min.getX(), min.getY(), max.getZ()));
      set.add(new BlockPos(max.getX(), min.getY(), max.getZ()));
      set.add(new BlockPos(min.getX(), max.getY(), max.getZ()));
      set.add(max);
      return set.build();
   }

   private static int getBoxAxisCount(BlockPos min, BlockPos max, BlockPos pos) {
      if (min != null && max != null && pos != null) {
         int same = 0;
         int x = pos.getX();
         int minX = min.getX();
         int maxX = max.getX();
         if (minX != x && maxX != x) {
            if (minX > x || maxX < x) {
               return 0;
            }
         } else {
            same++;
         }

         int y = pos.getY();
         int minY = min.getY();
         int maxY = max.getY();
         if (minY != y && maxY != y) {
            if (minY > y || maxY < y) {
               return 0;
            }
         } else {
            same++;
         }

         int z = pos.getZ();
         int minZ = min.getZ();
         int maxZ = max.getZ();
         if (minZ != z && maxZ != z) {
            if (minZ > z || maxZ < z) {
               return 0;
            }
         } else {
            same++;
         }

         return same;
      } else {
         return 0;
      }
   }

   public static boolean isCorner(BlockPos min, BlockPos max, BlockPos pos) {
      return getBoxAxisCount(min, max, pos) == 3;
   }

   public static boolean isOnEdge(BlockPos min, BlockPos max, BlockPos pos) {
      return getBoxAxisCount(min, max, pos) >= 2;
   }

   public static boolean isOnFace(BlockPos min, BlockPos max, BlockPos pos) {
      return getBoxAxisCount(min, max, pos) >= 1;
   }

   public static boolean isNextTo(BlockPos one, BlockPos two) {
      BlockPos diff = one.subtract(two);
      boolean x = diff.getX() == 1 || diff.getX() == -1;
      boolean y = diff.getY() == 1 || diff.getY() == -1;
      if (x && y) {
         return false;
      }

      boolean z = diff.getZ() == 1 || diff.getZ() == -1;
      return y && z ? false : x != z;
   }

   public static Rotation getRotatedFacing(Direction from, Direction to, Axis axis) {
      if (from.getAxis() == axis || to.getAxis() == axis) {
         throw new IllegalArgumentException("Cannot rotate around " + axis + " with " + from + " and " + to);
      } else if (from == to) {
         return Rotation.NONE;
      } else if (from.getOpposite() == to) {
         return Rotation.CLOCKWISE_180;
      } else {
         return from.getClockWise(axis) == to ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
      }
   }

   public static Direction rotateFacing(Direction from, Axis axis, Rotation rotation) {
      if (rotation != Rotation.NONE && rotation != null) {
         if (from.getAxis() == axis) {
            return from;
         }

         if (rotation == Rotation.CLOCKWISE_180) {
            return from.getOpposite();
         }

         if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            from = from.getOpposite();
         }

         return from.getClockWise(axis);
      } else {
         return from;
      }
   }

   public static Vec3 rotateVec(Vec3 from, Axis axis, Rotation rotation) {
      Vec3 rotated = new Vec3(0.0, 0.0, 0.0);
      double numEast = from.x;
      double numUp = from.y;
      double numSouth = from.z;
      Direction newEast = rotateFacing(Direction.EAST, axis, rotation);
      Direction newUp = rotateFacing(Direction.UP, axis, rotation);
      Direction newSouth = rotateFacing(Direction.SOUTH, axis, rotation);
      rotated = VecUtil.replaceValue(rotated, newEast.getAxis(), numEast * newEast.getAxisDirection().getStep());
      rotated = VecUtil.replaceValue(rotated, newUp.getAxis(), numUp * newUp.getAxisDirection().getStep());
      return VecUtil.replaceValue(rotated, newSouth.getAxis(), numSouth * newSouth.getAxisDirection().getStep());
   }

   public static BlockPos rotatePos(Vec3i from, Axis axis, Rotation rotation) {
      BlockPos rotated = new BlockPos(0, 0, 0);
      int numEast = from.getX();
      int numUp = from.getY();
      int numSouth = from.getZ();
      Direction newEast = rotateFacing(Direction.EAST, axis, rotation);
      Direction newUp = rotateFacing(Direction.UP, axis, rotation);
      Direction newSouth = rotateFacing(Direction.SOUTH, axis, rotation);
      rotated = VecUtil.replaceValue(rotated, newEast.getAxis(), numEast * newEast.getAxisDirection().getStep());
      rotated = VecUtil.replaceValue(rotated, newUp.getAxis(), numUp * newUp.getAxisDirection().getStep());
      return VecUtil.replaceValue(rotated, newSouth.getAxis(), numSouth * newSouth.getAxisDirection().getStep());
   }

   public static PositionUtil.LineSkewResult findLineSkewPoint(PositionUtil.Line line, Vec3 start, Vec3 direction) {
      double ia = 0.0;
      double ib = 1.0;
      double da = 0.0;
      double db = 0.0;
      double id = 0.5;
      Vec3 best = null;

      for (int i = 0; i < 10; i++) {
         Vec3 a = line.interpolate(ia);
         Vec3 b = line.interpolate(ib);
         Vec3 va = closestPointOnLineToPoint(a, start, direction);
         Vec3 vb = closestPointOnLineToPoint(b, start, direction);
         da = a.distanceToSqr(va);
         db = b.distanceToSqr(vb);
         if (da < db) {
            best = a;
            ib -= id;
         } else {
            best = b;
            ia += id;
         }

         id /= 2.0;
      }

      return new PositionUtil.LineSkewResult(best, Math.sqrt(Math.min(da, db)));
   }

   public static Vec3 closestPointOnLineToPoint(Vec3 point, Vec3 linePoint, Vec3 lineVector) {
      Vec3 v = lineVector.normalize();
      Vec3 p1 = linePoint;
      Vec3 p2 = point;
      Vec3 p2_minus_p1 = p2.subtract(p1);
      double _dot_v = VecUtil.dot(p2_minus_p1, v);
      Vec3 _scale_v = VecUtil.scale(v, _dot_v);
      return p1.add(_scale_v);
   }

   public static ImmutableList<BlockPos> getAllOnEdge(BlockPos min, BlockPos max) {
      com.google.common.collect.ImmutableList.Builder<BlockPos> list = ImmutableList.builder();
      boolean addX = max.getX() != min.getX();
      boolean addY = max.getY() != min.getY();
      boolean addZ = max.getZ() != min.getZ();
      if (addX & addY & addZ) {
         return getAllOnEdgeFull(min, max);
      }

      for (int x = min.getX(); x <= max.getX(); x++) {
         list.add(new BlockPos(x, min.getY(), min.getZ()));
         if (addY) {
            list.add(new BlockPos(x, max.getY(), min.getZ()));
            if (addZ) {
               list.add(new BlockPos(x, max.getY(), max.getZ()));
            }
         }

         if (addZ) {
            list.add(new BlockPos(x, min.getY(), max.getZ()));
         }
      }

      if (addY) {
         for (int y = min.getY() + 1; y < max.getY(); y++) {
            list.add(new BlockPos(min.getX(), y, min.getZ()));
            if (addX) {
               list.add(new BlockPos(max.getX(), y, min.getZ()));
               if (addZ) {
                  list.add(new BlockPos(max.getX(), y, max.getZ()));
               }
            }

            if (addZ) {
               list.add(new BlockPos(min.getX(), y, max.getZ()));
            }
         }
      }

      if (addZ) {
         for (int z = min.getZ() + 1; z < max.getZ(); z++) {
            list.add(new BlockPos(min.getX(), min.getY(), z));
            if (addX) {
               list.add(new BlockPos(max.getX(), min.getY(), z));
               if (addY) {
                  list.add(new BlockPos(max.getX(), max.getY(), z));
               }
            }

            if (addY) {
               list.add(new BlockPos(min.getX(), max.getY(), z));
            }
         }
      }

      return list.build();
   }

   private static ImmutableList<BlockPos> getAllOnEdgeFull(BlockPos min, BlockPos max) {
      com.google.common.collect.ImmutableList.Builder<BlockPos> list = ImmutableList.builder();

      for (int x = min.getX(); x <= max.getX(); x++) {
         list.add(new BlockPos(x, min.getY(), min.getZ()));
         list.add(new BlockPos(x, max.getY(), min.getZ()));
         list.add(new BlockPos(x, max.getY(), max.getZ()));
         list.add(new BlockPos(x, min.getY(), max.getZ()));
      }

      for (int y = min.getY() + 1; y < max.getY(); y++) {
         list.add(new BlockPos(min.getX(), y, min.getZ()));
         list.add(new BlockPos(max.getX(), y, min.getZ()));
         list.add(new BlockPos(max.getX(), y, max.getZ()));
         list.add(new BlockPos(min.getX(), y, max.getZ()));
      }

      for (int z = min.getZ() + 1; z < max.getZ(); z++) {
         list.add(new BlockPos(min.getX(), min.getY(), z));
         list.add(new BlockPos(max.getX(), min.getY(), z));
         list.add(new BlockPos(max.getX(), max.getY(), z));
         list.add(new BlockPos(min.getX(), max.getY(), z));
      }

      return list.build();
   }

   public static int getCountOnEdge(BlockPos min, BlockPos max) {
      int dx = Math.abs(max.getX() - min.getX());
      int dy = Math.abs(max.getY() - min.getY());
      int dz = Math.abs(max.getZ() - min.getZ());
      boolean addX = dx > 0;
      boolean addY = dy > 0;
      boolean addZ = dz > 0;
      int count = dx + 1;
      if (dy > 0) {
         count += dx + 1;
         if (addZ) {
            count += dx + 1;
         }
      }

      if (addZ) {
         count += dx + 1;
      }

      if (addY) {
         count += dy - 1;
         if (addX) {
            count += dy - 1;
            if (addZ) {
               count += dy - 1;
            }
         }

         if (addZ) {
            count += dy - 1;
         }
      }

      if (addZ) {
         count += dz - 1;
         if (addX) {
            count += dz - 1;
            if (addY) {
               count += dz - 1;
            }
         }

         if (addY) {
            count += dz - 1;
         }
      }

      return count;
   }

   public static ImmutableList<BlockPos> getAllOnPath(BlockPos from, BlockPos to) {
      com.google.common.collect.ImmutableList.Builder<BlockPos> interp = ImmutableList.builder();
      forAllOnPath(from, to, interp::add);
      return interp.build();
   }

   public static void forAllOnPath(BlockPos from, BlockPos to, Consumer<BlockPos> iter) {
      BlockPos difference = to.subtract(from);
      int ax = Math.abs(difference.getX());
      int ay = Math.abs(difference.getY());
      int az = Math.abs(difference.getZ());
      int count = ax + ay + az;
      BlockPos current = from;
      int ddx = difference.getX() > 0 ? 1 : -1;
      int ddy = difference.getY() > 0 ? 1 : -1;
      int ddz = difference.getZ() > 0 ? 1 : -1;
      int dx = count / 2;
      int dy = count / 2;
      int dz = count / 2;

      for (int j = 0; j < count; j++) {
         dx += ax;
         dy += ay;
         dz += az;
         boolean changed = false;
         if (dx >= count) {
            changed = true;
            dx -= count;
            current = current.offset(ddx, 0, 0);
         }

         if (dy >= count) {
            changed = true;
            dy -= count;
            current = current.offset(0, ddy, 0);
         }

         if (dz >= count) {
            changed = true;
            dz -= count;
            current = current.offset(0, 0, ddz);
         }

         if (changed) {
            iter.accept(current);
         }
      }
   }

   public static void forAllOnPath2d(int a1, int b1, int a2, int b2, PositionUtil.PathIterator2d iter) {
      int diff_a = a2 - a1;
      int diff_b = b2 - b1;
      int max_a = Math.abs(diff_a);
      int max_b = Math.abs(diff_b);
      int size_a = max_a + 1;
      int size_b = max_b + 1;
      int mult_a = diff_a > 0 ? 1 : -1;
      int mult_b = diff_b > 0 ? 1 : -1;
      boolean reverse = false;
      int multiplier;
      int offset;
      if (size_a == size_b) {
         multiplier = 1;
         offset = 0;
      } else {
         if (size_a > size_b) {
            int temp = size_a;
            size_a = size_b;
            size_b = temp;
            reverse = true;
         }

         multiplier = size_b / size_a;
         offset = size_b % size_a;
      }

      int normalLength = multiplier;
      int currentOffsetA = 0;
      int currentOffsetB = 0;
      int count = size_a;

      for (int i = 0; i < count; i++) {
         int length = normalLength;
         if (i < offset) {
            length++;
         }

         for (int l = 0; l < length; l++) {
            if (reverse) {
               iter.iterate(a1 + mult_a * currentOffsetB, b1 + mult_b * currentOffsetA);
            } else {
               iter.iterate(a1 + mult_a * currentOffsetA, b1 + mult_b * currentOffsetB);
            }

            currentOffsetB++;
         }

         currentOffsetA++;
      }
   }

   public static void forAllOnArc2d(int a, int b, int degrees, PositionUtil.PathIterator2d iter) {
   }

   public static BlockPos randomBlockPos(Random rand, BlockPos size) {
      return new BlockPos(rand.nextInt(size.getX()), rand.nextInt(size.getY()), rand.nextInt(size.getZ()));
   }

   public static BlockPos randomBlockPos(Random rand, BlockPos min, BlockPos max) {
      return new BlockPos(
         min.getX() + rand.nextInt(max.getX() - min.getX()),
         min.getY() + rand.nextInt(max.getY() - min.getY()),
         min.getZ() + rand.nextInt(max.getZ() - min.getZ())
      );
   }

   public static class Line {
      public final Vec3 start;
      public final Vec3 end;

      public Line(Vec3 start, Vec3 end) {
         this.start = start;
         this.end = end;
      }

      public static PositionUtil.Line createLongLine(Vec3 start, Vec3 direction) {
         return new PositionUtil.Line(start, VecUtil.scale(direction, 1024.0));
      }

      public Vec3 interpolate(double interp) {
         return VecUtil.scale(this.start, 1.0 - interp).add(VecUtil.scale(this.end, interp));
      }
   }

   public static class LineSkewResult {
      public final Vec3 closestPos;
      public final double distFromLine;

      public LineSkewResult(Vec3 closestPos, double distFromLine) {
         this.closestPos = closestPos;
         this.distFromLine = distFromLine;
      }
   }

   @FunctionalInterface
   public interface PathIterator2d {
      void iterate(int var1, int var2);
   }
}
