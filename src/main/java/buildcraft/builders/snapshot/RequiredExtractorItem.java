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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RequiredExtractorItem extends RequiredExtractor {
    private NbtPath path = null;

    private static ItemStack parseItemStack(CompoundTag ct) {
        DataResult<ItemStack> result = ItemStack.CODEC.parse(NbtOps.INSTANCE, ct);
        return result.resultOrPartial().orElse(ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
        if (tileNbt == null || path == null) return Collections.emptyList();
        Tag result = path.get(tileNbt);
        if (result instanceof CompoundTag ct) {
            ItemStack stack = parseItemStack(ct);
            if (!stack.isEmpty()) {
                return Collections.singletonList(stack);
            }
        }
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromEntity(@Nonnull CompoundTag entityNbt) {
        if (path == null) return Collections.emptyList();
        Tag result = path.get(entityNbt);
        if (result instanceof CompoundTag ct) {
            ItemStack stack = parseItemStack(ct);
            if (!stack.isEmpty()) {
                return Collections.singletonList(stack);
            }
        }
        return Collections.emptyList();
    }
}
