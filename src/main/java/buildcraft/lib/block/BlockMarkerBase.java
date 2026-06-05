/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.AABB;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.tile.TileMarker;

@SuppressWarnings("this-escape")
public abstract class BlockMarkerBase extends Block implements EntityBlock {
    private static final Map<Direction, VoxelShape> BOUNDING_BOXES = new EnumMap<>(Direction.class);

    static {
        double halfWidth = 0.1;
        double h = 0.65;

        final double nw = 0.5 - halfWidth;
        final double pw = 0.5 + halfWidth;
        final double ih = 1 - h;
        BOUNDING_BOXES.put(Direction.DOWN, Shapes.create(new AABB(nw, ih, nw, pw, 1, pw)));
        BOUNDING_BOXES.put(Direction.UP, Shapes.create(new AABB(nw, 0, nw, pw, h, pw)));
        BOUNDING_BOXES.put(Direction.SOUTH, Shapes.create(new AABB(nw, nw, 0, pw, pw, h)));
        BOUNDING_BOXES.put(Direction.NORTH, Shapes.create(new AABB(nw, nw, ih, pw, pw, 1)));
        BOUNDING_BOXES.put(Direction.EAST, Shapes.create(new AABB(0, nw, nw, h, pw, pw)));
        BOUNDING_BOXES.put(Direction.WEST, Shapes.create(new AABB(ih, nw, nw, 1, pw, pw)));
    }

    public BlockMarkerBase(Properties properties) {
        super(properties);

        BlockState defaultState = defaultBlockState();
        defaultState = defaultState.setValue(BuildCraftProperties.BLOCK_FACING_6, Direction.UP);
        defaultState = defaultState.setValue(BuildCraftProperties.ACTIVE, false);
        registerDefaultState(defaultState);
    }

    @Override
    protected void createBlockStateDefinition(
            net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BuildCraftProperties.BLOCK_FACING_6, BuildCraftProperties.ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext ctx) {
        Direction direction = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        return BOUNDING_BOXES.getOrDefault(direction, Shapes.block());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getClickedFace();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = defaultBlockState();
        return defaultBlockState().setValue(BuildCraftProperties.BLOCK_FACING_6, facing);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction sideOn = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        BlockPos neighborPos = pos.relative(sideOn.getOpposite());
        return level.getBlockState(neighborPos).isFaceSturdy(level, neighborPos, sideOn);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            scheduledTickAccess.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, false);
            net.minecraft.world.level.block.Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(this.asItem()));
        }
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(BuildCraftProperties.BLOCK_FACING_6,
                rot.rotate(state.getValue(BuildCraftProperties.BLOCK_FACING_6)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return createTileEntity(pos, state);
    }

    public abstract BlockEntity createTileEntity(BlockPos pos, BlockState state);

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileMarker<?> marker) {
                marker.onPlacedBy(placer, stack);
            }
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.singletonList(new ItemStack(this.asItem()));
    }
}
