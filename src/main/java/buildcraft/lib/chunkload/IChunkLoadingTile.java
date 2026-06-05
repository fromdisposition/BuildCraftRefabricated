/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.chunkload;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public interface IChunkLoadingTile {

    @Nullable
    default LoadType getLoadType() {
        return LoadType.SOFT;
    }

    @Nullable
    default Set<ChunkPos> getChunksToLoad() {
        BlockPos pos = ((BlockEntity) this).getBlockPos();
        Set<ChunkPos> chunkPoses = new HashSet<>(4);
        for (Direction face : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST }) {
            chunkPoses.add(buildcraft.lib.misc.PositionUtil.chunkContaining(pos.relative(face)));
        }
        return chunkPoses;
    }

    public enum LoadType {

        SOFT,

        HARD
    }
}
