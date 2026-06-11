/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public abstract class OilGenStructure {
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

   protected abstract int countOilBlocks();

   public void setOilIfCanReplace(LevelAccessor level, BlockPos pos) {
      if (this.canReplaceForOil(level, pos)) {
         setOil(level, pos);
      }
   }

   public boolean canReplaceForOil(LevelAccessor level, BlockPos pos) {
      return this.replaceType.canReplace(level, pos);
   }

   public static void setOil(LevelAccessor level, BlockPos pos) {
      BlockState oil = BCEnergyFluidsFabric.OIL_COOL != null
         ? BCEnergyFluidsFabric.OIL_COOL.still().defaultFluidState().createLegacyBlock()
         : BCEnergyFluidsFabric.oilSourceBlockStateForLevel(null);
      level.setBlock(pos, oil, GEN_FLAGS);
   }

   public enum ReplaceType {
      ALWAYS {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return true;
         }
      },
      IS_FOR_LAKE {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return ALWAYS.canReplace(level, pos);
         }
      };

      public abstract boolean canReplace(LevelAccessor level, BlockPos pos);
   }

   public static class GenByPredicate extends OilGenStructure {
      public final Predicate<BlockPos> predicate;

      public GenByPredicate(Box containingBox, ReplaceType replaceType, Predicate<BlockPos> predicate) {
         super(containingBox, replaceType);
         this.predicate = predicate;
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         for (BlockPos pos : BlockPos.betweenClosed(intersect.min(), intersect.max())) {
            BlockPos immutable = pos.immutable();
            if (this.predicate.test(immutable)) {
               this.setOilIfCanReplace(level, immutable);
            }
         }
      }

      @Override
      protected int countOilBlocks() {
         int count = 0;
         for (BlockPos pos : BlockPos.betweenClosed(this.box.min(), this.box.max())) {
            if (this.predicate.test(pos.immutable())) {
               count++;
            }
         }
         return count;
      }
   }

   public static class PatternTerrainHeight extends OilGenStructure {
      private final boolean[][] pattern;
      private final int depth;

      private PatternTerrainHeight(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
         super(containingBox, replaceType);
         this.pattern = pattern;
         this.depth = depth;
      }

      public static PatternTerrainHeight create(BlockPos start, ReplaceType replaceType, boolean[][] pattern, int depth) {
         BlockPos min = VecUtil.replaceValue(start, Axis.Y, 1);
         BlockPos max = min.offset(pattern.length - 1, 255, pattern.length == 0 ? 0 : pattern[0].length - 1);
         return new PatternTerrainHeight(new Box(min, max), replaceType, pattern, depth);
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
            int px = x - this.box.min().getX();
            for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
               int pz = z - this.box.min().getZ();
               if (!this.pattern[px][pz]) {
                  continue;
               }
               int topY = level instanceof WorldGenLevel wg
                  ? wg.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1
                  : level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
               BlockPos upper = new BlockPos(x, topY, z);
               if (!this.canReplaceForOil(level, upper)) {
                  continue;
               }
               for (int y = 0; y < 5; y++) {
                  level.setBlock(upper.above(y), Blocks.AIR.defaultBlockState(), GEN_FLAGS);
               }
               for (int y = 0; y < this.depth; y++) {
                  this.setOilIfCanReplace(level, upper.below(y));
               }
            }
         }
      }

      @Override
      protected int countOilBlocks() {
         int count = 0;
         for (int x = 0; x < this.pattern.length; x++) {
            for (int z = 0; z < this.pattern[x].length; z++) {
               if (this.pattern[x][z]) {
                  count++;
               }
            }
         }
         return count * this.depth;
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
         BlockPos worldTop = new BlockPos(this.start.getX(), topY, this.start.getZ());
         for (int y = topY; y >= this.start.getY(); y--) {
            worldTop = new BlockPos(this.start.getX(), y, this.start.getZ());
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

         OilGenStructure tubeY = OilGenerator.createTube(this.start, worldTop.getY() - this.start.getY(), this.radius, Axis.Y);
         tubeY.generate(level, tubeY.box);
         this.count += tubeY.countOilBlocks();
         BlockPos base = worldTop;
         for (int r = this.radius; r >= 0; r--) {
            OilGenStructure struct = OilGenerator.createTube(base, this.height, r, Axis.Y);
            struct.generate(level, struct.box);
            base = base.offset(0, this.height, 0);
            this.count += struct.countOilBlocks();
         }
      }

      @Override
      protected int countOilBlocks() {
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
      protected int countOilBlocks() {
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
         for (int y = minY; y <= scanMax; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
               return pos;
            }
         }
         return null;
      }
   }
}
