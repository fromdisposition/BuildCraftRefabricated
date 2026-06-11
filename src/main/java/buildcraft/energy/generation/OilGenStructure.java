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
import buildcraft.lib.misc.data.Box;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class OilGenStructure {
   private static final int SURFACE_BOX_Y_MARGIN = 8;
   private static final int OCEAN_SPREAD_CLEAR_HEIGHT = 5;
   private static final int GEN_FLAGS = 0;

   private OilGenStructure() {
   }

   public static void setOil(LevelAccessor level, BlockPos pos) {
      BlockState oil = BCEnergyFluidsFabric.oilSourceBlockStateForLevel(level instanceof Level l ? l : null);
      if (oil == null) {
         oil = BCEnergyFluidsFabric.OIL_COOL.still().defaultFluidState().createLegacyBlock();
      }

      level.setBlock(pos, oil, GEN_FLAGS);
   }

   public static BlockPos findTerrainUpper(LevelAccessor level, int x, int z) {
      if (level instanceof WorldGenLevel wg) {
         return new BlockPos(x, wg.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
      }

      if (level instanceof LevelReader reader) {
         return new BlockPos(x, reader.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
      }

      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && BlockUtil.blocksMotion(state)) {
            return new BlockPos(x, y - 1, z);
         }
      }

      return new BlockPos(x, level.getMinY(), z);
   }

   public static Box tendrilBounds(BlockPos start, boolean[][] pattern, int depth, int surfaceY) {
      int minY = Math.max(surfaceY - SURFACE_BOX_Y_MARGIN - depth, 1);
      int maxY = surfaceY + OCEAN_SPREAD_CLEAR_HEIGHT + SURFACE_BOX_Y_MARGIN;
      int depthZ = pattern.length == 0 ? 1 : pattern[0].length;
      BlockPos min = new BlockPos(start.getX(), minY, start.getZ());
      BlockPos max = new BlockPos(start.getX() + pattern.length - 1, maxY, start.getZ() + depthZ - 1);
      return new Box(min, max);
   }

   public static final class Spring {
      private final BlockPos pos;

      public Spring(BlockPos pos) {
         this.pos = pos;
      }

      public void generate(LevelAccessor level, int count) {
         BlockPos placement = resolvePlacement(level, this.pos);
         if (placement == null) {
            BCLog.logger.warn("[energy.gen.oil] Skipping oil spring at " + this.pos + " because no bedrock was found.");
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
         if (level instanceof Level world && world.dimension() == Level.NETHER) {
            return BlockUtil.blocksMotion(level.getBlockState(column)) ? column : null;
         }

         int x = column.getX();
         int z = column.getZ();
         int scanMax = level.getMinY() + 4;

         for (int y = level.getMinY(); y <= scanMax; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
               return pos;
            }
         }

         return null;
      }
   }
}
