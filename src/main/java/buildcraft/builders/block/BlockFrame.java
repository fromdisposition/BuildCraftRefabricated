/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.api.properties.BuildCraftProperties;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockFrame extends Block {
   public static final MapCodec<BlockFrame> CODEC = simpleCodec(BlockFrame::new);
   public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;
   private static final VoxelShape BASE_SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
   private static final Map<Direction, VoxelShape> CONNECTION_SHAPES = Map.of(
      Direction.DOWN,
      Block.box(4.0, 0.0, 4.0, 12.0, 4.0, 12.0),
      Direction.UP,
      Block.box(4.0, 12.0, 4.0, 12.0, 16.0, 12.0),
      Direction.NORTH,
      Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 4.0),
      Direction.SOUTH,
      Block.box(4.0, 4.0, 12.0, 12.0, 12.0, 16.0),
      Direction.WEST,
      Block.box(0.0, 4.0, 4.0, 4.0, 12.0, 12.0),
      Direction.EAST,
      Block.box(12.0, 4.0, 4.0, 16.0, 12.0, 12.0)
   );

   public BlockFrame(Properties properties) {
      super(properties);
      BlockState defaultState = (BlockState)this.stateDefinition.any();

      for (Property<Boolean> prop : CONNECTED_MAP.values()) {
         defaultState = (BlockState)defaultState.setValue(prop, false);
      }

      this.registerDefaultState(defaultState);
   }

   protected MapCodec<? extends Block> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      for (Property<Boolean> prop : CONNECTED_MAP.values()) {
         builder.add(prop);
      }
   }

   private boolean canConnectTo(BlockGetter level, BlockPos pos) {
      Block block = level.getBlockState(pos).getBlock();
      return block instanceof BlockFrame || block instanceof BlockQuarry;
   }

   private BlockState computeConnections(BlockGetter level, BlockPos pos, BlockState state) {
      for (Entry<Direction, Property<Boolean>> entry : CONNECTED_MAP.entrySet()) {
         state = (BlockState)state.setValue(entry.getValue(), this.canConnectTo(level, pos.relative(entry.getKey())));
      }

      return state;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.computeConnections(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
   }

   protected BlockState updateShape(
      BlockState state,
      LevelReader level,
      ScheduledTickAccess scheduledTickAccess,
      BlockPos pos,
      Direction direction,
      BlockPos neighborPos,
      BlockState neighborState,
      RandomSource random
   ) {
      Property<Boolean> prop = CONNECTED_MAP.get(direction);
      if (prop != null) {
         state = (BlockState)state.setValue(prop, this.canConnectTo(level, neighborPos));
      }

      return state;
   }

   protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
      super.onPlace(state, level, pos, oldState, movedByPiston);
      if (!level.isClientSide() && !oldState.is(this)) {
         BlockState newState = this.computeConnections(level, pos, state);
         if (newState != state) {
            level.setBlock(pos, newState, 3);
         }
      }
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      VoxelShape shape = BASE_SHAPE;

      for (Entry<Direction, VoxelShape> entry : CONNECTION_SHAPES.entrySet()) {
         Property<Boolean> prop = CONNECTED_MAP.get(entry.getKey());
         if ((Boolean)state.getValue(prop)) {
            shape = Shapes.join(shape, entry.getValue(), BooleanOp.OR);
         }
      }

      return shape;
   }

   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.getShape(state, level, pos, context);
   }

   protected boolean isPathfindable(BlockState state, PathComputationType type) {
      return false;
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }
}
