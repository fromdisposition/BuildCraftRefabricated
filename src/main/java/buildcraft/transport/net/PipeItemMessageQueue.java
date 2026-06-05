/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.net;

import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import buildcraft.lib.fabric.PacketDistributor;

public class PipeItemMessageQueue {

    private static final Map<ServerPlayer, MessageMultiPipeItem> cachedPlayerPackets = new WeakHashMap<>();

    public static void serverTick() {
        for (var entry : cachedPlayerPackets.entrySet()) {
            PacketDistributor.sendToPlayer(entry.getKey(), entry.getValue());
        }
        cachedPlayerPackets.clear();
    }

    public static void appendTravellingItem(Level world, BlockPos pos, ItemStack stack, int stackCount,
            boolean toCenter, Direction side, @Nullable DyeColor colour, int timeToDest) {
        if (!(world instanceof ServerLevel server)) {
            return;
        }

        ChunkPos chunkPos = buildcraft.lib.misc.PositionUtil.chunkContaining(pos);
        var chunkMap = server.getChunkSource().chunkMap;
        var players = chunkMap.getPlayers(chunkPos, false);

        for (ServerPlayer player : players) {
            cachedPlayerPackets.computeIfAbsent(player, pl -> new MessageMultiPipeItem())
                    .append(pos, stack, stackCount, toCenter, side, colour, timeToDest);
        }
    }
}
