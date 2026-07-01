package buildcraft.fabric.fluid;

import buildcraft.lib.misc.BlockUtil;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class BcGaseousFluidPhysics {
   private static final Direction[] HORIZONTAL_SIDES = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

   private BcGaseousFluidPhysics() {
   }

   static void tick(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
      if (!fluidState.isSource()) {
         FluidState newFluidState = host.getNewLiquid(level, pos, state);
         int tickDelay = ((BcOilFluid)host).spreadDelay(level, pos, fluidState, newFluidState);
         if (newFluidState.isEmpty()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
         }

         if (newFluidState != fluidState) {
            state = newFluidState.createLegacyBlock();
            level.setBlock(pos, state, 3);
            level.scheduleTick(pos, newFluidState.getType(), tickDelay);
            fluidState = newFluidState;
         }
      }

      spread(host, level, pos, state, fluidState);
   }

   static void spread(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
      if (!fluidState.isEmpty()) {
         BlockPos abovePos = pos.above();
         BlockState aboveState = level.getBlockState(abovePos);
         FluidState aboveFluid = aboveState.getFluidState();
         if (gaseousCanMaybePassThrough(host, level, pos, state, Direction.UP, abovePos, aboveState, aboveFluid)) {
            FluidState newAboveFluid = host.getNewLiquid(level, abovePos, aboveState);
            Fluid newAboveType = newAboveFluid.getType();
            if (aboveFluid.canBeReplacedWith(level, abovePos, newAboveType, Direction.UP) && gaseousCanHoldFluid(level, abovePos, aboveState, newAboveType)) {
               host.spreadTo(level, abovePos, aboveState, Direction.UP, newAboveFluid);
               if (gaseousSourceNeighborCount(host, level, pos) >= 3) {
                  spreadToSides(host, level, pos, fluidState, host.getSpread(level, pos, state));
               }

               return;
            }
         }

         if (fluidState.isSource() || !isRiseHole(host, level, pos, state, abovePos, aboveState)) {
            spreadToSides(host, level, pos, fluidState, host.getSpread(level, pos, state));
         }
      }
   }

   static Map<Direction, FluidState> getSpread(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state) {
      Map<Direction, FluidState> map = new EnumMap<>(Direction.class);
      FluidState self = state.getFluidState();
      if (!self.isSource() && self.getAmount() < host.getDropOff(level)) {
         return map;
      }

      BlockPos above = pos.above();
      BlockState aboveState = level.getBlockState(above);
      if (canSpreadInto(host, level, above, aboveState, Direction.DOWN)) {
         FluidState spread = host.getNewLiquid(level, above, aboveState);
         if (!spread.isEmpty()) {
            map.put(Direction.UP, spread);
            return map;
         }
      }

      boolean[] flowTo = getOptimalFlowDirections(host, level, pos, state);
      int flowMeta = self.isSource() ? 1 : self.getAmount() - host.getDropOff(level) + 1;
      if (flowMeta <= 0) {
         return map;
      }

      for (int i = 0; i < HORIZONTAL_SIDES.length; i++) {
         if (flowTo[i]) {
            Direction dir = HORIZONTAL_SIDES[i];
            BlockPos offset = pos.relative(dir);
            BlockState offsetState = level.getBlockState(offset);
            if (canSpreadInto(host, level, offset, offsetState, dir.getOpposite())) {
               FluidState spread = host.getNewLiquid(level, offset, offsetState);
               if (!spread.isEmpty()) {
                  map.put(dir, spread);
               }
            }
         }
      }

      return map;
   }

   static FluidState getNewLiquid(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state) {
      int highestNeighbor = 0;
      MutableBlockPos mutablePos = new MutableBlockPos();
      FlowingFluid self = host.self();

      for (Direction direction : Plane.HORIZONTAL) {
         BlockPos relativePos = mutablePos.setWithOffset(pos, direction);
         BlockState blockState = level.getBlockState(relativePos);
         FluidState fluidState = blockState.getFluidState();
         if (fluidState.getType().isSame(self) && gaseousCanPassThroughWall(direction, level, pos, state, relativePos, blockState)) {
            highestNeighbor = Math.max(highestNeighbor, fluidState.getAmount());
         }
      }

      BlockPos belowPos = mutablePos.setWithOffset(pos, Direction.DOWN);
      BlockState belowState = level.getBlockState(belowPos);
      FluidState belowFluid = belowState.getFluidState();
      if (!belowFluid.isEmpty() && belowFluid.getType().isSame(self) && gaseousCanPassThroughWall(Direction.DOWN, level, pos, state, belowPos, belowState)) {
         return host.self().getFlowing(8, true);
      }

      int amount = highestNeighbor - host.getDropOff(level);
      return amount <= 0 ? Fluids.EMPTY.defaultFluidState() : host.self().getFlowing(amount, false);
   }

   static float getHeight(FluidState fluidState, BlockGetter level, BlockPos pos) {
      return hasSameGaseousBelow(fluidState, level, pos) ? 1.0F : 1.0F - fluidState.getOwnHeight();
   }

   static VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos, BcFluidPhysicsHost host) {
      float height = getHeight(state, level, pos);
      if (height >= 1.0F) {
         return Shapes.block();
      }

      float minY = 1.0F - height;
      return Shapes.box(0.0, minY, 0.0, 1.0, 1.0, 1.0);
   }

   static Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState fluidState) {
      double flowX = 0.0;
      double flowZ = 0.0;
      MutableBlockPos blockPos = new MutableBlockPos();

      for (Direction direction : Plane.HORIZONTAL) {
         blockPos.setWithOffset(pos, direction);
         FluidState neighbourFluid = level.getFluidState(blockPos);
         if (neighbourFluid.getType().isSame(fluidState.getType()) || neighbourFluid.isEmpty()) {
            float neighborHeight = neighbourFluid.isEmpty() ? 0.0F : neighbourFluid.getOwnHeight();
            float distance = 0.0F;
            if (neighborHeight == 0.0F) {
               if (!BlockUtil.blocksMotion(level.getBlockState(blockPos))) {
                  BlockPos aboveNeighbor = blockPos.above();
                  FluidState aboveNeighborState = level.getFluidState(aboveNeighbor);
                  if (aboveNeighborState.getType().isSame(fluidState.getType())) {
                     neighborHeight = aboveNeighborState.getOwnHeight();
                     if (neighborHeight > 0.0F) {
                        distance = fluidState.getOwnHeight() - (neighborHeight - 0.8888889F);
                     }
                  }
               }
            } else if (neighborHeight > 0.0F) {
               distance = fluidState.getOwnHeight() - neighborHeight;
            }

            if (distance != 0.0F) {
               flowX += direction.getStepX() * distance;
               flowZ += direction.getStepZ() * distance;
            }
         }
      }

      Vec3 flow = new Vec3(flowX, 0.0, flowZ);
      if ((Boolean)fluidState.getValue(FlowingFluid.FALLING)) {
         if (flow.lengthSqr() > 1.0E-8) {
            flow = flow.normalize().add(0.0, 6.0, 0.0);
         } else {
            flow = new Vec3(0.0, 6.0, 0.0);
         }
      }

      return flow.lengthSqr() > 1.0E-8 ? flow.normalize() : Vec3.ZERO;
   }

   private static void spreadToSides(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, FluidState fluidState, Map<Direction, FluidState> spread) {
      int spreadAmount = fluidState.getAmount() - host.getDropOff(level);
      if ((Boolean)fluidState.getValue(FlowingFluid.FALLING)) {
         spreadAmount = 7;
      }

      if (spreadAmount > 0) {
         for (Entry<Direction, FluidState> entry : spread.entrySet()) {
            Direction direction = entry.getKey();
            BlockPos neighborPos = pos.relative(direction);
            host.spreadTo(level, neighborPos, level.getBlockState(neighborPos), direction, entry.getValue());
         }
      }
   }

   private static boolean canSpreadInto(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state, Direction fromDir) {
      FluidState existing = state.getFluidState();
      if (!existing.isEmpty()) {
         if (!existing.getType().isSame(host.self())) {
            return false;
         }

         if (existing.isSource()) {
            return false;
         }
      } else if (!state.canBeReplaced()) {
         return false;
      }

      FluidState spread = host.getNewLiquid(level, pos, state);
      return !spread.isEmpty() && existing.canBeReplacedWith(level, pos, spread.getType(), fromDir);
   }

   private static boolean isRiseHole(
      BcFluidPhysicsHost host, BlockGetter level, BlockPos bottomPos, BlockState bottomState, BlockPos abovePos, BlockState aboveState
   ) {
      if (!gaseousCanPassThroughWall(Direction.UP, level, bottomPos, bottomState, abovePos, aboveState)) {
         return false;
      }

      FluidState aboveFluid = aboveState.getFluidState();
      return aboveFluid.getType().isSame(host.self()) || gaseousCanHoldFluid(level, abovePos, aboveState, host.self());
   }

   private static boolean[] getOptimalFlowDirections(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, BlockState state) {
      int[] flowCost = new int[4];
      int min = 1000;

      for (int side = 0; side < 4; side++) {
         flowCost[side] = 1000;
         Direction dir = HORIZONTAL_SIDES[side];
         BlockPos offset = pos.relative(dir);
         BlockState offsetState = level.getBlockState(offset);
         FluidState offsetFluid = offsetState.getFluidState();
         if (gaseousCanMaybePassThrough(host, level, pos, state, dir, offset, offsetState, offsetFluid) && !isGaseousSource(host, level, offset)) {
            BlockPos risePos = offset.above();
            BlockState riseState = level.getBlockState(risePos);
            if (gaseousCanMaybePassThrough(host, level, offset, offsetState, Direction.UP, risePos, riseState, riseState.getFluidState())) {
               flowCost[side] = 0;
            } else {
               flowCost[side] = gaseousFlowCost(host, level, offset, 1, side);
            }

            min = Math.min(min, flowCost[side]);
         }
      }

      boolean[] flowTo = new boolean[4];

      for (int side = 0; side < 4; side++) {
         flowTo[side] = flowCost[side] == min;
      }

      return flowTo;
   }

   private static int gaseousFlowCost(BcFluidPhysicsHost host, ServerLevel level, BlockPos pos, int depth, int fromSide) {
      int cost = 1000;
      int maxDepth = 8;

      for (int adjSide = 0; adjSide < 4; adjSide++) {
         if (adjSide != fromSide) {
            Direction dir = HORIZONTAL_SIDES[adjSide];
            BlockPos offset = pos.relative(dir);
            BlockState offsetState = level.getBlockState(offset);
            FluidState offsetFluid = offsetState.getFluidState();
            if (gaseousCanMaybePassThrough(host, level, pos, level.getBlockState(pos), dir, offset, offsetState, offsetFluid)
               && !isGaseousSource(host, level, offset)) {
               BlockPos risePos = offset.above();
               BlockState riseState = level.getBlockState(risePos);
               if (gaseousCanMaybePassThrough(host, level, offset, offsetState, Direction.UP, risePos, riseState, riseState.getFluidState())) {
                  return depth;
               }

               if (depth < maxDepth / 2) {
                  cost = Math.min(cost, gaseousFlowCost(host, level, offset, depth + 1, adjSide));
               }
            }
         }
      }

      return cost;
   }

   private static boolean gaseousCanMaybePassThrough(
      BcFluidPhysicsHost host,
      BlockGetter level,
      BlockPos sourcePos,
      BlockState sourceState,
      Direction direction,
      BlockPos testPos,
      BlockState testState,
      FluidState testFluidState
   ) {
      if (isGaseousSource(host, level, testPos)) {
         return false;
      } else {
         return !gaseousCanHoldAnyFluid(testState) && testFluidState.isEmpty()
            ? false
            : gaseousCanPassThroughWall(direction, level, sourcePos, sourceState, testPos, testState);
      }
   }

   private static boolean isGaseousSource(BcFluidPhysicsHost host, BlockGetter level, BlockPos pos) {
      FluidState state = level.getFluidState(pos);
      return state.getType().isSame(host.self()) && state.isSource();
   }

   private static int gaseousSourceNeighborCount(BcFluidPhysicsHost host, LevelReader level, BlockPos pos) {
      int count = 0;

      for (Direction direction : Plane.HORIZONTAL) {
         if (isGaseousSource(host, level, pos.relative(direction))) {
            count++;
         }
      }

      return count;
   }

   private static boolean gaseousCanHoldAnyFluid(BlockState state) {
      return state.getBlock() instanceof LiquidBlockContainer ? true : !BlockUtil.blocksMotion(state) && state.canBeReplaced();
   }

   private static boolean gaseousCanHoldFluid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
      if (!gaseousCanHoldAnyFluid(state)) {
         return false;
      } else {
         return state.getBlock() instanceof LiquidBlockContainer container ? container.canPlaceLiquid(null, level, pos, state, fluid) : true;
      }
   }

   private static boolean gaseousCanPassThroughWall(
      Direction direction, BlockGetter level, BlockPos sourcePos, BlockState sourceState, BlockPos targetPos, BlockState targetState
   ) {
      return BlockUtil.blocksMotion(targetState) && targetState.getFluidState().isEmpty()
         ? false
         : targetState.canBeReplaced() || !targetState.getFluidState().isEmpty();
   }

   private static boolean hasSameGaseousBelow(FluidState fluidState, BlockGetter level, BlockPos pos) {
      return fluidState.getType().isSame(level.getFluidState(pos.below()).getType());
   }
}
