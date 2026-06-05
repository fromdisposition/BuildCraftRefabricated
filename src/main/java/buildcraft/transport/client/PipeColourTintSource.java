/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportItems;

public final class PipeColourTintSource implements ItemTintSource {
    public static final PipeColourTintSource INSTANCE = new PipeColourTintSource();
    public static final MapCodec<PipeColourTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);

    private PipeColourTintSource() {}

    private static final int OVERLAY_ALPHA = 76;

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        DyeColor col = stack.get(BCTransportItems.PIPE_COLOUR.get());
        if (col != null) {
            return (OVERLAY_ALPHA << 24) | ColourUtil.getLightHex(col);
        }
        return (OVERLAY_ALPHA << 24) | 0xFFFFFF;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
