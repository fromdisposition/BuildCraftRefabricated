/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileFloodGate;

@SuppressWarnings("this-escape")
public class BlockFloodGate extends BaseEntityBlock {
    public static final MapCodec<BlockFloodGate> CODEC =
            simpleCodec(BlockFloodGate::new);

    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP;

    static {
        CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);
        CONNECTED_MAP.remove(Direction.UP);
    }

    public BlockFloodGate(Properties properties) {
        super(properties);

        BlockState defaultState = this.stateDefinition.any();
        for (Property<Boolean> prop : CONNECTED_MAP.values()) {
            defaultState = defaultState.setValue(prop, true);
        }
        this.registerDefaultState(defaultState);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        CONNECTED_MAP.values().forEach(builder::add);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileFloodGate(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof TileFloodGate floodGate) {
            floodGate.onPlacedBy(placer);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, BCFactoryBlockEntities.FLOOD_GATE,
                (lvl, pos, st, tile) -> tile.serverTick());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof IToolWrench wrench)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        Direction side = hitResult.getDirection();
        if (side == Direction.UP || !CONNECTED_MAP.containsKey(side)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileFloodGate floodGate) {

                boolean nowOpen;
                if (!floodGate.openSides.remove(side)) {
                    floodGate.openSides.add(side);
                    nowOpen = true;
                } else {
                    nowOpen = false;
                }

                floodGate.onSidesToggled();

                BlockState newState = state;
                for (Map.Entry<Direction, Property<Boolean>> entry : CONNECTED_MAP.entrySet()) {
                    newState = newState.setValue(entry.getValue(),
                            floodGate.openSides.contains(entry.getKey()));
                }

                level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
                floodGate.setChanged();

                level.playSound(null, pos,
                        nowOpen ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                        SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            }
        }

        wrench.wrenchUsed(player, hand, stack, hitResult);
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileFloodGate floodGate) {
            buildcraft.lib.misc.BlockDropsUtil.dropFluidShards(level, pos, floodGate.getTank());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
