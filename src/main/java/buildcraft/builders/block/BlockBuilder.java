/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import buildcraft.api.enums.EnumOptionalSnapshotType;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.tile.TileBuilder;

@SuppressWarnings("this-escape")
public class BlockBuilder extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BlockBuilder> CODEC = simpleCodec(BlockBuilder::new);

    public static final EnumProperty<EnumOptionalSnapshotType> SNAPSHOT_TYPE =
        EnumProperty.create("snapshot_type", EnumOptionalSnapshotType.class);

    public BlockBuilder(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SNAPSHOT_TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileBuilder(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        if (blockEntityType != BCBuildersBlockEntities.BUILDER) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof TileBuilder builder) {
                builder.tick();
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof TileBuilder builder) {
                player.openMenu(builder);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileBuilder builder) {
            builder.onPlacedBy(placer, stack);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileBuilder builder) {
                ItemStack snapshot = builder.getSnapshot();
                if (!snapshot.isEmpty()) {
                    Block.popResource(level, pos, snapshot);
                }
                for (int i = 0; i < TileBuilder.RESOURCE_SLOTS; i++) {
                    ItemStack stack = builder.getResource(i);
                    if (!stack.isEmpty()) {
                        Block.popResource(level, pos, stack);
                    }
                }
                net.minecraft.core.NonNullList<ItemStack> fluidDrops = net.minecraft.core.NonNullList.create();
                buildcraft.api.items.FluidItemDrops.addFluidDrops(fluidDrops,
                    builder.getTank(0), builder.getTank(1), builder.getTank(2), builder.getTank(3));
                for (ItemStack drop : fluidDrops) {
                    Block.popResource(level, pos, drop);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
