/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.template.TemplateApi;

public class TemplateBuilder extends SnapshotBuilder<ITileForTemplateBuilder> {
    public TemplateBuilder(ITileForTemplateBuilder tile) {
        super(tile);
    }

    @Override
    protected Template.BuildingInfo getBuildingInfo() {
        return tile.getTemplateBuildingInfo();
    }

    @Override
    protected boolean isAir(BlockPos blockPos) {
        return !getBuildingInfo().box.contains(blockPos) ||
            !getBuildingInfo().getSnapshot().data.get(
                getBuildingInfo().getSnapshot().posToIndex(
                    getBuildingInfo().fromWorld(blockPos)
                )
            );
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return isFillableSlot(tile.getWorldBC(), blockPos);
    }

    static boolean isFillableSlot(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return true;
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    @Override
    protected boolean isReadyToPlace(BlockPos blockPos) {
        return true;
    }

    @Override
    protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
        return !tile.getInvResources().extract(null, 1, 1, true).isEmpty();
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return Collections.singletonList(tile.getInvResources().extract(null, 1, 1, false));
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        if (placeTask.items == null || placeTask.items.isEmpty()) {
            return false;
        }
        net.minecraft.world.entity.player.Player fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(
            (ServerLevel) tile.getWorldBC(),
            tile.getOwner(),
            tile.getBuilderPos()
        );
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, placeTask.items.get(0));
        return TemplateApi.templateRegistry.handle(
            tile.getWorldBC(),
            placeTask.pos,
            fakePlayer,
            placeTask.items.get(0)
        );
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        super.cancelPlaceTask(placeTask);

        if (tile.getWorldBC() != null && !tile.getWorldBC().isClientSide()) {
            if (placeTask.items != null && !placeTask.items.isEmpty()) {
                tile.getInvResources().insert(placeTask.items.get(0), false, false);
            }
        }
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        return !isAir(blockPos) && !isFillableSlot(tile.getWorldBC(), blockPos);
    }
}
