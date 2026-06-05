/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import buildcraft.lib.block.BlockMarkerBase;

import buildcraft.core.tile.TileMarkerPath;

public class BlockMarkerPath extends BlockMarkerBase {
    public BlockMarkerPath(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity createTileEntity(BlockPos pos, BlockState state) {
        return new TileMarkerPath(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (!world.isClientSide()) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerPath marker) {
                marker.reverseDirection();
            }
        }
        return InteractionResult.SUCCESS;
    }
}
