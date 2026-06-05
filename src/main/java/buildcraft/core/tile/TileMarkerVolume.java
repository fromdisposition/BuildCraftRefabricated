/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import buildcraft.core.BCCoreBlockEntities;

import buildcraft.api.tiles.ITileAreaProvider;

import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.tile.TileMarker;

import buildcraft.core.marker.VolumeCache;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.core.marker.VolumeSubCache;

public class TileMarkerVolume extends TileMarker<VolumeConnection> implements ITileAreaProvider {
    private static final net.minecraft.resources.Identifier ADVANCEMENT_MARKERS
        = net.minecraft.resources.Identifier.parse("buildcraftcore:markers");

    private boolean showSignals = false;

    public TileMarkerVolume(BlockPos pos, BlockState state) {
        super(BCCoreBlockEntities.MARKER_VOLUME, pos, state);
    }

    public boolean isShowingSignals() {
        return showSignals;
    }

    @Override
    public VolumeCache getCache() {
        return VolumeCache.INSTANCE;
    }

    @Override
    public boolean isActiveForRender() {
        return showSignals || getCurrentConnection() != null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("showSignals", showSignals);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        showSignals = input.getBooleanOr("showSignals", false);
    }

    public void switchSignals() {
        if (level != null && !level.isClientSide()) {
            showSignals = !showSignals;
            setChanged();
        }
    }

    @Nonnull
    public AABB getRenderBoundingBox() {
        return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public void onManualConnectionAttempt(Player player) {
        MarkerSubCache<VolumeConnection> cache = this.getLocalCache();
        boolean connected = false;
        for (BlockPos other : cache.getValidConnections(getBlockPos())) {
            if (cache.tryConnect(getBlockPos(), other)) {
                connected = true;
            }
        }
        VolumeConnection c = getCurrentConnection();
        if (c != null) {
            for (BlockPos corner : PositionUtil.getCorners(c.getBox().min(), c.getBox().max())) {
                if (!c.getMarkerPositions().contains(corner) && cache.hasLoadedOrUnloadedMarker(corner)) {
                    if (c.addMarker(corner)) {
                        connected = true;
                    }
                }
            }
        }
        if (connected && level != null && !level.isClientSide()) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_MARKERS);
        }
    }

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        if (level == null) return;
        VolumeSubCache cache = VolumeCache.INSTANCE.getSubCache(level);
        BlockPos pos = getBlockPos();
        for (BlockPos other : cache.getValidConnections(pos)) {
            VolumeConnection c = cache.getConnection(other);
            if (c != null && c.getBox().isCorner(pos)) {
                if (c.addMarker(pos)) {
                    break;
                }
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("Min = " + min());
        left.add("Max = " + max());
        left.add("Signals = " + showSignals);
    }

    @Override
    public BlockPos min() {
        VolumeConnection connection = getCurrentConnection();
        return connection == null ? getBlockPos() : connection.getBox().min();
    }

    @Override
    public BlockPos max() {
        VolumeConnection connection = getCurrentConnection();
        return connection == null ? getBlockPos() : connection.getBox().max();
    }

    @Override
    public void removeFromWorld() {
        if (level == null || level.isClientSide()) {
            return;
        }
        VolumeConnection connection = getCurrentConnection();
        if (connection != null) {
            List<BlockPos> allPositions = ImmutableList.copyOf(connection.getMarkerPositions());
            for (BlockPos p : allPositions) {
                BlockState markerState = level.getBlockState(p);
                if (!markerState.isAir()) {
                    Block.popResource(level, p, new ItemStack(markerState.getBlock()));
                    level.destroyBlock(p, false);
                }
            }
        }
    }

    @Override
    public boolean isValidFromLocation(BlockPos pos) {
        VolumeConnection connection = getCurrentConnection();
        if (connection == null) {
            return false;
        }
        Box box = connection.getBox();
        if (box.contains(pos)) {
            return false;
        }
        for (BlockPos p : PositionUtil.getCorners(box.min(), box.max())) {
            if (PositionUtil.isNextTo(p, pos)) {
                return true;
            }
        }
        return false;
    }
}
