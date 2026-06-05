/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;

import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.net.MessageMarker;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;

@SuppressWarnings("this-escape")
public class VolumeSubCache extends MarkerSubCache<VolumeConnection> {
    private VolumeSavedData savedData;

    @Override
    public LaserType getPossibleLaserType() {
        return BuildCraftLaserManager.MARKER_VOLUME_POSSIBLE;
    }

    public VolumeSubCache(Level world) {
        super(world, MarkerCache.CACHES.indexOf(VolumeCache.INSTANCE));
        VolumeSavedData data = VolumeSavedData.getOrCreate(world);
        this.savedData = data;
        for (BlockPos pos : data.markerPositions) {
            loadMarker(pos, null);
        }
        for (java.util.List<BlockPos> connectionPositions : data.markerConnections) {
            if (connectionPositions.size() >= 2) {
                addConnection(new VolumeConnection(this, connectionPositions));
            }
        }
        data.setSubCache(this);
        data.setDirty();
    }

    @Override
    protected void markSavedDataDirty() {
        if (savedData != null) {
            savedData.setDirty();
        }
    }

    @Override
    public boolean tryConnect(BlockPos from, BlockPos to) {
        VolumeConnection fromConnection = getConnection(from);
        VolumeConnection toConnection = getConnection(to);
        if (fromConnection == null) {
            if (toConnection == null) {
                return VolumeConnection.tryCreateConnection(this, from, to);
            } else {
                return toConnection.addMarker(from);
            }
        } else {
            if (toConnection == null) {
                return fromConnection.addMarker(to);
            } else {
                return fromConnection.mergeWith(toConnection);
            }
        }
    }

    @Override
    public boolean canConnect(BlockPos from, BlockPos to) {
        VolumeConnection fromConnection = getConnection(from);
        VolumeConnection toConnection = getConnection(to);
        if (fromConnection == null) {
            if (toConnection == null) {
                return VolumeConnection.canCreateConnection(this, from, to);
            } else {
                return toConnection.canAddMarker(from);
            }
        } else {
            if (toConnection == null) {
                return fromConnection.canAddMarker(to);
            } else {
                return fromConnection.canMergeWith(toConnection);
            }
        }
    }

    @Override
    public ImmutableList<BlockPos> getValidConnections(BlockPos from) {
        VolumeConnection existing = getConnection(from);
        Set<Axis> taken = EnumSet.noneOf(Direction.Axis.class);
        if (existing != null) {
            taken.addAll(existing.getConnectedAxis());
        }

        ImmutableList.Builder<BlockPos> valids = ImmutableList.builder();
        for (Direction face : Direction.values()) {
            if (taken.contains(face.getAxis())) continue;
            for (int i = 1; i <= BCCoreConfig.markerMaxDistance.get(); i++) {
                BlockPos toTry = from.relative(face, i);
                if (hasLoadedOrUnloadedMarker(toTry)) {
                    if (!canConnect(from, toTry)) break;
                    valids.add(toTry);
                    break;
                }
            }
        }
        return valids.build();
    }

    @Override
    protected boolean handleMessage(MessageMarker message) {
        List<BlockPos> positions = message.positions();
        if (message.connection()) {
            if (message.add()) {
                for (BlockPos p : positions) {
                    VolumeConnection existing = this.getConnection(p);
                    destroyConnection(existing);
                }
                VolumeConnection con = new VolumeConnection(this, positions);
                addConnection(con);
            } else {
                for (BlockPos p : positions) {
                    VolumeConnection existing = this.getConnection(p);
                    if (existing != null) {
                        existing.removeMarker(p);
                        refreshConnection(existing);
                    }
                }
            }
        }
        return false;
    }
}
