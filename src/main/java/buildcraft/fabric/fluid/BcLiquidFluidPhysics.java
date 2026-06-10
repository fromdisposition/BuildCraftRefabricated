package buildcraft.fabric.fluid;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

final class BcLiquidFluidPhysics {
   private BcLiquidFluidPhysics() {
   }

   static void tickBeforeVanilla(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos) {
      BcFluidWorldProperties props = host.holder().props;
      if (props.displacesWater() || props.denseFluid()) {
         BlockPos below = pos.below();
         if (BcFluidUtil.isVanillaWater(level.getFluidState(below))) {
            level.setBlockAndUpdate(below, Blocks.AIR.defaultBlockState());
         }
      }
   }

   static Map<Direction, FluidState> enhanceSpread(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state, Map<Direction, FluidState> map) {
      BcFluidWorldProperties props = host.holder().props;
      boolean waterBelow = BcFluidUtil.isVanillaWater(level.getFluidState(pos.below()));
      if (!props.floatsOnWater() && !props.displacesWater() && !props.denseFluid() && !waterBelow) {
         return map;
      }

      if (props.floatsOnWater()) {
         if (waterBelow) {
            map.remove(Direction.DOWN);
            stripSpreadIntoVanillaWater(level, pos, map);
            for (Direction dir : Plane.HORIZONTAL) {
               if (!map.containsKey(dir)) {
                  tryAddOceanSurfaceSpread(host, level, pos.relative(dir), map, dir);
               }
            }
         } else {
            stripSpreadIntoVanillaWater(level, pos, map);
         }
      }

      if (props.displacesWater()) {
         for (Direction dir : Direction.values()) {
            if (!map.containsKey(dir)) {
               BlockPos targetPos = pos.relative(dir);
               if (BcFluidUtil.isVanillaWater(level.getFluidState(targetPos))) {
                  FluidState newFluid = host.getNewLiquid(level, targetPos, level.getBlockState(targetPos));
                  if (!newFluid.isEmpty()) {
                     map.put(dir, newFluid);
                  }
               }
            }
         }
      } else {
         stripSpreadIntoVanillaWater(level, pos, map);
      }

      return map;
   }

   static boolean displacesWaterAt(BcFluidPhysicsHost host, BlockState state, LevelAccessor level, BlockPos pos, FluidState target) {
      if (host.holder().props.displacesWater() && BcFluidUtil.isVanillaWater(state.getFluidState())) {
         level.setBlock(pos, target.createLegacyBlock(), 3);
         return true;
      } else {
         return false;
      }
   }

   static boolean blocksSpreadInto(BlockState state, FluidState fluidState) {
      return BcFluidUtil.isVanillaWater(fluidState);
   }

   private static void stripSpreadIntoVanillaWater(ServerLevel level, BlockPos pos, Map<Direction, FluidState> map) {
      map.entrySet().removeIf(entry -> BcFluidUtil.isVanillaWater(level.getFluidState(pos.relative(entry.getKey()))));
   }

   /** Ocean film only — land uses vanilla FlowingFluid spread (BC 8.0). */
   private static void tryAddOceanSurfaceSpread(
      BcFluidPhysicsHost host, ServerLevel level, BlockPos targetPos, Map<Direction, FluidState> map, Direction dir
   ) {
      FluidState targetFluid = level.getFluidState(targetPos);
      if (BcFluidUtil.isVanillaWater(targetFluid)) {
         return;
      }

      BlockState targetState = level.getBlockState(targetPos);
      if (!targetState.isAir() && !targetState.canBeReplaced()) {
         return;
      }

      if (!hasVanillaWaterBelow(level, targetPos)) {
         return;
      }

      FluidState surfaceFluid = host.getNewLiquid(level, targetPos, targetState);
      if (!surfaceFluid.isEmpty()) {
         map.put(dir, surfaceFluid);
      }
   }

   private static boolean hasVanillaWaterBelow(LevelReader level, BlockPos targetPos) {
      for (int y = targetPos.getY() - 1; y >= level.getMinY(); y--) {
         BlockPos scan = new BlockPos(targetPos.getX(), y, targetPos.getZ());
         FluidState fluid = level.getFluidState(scan);
         if (BcFluidUtil.isVanillaWater(fluid)) {
            return true;
         }

         if (fluid.isEmpty()) {
            return false;
         }
      }

      return false;
   }
}
