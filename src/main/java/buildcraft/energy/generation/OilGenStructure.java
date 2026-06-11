/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation.core;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public abstract class OilGenStructure {
   /** Vanilla fluid features use flag 2 (block update, no neighbour reaction during gen). */
   public static final int GEN_FLAGS = 2;
   public final Box box;
   public final ReplaceType replaceType;

   protected OilGenStructure(Box containingBox, ReplaceType replaceType) {
      this.box = containingBox;
      this.replaceType = replaceType;
   }

   public final void generate(LevelAccessor level, Box within) {
      Box intersect = this.box.getIntersect(within);
      if (intersect != null) {
         this.generateWithin(level, intersect);
      }
   }

   protected abstract void generateWithin(LevelAccessor level, Box intersect);

   public abstract int countOilBlocks();

   public void setOilIfCanReplace(LevelAccessor level, BlockPos pos) {
      if (this.canReplaceForOil(level, pos)) {
         setOil(level, pos);
      }
   }

   public boolean canReplaceForOil(LevelAccessor level, BlockPos pos) {
      return this.replaceType.canReplace(level, pos);
   }

   private static BlockState cachedOilState;

   public static void setOil(LevelAccessor level, BlockPos pos) {
      level.setBlock(pos, oilState(), GEN_FLAGS);
   }

   private static BlockState oilState() {
      BlockState state = cachedOilState;
      if (state == null) {
         state = BCEnergyFluidsFabric.oilSourceBlockStateForLevel(null);
         cachedOilState = state;
      }
      return state;
   }

   private static double distLowCornerSq(BlockPos origin, int x, int y, int z) {
      int dx = origin.getX() - x;
      int dy = origin.getY() - y;
      int dz = origin.getZ() - z;
      return dx * dx + dy * dy + dz * dz;
   }

   public enum ReplaceType {
      ALWAYS {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return true;
         }
      };

      public abstract boolean canReplace(LevelAccessor level, BlockPos pos);
   }

   /** Filled sphere — slice iteration (~50% fewer tests than brute-force bounding cube). */
   public static final class Sphere extends OilGenStructure {
      private final BlockPos center;
      private final int radius;
      private final double radiusSq;
      private final int totalOilBlocks;

      public Sphere(BlockPos center, int radius, ReplaceType replaceType) {
         super(new Box(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius)), replaceType);
         this.center = center;
         this.radius = radius;
         this.radiusSq = radius * radius + 0.01;
         this.totalOilBlocks = countSphereBlocks(center, radius, this.radiusSq);
      }

      private static int countSphereBlocks(BlockPos center, int radius, double radiusSq) {
         int count = 0;
         for (int dy = -radius; dy <= radius; dy++) {
            int y = center.getY() + dy;
            int xzExtent = xzExtentAtY(dy, radius, radiusSq);
            for (int dx = -xzExtent; dx <= xzExtent; dx++) {
               int x = center.getX() + dx;
               for (int dz = -xzExtent; dz <= xzExtent; dz++) {
                  int z = center.getZ() + dz;
                  if (distLowCornerSq(center, x, y, z) <= radiusSq) {
                     count++;
                  }
               }
            }
         }
         return count;
      }

      private static int xzExtentAtY(int dy, int radius, double radiusSq) {
         double xzSq = radiusSq - dy * dy;
         return xzSq <= 0.0 ? 0 : (int)Math.floor(Math.sqrt(xzSq));
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         int minY = Math.max(intersect.min().getY(), this.center.getY() - this.radius);
         int maxY = Math.min(intersect.max().getY(), this.center.getY() + this.radius);
         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
         for (int y = minY; y <= maxY; y++) {
            int dy = y - this.center.getY();
            int xzExtent = xzExtentAtY(dy, this.radius, this.radiusSq);
            int minX = Math.max(intersect.min().getX(), this.center.getX() - xzExtent);
            int maxX = Math.min(intersect.max().getX(), this.center.getX() + xzExtent);
            int minZ = Math.max(intersect.min().getZ(), this.center.getZ() - xzExtent);
            int maxZ = Math.min(intersect.max().getZ(), this.center.getZ() + xzExtent);
            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  if (distLowCornerSq(this.center, x, y, z) <= this.radiusSq) {
                     this.setOilIfCanReplace(level, pos.set(x, y, z));
                  }
               }
            }
         }
      }

      @Override
      public int countOilBlocks() {
         return this.totalOilBlocks;
      }
   }

   /** Vertical cylinder (BC wells use {@link Axis#Y} tubes). */
   public static final class CylinderY extends OilGenStructure {
      private final BlockPos base;
      private final int length;
      private final int radius;
      private final int radiusSq;
      private final int totalOilBlocks;

      public CylinderY(BlockPos base, int length, int radius, ReplaceType replaceType) {
         super(
            new Box(
               base.offset(-radius, 0, -radius),
               base.offset(radius, length, radius)
            ),
            replaceType
         );
         this.base = base;
         this.length = length;
         this.radius = radius;
         this.radiusSq = radius * radius;
         this.totalOilBlocks = countCylinderBlocks(length, radius, this.radiusSq);
      }

      private static int countCylinderBlocks(int length, int radius, int radiusSq) {
         if (radius == 0) {
            return length + 1;
         }
         int perSlice = 0;
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               if (dx * dx + dz * dz <= radiusSq) {
                  perSlice++;
               }
            }
         }
         return perSlice * (length + 1);
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         int minY = Math.max(intersect.min().getY(), this.base.getY());
         int maxY = Math.min(intersect.max().getY(), this.base.getY() + this.length);
         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
         for (int y = minY; y <= maxY; y++) {
            int minX = Math.max(intersect.min().getX(), this.base.getX() - this.radius);
            int maxX = Math.min(intersect.max().getX(), this.base.getX() + this.radius);
            int minZ = Math.max(intersect.min().getZ(), this.base.getZ() - this.radius);
            int maxZ = Math.min(intersect.max().getZ(), this.base.getZ() + this.radius);
            for (int x = minX; x <= maxX; x++) {
               int dx = x - this.base.getX();
               for (int z = minZ; z <= maxZ; z++) {
                  int dz = z - this.base.getZ();
                  if (dx * dx + dz * dz <= this.radiusSq) {
                     this.setOilIfCanReplace(level, pos.set(x, y, z));
                  }
               }
            }
         }
      }

      @Override
      public int countOilBlocks() {
         return this.totalOilBlocks;
      }
   }

   public static class PatternTerrainHeight extends OilGenStructure {
      private final boolean[][] pattern;
      private final int depth;
      private final int totalOilBlocks;

      private PatternTerrainHeight(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
         super(containingBox, replaceType);
         this.pattern = pattern;
         this.depth = depth;
         int cells = 0;
         for (boolean[] row : pattern) {
            for (boolean cell : row) {
               if (cell) {
                  cells++;
               }
            }
         }
         this.totalOilBlocks = cells * depth;
      }

      public static PatternTerrainHeight create(BlockPos start, ReplaceType replaceType, boolean[][] pattern, int depth) {
         BlockPos min = VecUtil.replaceValue(start, Axis.Y, 1);
         BlockPos max = min.offset(pattern.length - 1, 255, pattern.length == 0 ? 0 : pattern[0].length - 1);
         return new PatternTerrainHeight(new Box(min, max), replaceType, pattern, depth);
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         WorldGenLevel worldGen = level instanceof WorldGenLevel wg ? wg : null;
         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
         for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
            int px = x - this.box.min().getX();
            for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
               int pz = z - this.box.min().getZ();
               if (px < 0 || pz < 0 || px >= this.pattern.length || pz >= this.pattern[px].length || !this.pattern[px][pz]) {
                  continue;
               }
               int topY = (worldGen != null ? worldGen : level).getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
               pos.set(x, topY, z);
               if (!this.canReplaceForOil(level, pos)) {
                  continue;
               }
               for (int y = 0; y < 5; y++) {
                  level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), GEN_FLAGS);
               }
               for (int y = 0; y < this.depth; y++) {
                  this.setOilIfCanReplace(level, pos.below(y));
               }
            }
         }
      }

      @Override
      public int countOilBlocks() {
         return this.totalOilBlocks;
      }
   }

   public static class Spout extends OilGenStructure {
      public final BlockPos start;
      public final int radius;
      public final int height;
      private int count = 0;

      public Spout(BlockPos start, ReplaceType replaceType, int radius, int height) {
         super(createBox(start), replaceType);
         this.start = start;
         this.radius = radius;
         this.height = height;
      }

      private static Box createBox(BlockPos start) {
         return new Box(start, VecUtil.replaceValue(start, Axis.Y, 384));
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         this.count = 0;
         int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, this.start.getX(), this.start.getZ()) - 1;
         BlockPos.MutableBlockPos worldTop = new BlockPos.MutableBlockPos(this.start.getX(), topY, this.start.getZ());
         for (int y = topY; y >= this.start.getY(); y--) {
            worldTop.setY(y);
            BlockState state = level.getBlockState(worldTop);
            if (state.isAir()) {
               continue;
            }
            if (!state.getFluidState().isEmpty()) {
               break;
            }
            if (BlockUtil.blocksMotion(state)) {
               break;
            }
         }

         OilGenStructure tubeY = OilGenerator.createTubeY(this.start, worldTop.getY() - this.start.getY(), this.radius);
         tubeY.generate(level, intersect);
         this.count += tubeY.countOilBlocks();
         BlockPos base = worldTop.immutable();
         for (int r = this.radius; r >= 0; r--) {
            OilGenStructure struct = OilGenerator.createTubeY(base, this.height, r);
            struct.generate(level, intersect);
            base = base.offset(0, this.height, 0);
            this.count += struct.countOilBlocks();
         }
      }

      @Override
      public int countOilBlocks() {
         return this.count;
      }
   }

   public static class Spring extends OilGenStructure {
      public final BlockPos pos;

      public Spring(BlockPos pos) {
         super(new Box(pos, pos), ReplaceType.ALWAYS);
         this.pos = pos;
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
      }

      @Override
      public int countOilBlocks() {
         return 0;
      }

      public void generate(LevelAccessor level, int count) {
         BlockPos placement = resolvePlacement(level, this.pos);
         if (placement == null) {
            return;
         }
         BlockState state = BCCoreBlocks.SPRING_OIL.defaultBlockState();
         level.setBlock(placement, state, GEN_FLAGS);
         setOil(level, placement.above());
         if (level.getBlockEntity(placement) instanceof TileSpringOil spring) {
            spring.totalSources = count;
         } else {
            BCLog.logger.warn("[energy.gen.oil] Setting the blockstate didn't also set the tile at " + placement);
         }
      }

      @Nullable
      public static BlockPos resolvePlacement(LevelAccessor level, BlockPos column) {
         int x = column.getX();
         int z = column.getZ();
         int minY = level.getMinY();
         int scanMax = minY + 4;
         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
         for (int y = minY; y <= scanMax; y++) {
            pos.set(x, y, z);
            if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
               return pos.immutable();
            }
         }
         return null;
      }
   }
}
