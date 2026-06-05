/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.fluid;

import com.google.common.collect.MapMaker;
import com.google.common.math.IntMath;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import buildcraft.lib.fluids.CauldronFluidContent;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.item.VanillaContainerWrapper;
import buildcraft.lib.transfer.transaction.SnapshotJournal;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CauldronWrapper extends SnapshotJournal<BlockState> implements ResourceHandler<FluidResource> {

    private record WrapperLocation(Level level, BlockPos pos) {
        public BlockState getBlockState() {
            return level.getBlockState(pos);
        }
    }

    private static final Map<WrapperLocation, CauldronWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakKeys().weakValues().makeMap();

    public static CauldronWrapper get(Level level, BlockPos pos) {
        WrapperLocation location = new WrapperLocation(level, pos.immutable());
        return wrappers.computeIfAbsent(location, CauldronWrapper::new);
    }

    private final WrapperLocation location;

    private CauldronWrapper(WrapperLocation location) {
        this.location = location;
    }

    private CauldronFluidContent getContent(BlockState state) {
        CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
        if (content == null) {
            throw new IllegalStateException("Unexpected error: no cauldron at location " + location.pos + " in " + location.level.dimension().identifier());
        }
        return content;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());

        BlockState state = location.getBlockState();
        return FluidResource.of(getContent(state).fluid);
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());

        BlockState state = location.getBlockState();
        CauldronFluidContent content = getContent(state);

        return (long) content.totalAmount * content.currentLevel(state) / content.maxLevel;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, size());

        CauldronFluidContent fluidContent = CauldronFluidContent.getForFluid(resource.getFluid());
        return fluidContent == null ? 0 : fluidContent.totalAmount;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmpty(resource);

        return resource.isComponentsPatchEmpty() && CauldronFluidContent.getForFluid(resource.getFluid()) != null;
    }

    private void setLevel(CauldronFluidContent newContent, int fluidLevel, TransactionContext transaction) {
        updateSnapshots(transaction);

        if (fluidLevel == 0) {

            this.location.level.setBlock(location.pos, Blocks.CAULDRON.defaultBlockState(), 0);
        } else {
            BlockState newState = newContent.block.defaultBlockState();

            if (newContent.levelProperty != null) {
                newState = newState.setValue(newContent.levelProperty, fluidLevel);
            }

            this.location.level.setBlock(location.pos, newState, 0);
        }
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        if (!resource.isComponentsPatchEmpty()) {

            return 0;
        }
        CauldronFluidContent insertContent = CauldronFluidContent.getForFluid(resource.getFluid());
        if (insertContent == null) {
            return 0;
        }

        BlockState state = location.getBlockState();
        CauldronFluidContent currentContent = getContent(state);
        if (currentContent.fluid != Fluids.EMPTY && !resource.is(currentContent.fluid)) {

            return 0;
        }

        int d = IntMath.gcd(insertContent.maxLevel, insertContent.totalAmount);
        int amountIncrements = insertContent.totalAmount / d;
        int levelIncrements = insertContent.maxLevel / d;

        int currentLevel = currentContent.currentLevel(state);
        int insertedIncrements = Math.min(amount / amountIncrements, (insertContent.maxLevel - currentLevel) / levelIncrements);

        if (insertedIncrements > 0) {
            setLevel(insertContent, currentLevel + insertedIncrements * levelIncrements, transaction);
        }

        return insertedIncrements * amountIncrements;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        BlockState state = location.getBlockState();
        CauldronFluidContent currentContent = getContent(state);

        if (!resource.is(currentContent.fluid) || !resource.isComponentsPatchEmpty()) {
            return 0;
        }

        int d = IntMath.gcd(currentContent.maxLevel, currentContent.totalAmount);
        int levelIncrements = currentContent.maxLevel / d;
        int amountIncrements = currentContent.totalAmount / d;

        int currentLevel = currentContent.currentLevel(state);
        int extractedIncrements = Math.min(amount / amountIncrements, currentLevel / levelIncrements);

        if (extractedIncrements > 0) {
            setLevel(currentContent, currentLevel - extractedIncrements * levelIncrements, transaction);
        }

        return extractedIncrements * amountIncrements;
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
        BlockState state = location.getBlockState();

        if (originalState == state || CauldronFluidContent.getForBlock(state.getBlock()) == null) return;

        location.level.setBlock(location.pos, originalState, 0);

        location.level.setBlockAndUpdate(location.pos, state);

    }
}
