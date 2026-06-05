/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.engine;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.context.BlockPlaceContext;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;

@SuppressWarnings("this-escape")
public abstract class BlockEngineBase_BC8 extends Block implements EntityBlock, ICustomRotationHandler {

    public BlockEngineBase_BC8(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(defaultBlockState()
            .setValue(BuildCraftProperties.BLOCK_FACING_6, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BuildCraftProperties.BLOCK_FACING_6);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(BuildCraftProperties.BLOCK_FACING_6, ctx.getClickedFace());
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(BuildCraftProperties.BLOCK_FACING_6,
            rot.rotate(state.getValue(BuildCraftProperties.BLOCK_FACING_6)));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        return switch (facing) {
            case DOWN -> Block.box(0, 12, 0, 16, 16, 16);
            case UP -> Block.box(0, 0, 0, 16, 4, 16);
            case NORTH -> Block.box(0, 0, 12, 16, 16, 16);
            case SOUTH -> Block.box(0, 0, 0, 16, 16, 4);
            case WEST -> Block.box(12, 0, 0, 16, 16, 16);
            case EAST -> Block.box(0, 0, 0, 4, 16, 16);
        };
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {

        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileEngineBase_BC8 engine) {
                engine.onPlacedBy(placer, stack);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof TileEngineBase_BC8 engine) {
                    TileEngineBase_BC8.serverTick(lvl, pos, st, engine);
                }
            };
        } else {
            return (lvl, pos, st, be) -> {
                if (be instanceof TileEngineBase_BC8 engine) {
                    engine.clientTick();
                }
            };
        }
    }

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof TileEngineBase_BC8 engine) {
            if (engine.attemptRotation()) {
                world.setBlock(pos, state.setValue(
                        BuildCraftProperties.BLOCK_FACING_6, engine.getOrientation()), 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn,
            @Nullable Orientation orientation, boolean isMoving) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEngineBase_BC8 engine) {
            engine.onNeighborUpdate();
        }
    }
}
