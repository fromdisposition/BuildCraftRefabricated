/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.Map;
import java.util.Objects;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.transaction.SnapshotJournal;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class ComposterWrapper extends SnapshotJournal<BlockState> {

    private record WrapperLocation(Level level, BlockPos pos) {
        public BlockState getBlockState() {
            return level.getBlockState(pos);
        }
    }

    private static final Map<WrapperLocation, ComposterWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakKeys().weakValues().makeMap();

    @Nullable
    public static ResourceHandler<ItemResource> get(Level level, BlockPos pos, @Nullable Direction direction) {

        if (direction == null || !direction.getAxis().isVertical()) return null;

        WrapperLocation location = new WrapperLocation(level, pos.immutable());
        ComposterWrapper wrapper = wrappers.computeIfAbsent(location, ComposterWrapper::new);
        return direction == Direction.UP ? wrapper.topHandler : wrapper.bottomHandler;
    }

    private static final ItemResource BONE_MEAL = ItemResource.of(Items.BONE_MEAL);

    private final WrapperLocation location;
    private final TransactionalRandom transactionalRandom = new TransactionalRandom();
    private final ResourceHandler<ItemResource> topHandler = new Top();
    private final ResourceHandler<ItemResource> bottomHandler = new Bottom();

    private ComposterWrapper(WrapperLocation location) {
        this.location = location;
    }

    @Override
    protected BlockState createSnapshot() {
        return location.getBlockState();
    }

    @Override
    protected void revertToSnapshot(BlockState snapshot) {
        location.level.setBlock(location.pos, snapshot, 0);
    }

    @Override
    protected void onRootCommit(BlockState originalState) {
        BlockState currentState = location.getBlockState();

        if (!currentState.is(Blocks.COMPOSTER)) return;

        if (originalState != currentState) {

            location.level.setBlock(location.pos, originalState, 0);

            location.level.setBlockAndUpdate(location.pos, currentState);
            location.level.gameEvent(GameEvent.BLOCK_CHANGE, location.pos, GameEvent.Context.of(currentState));
        }

        int originalLevel = originalState.getValue(ComposterBlock.LEVEL);
        int currentLevel = currentState.getValue(ComposterBlock.LEVEL);

        if (originalLevel < ComposterBlock.MAX_LEVEL) {
            if (currentLevel == ComposterBlock.MAX_LEVEL) {
                location.level.scheduleTick(location.pos, currentState.getBlock(), SharedConstants.TICKS_PER_SECOND);
            }
            location.level.levelEvent(LevelEvent.COMPOSTER_FILL, location.pos, currentLevel > originalLevel ? 1 : 0);
        }
    }

    private static float getComposterValue(ItemResource resource) {
        return buildcraft.lib.fabric.Mc26Compat.composterValue(resource.toStack());
    }

    private void setLevel(BlockState state, int newLevel) {
        BlockState newState = state.setValue(ComposterBlock.LEVEL, newLevel);
        location.level.setBlock(location.pos, newState, 0);
    }

    private class Top implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(index, size());
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            if (amount < 1) return 0;

            BlockState state = location.getBlockState();
            int currentLevel = state.getValue(ComposterBlock.LEVEL);
            if (currentLevel >= ComposterBlock.MAX_LEVEL) return 0;

            float value = getComposterValue(resource);
            if (value <= 0) return 0;

            updateSnapshots(transaction);

            if (currentLevel == ComposterBlock.MIN_LEVEL || transactionalRandom.nextDouble(transaction) < value) {
                setLevel(state, currentLevel + 1);
            }

            return 1;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return resource.isEmpty() || getComposterValue(resource) > 0 ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return getComposterValue(resource) > 0;
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/top]";
        }
    }

    private class Bottom implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(index, size());
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            if (amount < 1) return 0;

            if (!BONE_MEAL.equals(resource)) return 0;

            BlockState state = location.getBlockState();
            if (state.getValue(ComposterBlock.LEVEL) != ComposterBlock.READY) return 0;

            updateSnapshots(transaction);
            setLevel(state, ComposterBlock.MIN_LEVEL);
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            Objects.checkIndex(index, size());
            return location.getBlockState().getValue(ComposterBlock.LEVEL) == ComposterBlock.READY ? BONE_MEAL : ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return getResource(index).equals(BONE_MEAL) ? 1 : 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return resource.isEmpty() || BONE_MEAL.equals(resource) ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return BONE_MEAL.equals(resource);
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/bottom]";
        }
    }
}
