/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.api.properties.BuildCraftProperties;

@SuppressWarnings("this-escape")
public class BlockFrame extends Block {
    public static final MapCodec<BlockFrame> CODEC = simpleCodec(BlockFrame::new);

    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    private static final VoxelShape BASE_SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    private static final Map<Direction, VoxelShape> CONNECTION_SHAPES = Map.of(
            Direction.DOWN, Block.box(4, 0, 4, 12, 4, 12),
            Direction.UP, Block.box(4, 12, 4, 12, 16, 12),
            Direction.NORTH, Block.box(4, 4, 0, 12, 12, 4),
            Direction.SOUTH, Block.box(4, 4, 12, 12, 12, 16),
            Direction.WEST, Block.box(0, 4, 4, 4, 12, 12),
            Direction.EAST, Block.box(12, 4, 4, 16, 12, 12)
    );

    public BlockFrame(Properties properties) {
        super(properties);
        BlockState defaultState = stateDefinition.any();
        for (Property<Boolean> prop : CONNECTED_MAP.values()) {
            defaultState = defaultState.setValue(prop, false);
        }
        registerDefaultState(defaultState);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        CONNECTED_MAP.values().forEach(builder::add);
    }

    private boolean canConnectTo(BlockGetter level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block instanceof BlockFrame || block instanceof BlockQuarry;
    }

    private BlockState computeConnections(BlockGetter level, BlockPos pos, BlockState state) {
        for (Map.Entry<Direction, Property<Boolean>> entry : CONNECTED_MAP.entrySet()) {
            state = state.setValue(entry.getValue(), canConnectTo(level, pos.relative(entry.getKey())));
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return computeConnections(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level,
            ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        Property<Boolean> prop = CONNECTED_MAP.get(direction);
        if (prop != null) {
            state = state.setValue(prop, canConnectTo(level, neighborPos));
        }
        return state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this)) {
            BlockState newState = computeConnections(level, pos, state);
            if (newState != state) {
                level.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = BASE_SHAPE;
        for (Map.Entry<Direction, VoxelShape> entry : CONNECTION_SHAPES.entrySet()) {
            Property<Boolean> prop = CONNECTED_MAP.get(entry.getKey());
            if (state.getValue(prop)) {
                shape = Shapes.join(shape, entry.getValue(), BooleanOp.OR);
            }
        }
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

}
