/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.serialization.DataResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.fluids.FluidStack;

public class RequiredExtractorTank extends RequiredExtractor {
    private NbtPath path = null;

    private static FluidStack parseFluidStack(CompoundTag ct) {
        DataResult<FluidStack> result = FluidStack.CODEC.parse(NbtOps.INSTANCE, ct);
        return result.resultOrPartial().orElse(FluidStack.EMPTY);
    }

    @Nonnull
    @Override
    public List<FluidStack> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
        if (tileNbt == null || path == null) return Collections.emptyList();
        Tag result = path.get(tileNbt);
        if (result instanceof CompoundTag ct && !ct.contains("Empty")) {
            FluidStack stack = parseFluidStack(ct);
            if (!stack.isEmpty()) {
                return Collections.singletonList(stack);
            }
        }
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<FluidStack> extractFluidsFromEntity(@Nonnull CompoundTag entityNbt) {
        if (path == null) return Collections.emptyList();
        Tag result = path.get(entityNbt);
        if (result instanceof CompoundTag ct && !ct.contains("Empty")) {
            FluidStack stack = parseFluidStack(ct);
            if (!stack.isEmpty()) {
                return Collections.singletonList(stack);
            }
        }
        return Collections.emptyList();
    }
}
