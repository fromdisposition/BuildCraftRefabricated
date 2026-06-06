package buildcraft.fabric.fluid;

import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

final class BcLiquidFluidPhysics {
   private BcLiquidFluidPhysics() {
   }

   static void tickBeforeVanilla(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos) {
      BcFluidWorldProperties props = host.holder().props;
      if (props.displacesWater() || props.denseFluid()) {
         BlockPos below = pos.below();
         if (level.getFluidState(below).is(FluidTags.WATER)) {
            level.setBlockAndUpdate(below, Blocks.AIR.defaultBlockState());
         }
      }
   }

   static void tickAfterVanilla(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
      BcFluidWorldProperties props = host.holder().props;
      if (!props.denseFluid() && !props.displacesWater()) {
         if (!fluidState.isSource()) {
            FluidState belowFluid = level.getFluidState(pos.below());
            if (belowFluid.is(FluidTags.WATER) && !belowFluid.getType().isSame(host.self())) {
               FluidState currentFluid = state.getFluidState();
               if (!currentFluid.isEmpty() && currentFluid.getType().isSame(host.self())) {
                  int neighborAmount = currentFluid.getAmount() - host.getDropOff(level);
                  if ((Boolean)currentFluid.getValue(FlowingFluid.FALLING)) {
                     neighborAmount = 7;
                  }

                  if (neighborAmount > 0) {
                     for (Entry<Direction, FluidState> entry : host.getSpread(level, pos, state).entrySet()) {
                        Direction dir = entry.getKey();
                        BlockPos targetPos = pos.relative(dir);
                        host.spreadTo(level, targetPos, level.getBlockState(targetPos), dir, entry.getValue());
                     }
                  }
               }
            }
         }
      }
   }

   static Map<Direction, FluidState> enhanceSpread(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state, Map<Direction, FluidState> map) {
      BcFluidWorldProperties props = host.holder().props;
      boolean waterBelow = level.getFluidState(pos.below()).is(FluidTags.WATER);
      if (!props.floatsOnWater() && !props.displacesWater() && !props.denseFluid() && !waterBelow) {
         return map;
      }

      if (props.floatsOnWater() && waterBelow) {
         map.remove(Direction.DOWN);
      }

      if (props.floatsOnWater() || props.denseFluid() || waterBelow) {
         FlowingFluid self = host.self();

         for (Direction dir : Plane.HORIZONTAL) {
            if (!map.containsKey(dir)) {
               BlockPos targetPos = pos.relative(dir);
               BlockState targetState = level.getBlockState(targetPos);
               if (props.floatsOnWater() && (targetState.isAir() || targetState.canBeReplaced()) && level.getFluidState(targetPos.below()).is(FluidTags.WATER)) {
                  FluidState surfaceFluid = host.getNewLiquid(level, targetPos, targetState);
                  if (!surfaceFluid.isEmpty()) {
                     map.put(dir, surfaceFluid);
                     continue;
                  }
               }

               if (canSpreadTo(level, targetPos, targetState, props)) {
                  FluidState targetFluidState = targetState.getFluidState();
                  if (!targetFluidState.getType().isSame(self)) {
                     FluidState newFluid = host.getNewLiquid(level, targetPos, targetState);
                     if (!newFluid.isEmpty() && targetFluidState.canBeReplacedWith(level, targetPos, newFluid.getType(), dir)) {
                        map.put(dir, newFluid);
                     }
                  }
               }
            }
         }
      }

      if (props.displacesWater()) {
         for (Direction dir : Direction.values()) {
            if (!map.containsKey(dir)) {
               BlockPos targetPos = pos.relative(dir);
               if (level.getFluidState(targetPos).is(FluidTags.WATER)) {
                  FluidState newFluid = host.getNewLiquid(level, targetPos, level.getBlockState(targetPos));
                  if (!newFluid.isEmpty()) {
                     map.put(dir, newFluid);
                  }
               }
            }
         }
      }

      return map;
   }

   static boolean displacesWaterAt(BcFluidPhysicsHost host, BlockState state, LevelAccessor level, BlockPos pos, FluidState target) {
      if (host.holder().props.displacesWater() && state.getFluidState().is(FluidTags.WATER)) {
         level.setBlock(pos, target.createLegacyBlock(), 3);
         return true;
      } else {
         return false;
      }
   }

   private static boolean canSpreadTo(ServerLevel level, BlockPos targetPos, BlockState targetState, BcFluidWorldProperties props) {
      return !targetState.isAir() && !targetState.canBeReplaced() ? props.floatsOnWater() && level.getFluidState(targetPos.below()).is(FluidTags.WATER) : true;
   }
}
