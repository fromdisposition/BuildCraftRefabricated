/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;

public class SchematicBlockFluid implements ISchematicBlock {
    private BlockState blockState;
    private boolean isFlowing;

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {

        return !context.blockState.getFluidState().isEmpty();
    }

    @Override
    public void init(SchematicBlockContext context) {
        blockState = context.blockState;

        isFlowing = !context.blockState.getFluidState().isEmpty() &&
                    !context.blockState.getFluidState().isSource();
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return Stream.concat(
                Arrays.stream(Direction.values()).filter(d -> d.getAxis().isHorizontal()),
                Stream.of(Direction.DOWN)
            )
            .map(Direction::getUnitVec3i)
            .map(BlockPos::new)
            .collect(Collectors.toSet());
    }

    @Nullable
    @Override
    public BlockState getBlockStateForRender() {
        return blockState;
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids() {
        if (!isFlowing && !blockState.getFluidState().isEmpty()) {
            return Collections.singletonList(
                new FluidStack(blockState.getFluidState().getType(), 1000)
            );
        }
        return Collections.emptyList();
    }

    @Override
    public SchematicBlockFluid getRotated(Rotation rotation) {
        SchematicBlockFluid schematicBlock = SchematicBlockManager.createCleanCopy(this);
        schematicBlock.blockState = blockState;
        schematicBlock.isFlowing = isFlowing;
        return schematicBlock;
    }

    @Override
    public boolean canBuild(Level level, BlockPos blockPos) {
        return level.isEmptyBlock(blockPos) ||
            (!level.getFluidState(blockPos).isEmpty() &&
             !level.getFluidState(blockPos).isSource());
    }

    @Override
    public boolean build(Level level, BlockPos blockPos) {
        if (isFlowing) {
            return true;
        }
        if (level.setBlock(blockPos, blockState, 11)) {
            Stream.concat(
                Stream.of(Direction.values())
                    .map(Direction::getUnitVec3i)
                    .map(BlockPos::new),
                Stream.of(BlockPos.ZERO)
            )
                .map(blockPos::offset)
                .forEach(updatePos -> level.neighborChanged(updatePos, blockState.getBlock(), null));
            return true;
        }
        return false;
    }

    @Override
    public boolean buildWithoutChecks(Level level, BlockPos blockPos) {
        return level.setBlock(blockPos, blockState, 0);
    }

    @Override
    public boolean isBuilt(Level level, BlockPos blockPos) {
        return isFlowing || blockState.equals(level.getBlockState(blockPos));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("blockState", NbtUtils.writeBlockState(blockState));
        nbt.putBoolean("isFlowing", isFlowing);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        blockState = NbtUtils.readBlockState(
            BuiltInRegistries.BLOCK,
            nbt.getCompoundOrEmpty("blockState")
        );
        isFlowing = nbt.getBooleanOr("isFlowing", false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchematicBlockFluid that = (SchematicBlockFluid) o;
        return isFlowing == that.isFlowing && blockState.equals(that.blockState);
    }

    @Override
    public int hashCode() {
        int result = blockState.hashCode();
        result = 31 * result + (isFlowing ? 1 : 0);
        return result;
    }
}
