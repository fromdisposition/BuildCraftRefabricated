/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

import buildcraft.lib.block.BlockMarkerBase;
import buildcraft.core.tile.TileMarkerVolume;

public class BlockMarkerVolume extends BlockMarkerBase {
    public BlockMarkerVolume(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity createTileEntity(BlockPos pos, BlockState state) {
        return new TileMarkerVolume(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (!world.isClientSide()) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerVolume volume) {
                volume.onManualConnectionAttempt(player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean isMoving) {
        checkSignalState(world, pos);
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return true;
    }

    private static void checkSignalState(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            return;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerVolume volume) {
            boolean powered = world.hasNeighborSignal(pos);
            if (volume.isShowingSignals() != powered) {
                volume.switchSignals();
            }
        }
    }
}
