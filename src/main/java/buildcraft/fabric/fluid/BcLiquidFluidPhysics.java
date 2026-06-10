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
import net.minecraft.world.level.material.Fluids;

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

      if (props.floatsOnWater()) {
         FluidState fluidState = level.getFluidState(pos);
         if (!fluidState.isEmpty() && fluidState.getType().isSame(host.self()) && isSubmergedUnderWater(level, pos)) {
            BlockPos film = findOilFilmPos(level, pos.getX(), pos.getZ());
            if (film != null && film.getY() > pos.getY() && canPlaceOilFilmAt(level, film)) {
               level.setBlock(pos, Fluids.WATER.defaultFluidState().createLegacyBlock(), 3);
               FluidState newFluid = host.getNewLiquid(level, film, level.getBlockState(film));
               if (!newFluid.isEmpty()) {
                  host.spreadTo(level, film, level.getBlockState(film), Direction.UP, newFluid);
               }
            }
         }
      }
   }

   static void tickAfterVanilla(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
      BcFluidWorldProperties props = host.holder().props;
      if (!props.denseFluid() && !props.displacesWater()) {
         FluidState belowFluid = level.getFluidState(pos.below());
         if (belowFluid.is(FluidTags.WATER) && !belowFluid.getType().isSame(host.self())) {
            FluidState currentFluid = state.getFluidState();
            if (!currentFluid.isEmpty() && currentFluid.getType().isSame(host.self())) {
               if (!props.floatsOnWater() && fluidState.isSource()) {
                  return;
               }

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

   static Map<Direction, FluidState> enhanceSpread(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state, Map<Direction, FluidState> map) {
      BcFluidWorldProperties props = host.holder().props;
      boolean waterBelow = level.getFluidState(pos.below()).is(FluidTags.WATER);
      if (!props.floatsOnWater() && !props.displacesWater() && !props.denseFluid() && !waterBelow) {
         return map;
      }

      if (props.floatsOnWater()) {
         if (waterBelow) {
            map.remove(Direction.DOWN);
         }

         if (isSubmergedUnderWater(level, pos) && !isOilFilmPosition(level, pos)) {
            map.clear();
            map.remove(Direction.DOWN);
            tryAddFilmSpread(host, level, pos, map, Direction.UP);
            return map;
         }
      }

      if (props.floatsOnWater() || props.denseFluid() || waterBelow) {
         for (Direction dir : Plane.HORIZONTAL) {
            if (!map.containsKey(dir)) {
               tryAddFilmSpread(host, level, pos.relative(dir), map, dir);
            }
         }

         if (props.floatsOnWater() && !map.containsKey(Direction.UP)) {
            tryAddFilmSpread(host, level, pos.above(), map, Direction.UP);
         }
      }

      if (props.displacesWater()) {
         FlowingFluid self = host.self();

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

   @javax.annotation.Nullable
   private static BlockPos findTopWater(LevelAccessor level, int x, int z) {
      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         if (level.getFluidState(pos).is(FluidTags.WATER)) {
            return pos;
         }
      }

      return null;
   }

   @javax.annotation.Nullable
   private static BlockPos findOilFilmPos(LevelAccessor level, int x, int z) {
      BlockPos topWater = findTopWater(level, x, z);
      return topWater == null ? null : topWater.above();
   }

   private static boolean canPlaceOilFilmAt(LevelAccessor level, BlockPos pos) {
      if (pos.getY() > level.getMaxY()) {
         return false;
      }

      BlockState state = level.getBlockState(pos);
      return state.isAir() || state.canBeReplaced();
   }

   private static boolean isOilFilmPosition(LevelAccessor level, BlockPos pos) {
      return canPlaceOilFilmAt(level, pos) && level.getFluidState(pos.below()).is(FluidTags.WATER);
   }

   private static boolean isSubmergedUnderWater(LevelAccessor level, BlockPos pos) {
      BlockPos film = findOilFilmPos(level, pos.getX(), pos.getZ());
      return film != null && pos.getY() < film.getY();
   }

   private static void tryAddFilmSpread(
      BcFluidPhysicsHost host, ServerLevel level, BlockPos targetPos, Map<Direction, FluidState> map, Direction dir
   ) {
      if (!canPlaceOilFilmAt(level, targetPos)) {
         return;
      }

      if (!level.getFluidState(targetPos.below()).is(FluidTags.WATER)) {
         return;
      }

      FluidState surfaceFluid = host.getNewLiquid(level, targetPos, level.getBlockState(targetPos));
      if (!surfaceFluid.isEmpty()) {
         map.put(dir, surfaceFluid);
      }
   }
}
