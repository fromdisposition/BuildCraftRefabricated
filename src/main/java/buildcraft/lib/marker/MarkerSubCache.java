/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.marker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;

import buildcraft.lib.fabric.PacketDistributor;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.tile.TileMarker;

public abstract class MarkerSubCache<C extends MarkerConnection<C>> {
    public static final boolean DEBUG_FULL = BCDebugging.shouldDebugComplex("lib.marker.full");

    public final int cacheId;
    public final ResourceKey<Level> dimensionId;
    @Nullable
    private final ServerLevel serverLevel;
    public final boolean isServer;
    private final Map<BlockPos, C> posToConnection = new ConcurrentHashMap<>();
    private final Map<C, Set<BlockPos>> connectionToPos = new ConcurrentHashMap<>();
    private final Map<BlockPos, Optional<TileMarker<C>>> tileCache = new ConcurrentHashMap<>();

    public MarkerSubCache(Level level, int cacheId) {
        this.isServer = !level.isClientSide();
        this.dimensionId = level.dimension();
        this.serverLevel = level instanceof ServerLevel sl ? sl : null;
        this.cacheId = cacheId;
    }

    public void onPlayerJoinWorld(ServerPlayer player) {
        if (isServer) {

            if (!tileCache.isEmpty()) {
                List<BlockPos> positions = new ArrayList<>(tileCache.keySet());
                MessageMarker message = new MessageMarker(true, false, cacheId, positions);
                PacketDistributor.sendToPlayer(player, message);
            }

            for (C connection : connectionToPos.keySet()) {
                List<BlockPos> positions = new ArrayList<>(connection.getMarkerPositions());
                MessageMarker message = new MessageMarker(true, true, cacheId, positions);
                PacketDistributor.sendToPlayer(player, message);
            }
        }
    }

    public boolean hasLoadedOrUnloadedMarker(BlockPos pos) {
        return tileCache.containsKey(pos);
    }

    @Nullable
    public TileMarker<C> getMarker(BlockPos pos) {
        Optional<TileMarker<C>> op = tileCache.get(pos);
        if (op == null) {
            return null;
        } else {
            return op.orElse(null);
        }
    }

    public void loadMarker(BlockPos pos, @Nullable TileMarker<C> marker) {
        boolean did = tileCache.containsKey(pos);
        tileCache.put(pos, Optional.ofNullable(marker));
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Set a marker at " + pos + " as " + marker);
        }
        if (isServer && !did) {
            List<BlockPos> positions = new ArrayList<>();
            positions.add(pos);
            MessageMarker message = new MessageMarker(true, false, cacheId, positions);
            sendToDimension(message);
            markSavedDataDirty();
        }
    }

    public void unloadMarker(BlockPos pos) {
        loadMarker(pos, null);
    }

    public void removeMarker(BlockPos pos) {
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Removed a marker at " + pos);
        }
        tileCache.remove(pos);
        C connection = getConnection(pos);
        if (connection != null) {
            connection.removeMarker(pos);
            refreshConnection(connection);
        }
        if (isServer) {
            List<BlockPos> positions = new ArrayList<>();
            positions.add(pos);
            MessageMarker message = new MessageMarker(false, false, cacheId, positions);
            sendToDimension(message);
            markSavedDataDirty();
        }
    }

    public ImmutableList<BlockPos> getAllMarkers() {
        return ImmutableList.copyOf(tileCache.keySet());
    }

    @Nullable
    public C getConnection(BlockPos pos) {
        return posToConnection.get(pos);
    }

    public void destroyConnection(@Nullable C connection) {
        if (connection == null) {
            return;
        }
        Set<BlockPos> set = connectionToPos.remove(connection);
        if (set != null) {
            deinitConnection(set);
        }
        if (isServer) {
            markSavedDataDirty();
        }

        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    public void addConnection(@Nonnull C connection) {
        Set<BlockPos> lastSeen = new HashSet<>(connection.getMarkerPositions());
        initConnection(connection, lastSeen);
        if (isServer) {
            markSavedDataDirty();
        }
        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    public void refreshConnection(@Nonnull C connection) {
        Set<BlockPos> lastSeen = connectionToPos.get(connection);
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Refreshing a connection");
            BCLog.logger.info("[lib.marker.full]    - Old = " + lastSeen);
            BCLog.logger.info("[lib.marker.full]    - New = " + connection.getMarkerPositions());
        }
        if (lastSeen == null) {

            addConnection(connection);
        } else {
            Set<BlockPos> invalid = new HashSet<>(lastSeen);
            lastSeen = new HashSet<>(connection.getMarkerPositions());
            invalid.removeAll(lastSeen);
            deinitConnection(invalid);
            initConnection(connection, lastSeen);
            if (lastSeen.isEmpty()) {
                connectionToPos.remove(connection);
            }
        }
        if (isServer) {
            markSavedDataDirty();
        }

        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    private void validateAllConnections() {
        final String logStart = "[lib.marker.full][" + cacheId + "]";

        Set<C> visited = new HashSet<>();
        Set<BlockPos> visitedPos = new HashSet<>();

        for (Entry<C, Set<BlockPos>> entry : connectionToPos.entrySet()) {
            C con = entry.getKey();
            Set<BlockPos> positions = entry.getValue();
            Set<BlockPos> actual = new HashSet<>(con.getMarkerPositions());
            if (!positions.equals(actual)) {
                BCLog.logger.warn(logStart + " Positions differed!");
                List<BlockPos> total = new ArrayList<>();
                total.addAll(positions);
                total.addAll(actual);
                for (BlockPos p : total) {
                    String s = "(";
                    s += positions.contains(p) ? "R" : "_";
                    s += actual.contains(p) ? "S" : "_";
                    BCLog.logger.warn(logStart + "  - " + p + " " + s + ")");
                }
            }
            for (BlockPos p : positions) {
                if (visitedPos.contains(p)) {
                    BCLog.logger.warn(logStart + " Duplicate block positions!" + p + " - " + con);
                }
                visitedPos.add(p);
            }
            visited.add(con);
        }

        for (Entry<BlockPos, C> entry : posToConnection.entrySet()) {
            C connection = entry.getValue();
            BlockPos p = entry.getKey();
            if (!visited.contains(connection)) {
                BCLog.logger.warn(logStart + " Unknown connection " + connection + "(" + p + ")");
            }
            if (!visitedPos.contains(p)) {
                BCLog.logger.warn(logStart + " Unknown Position " + p + " (" + connection + ")");
            }
        }
    }

    private void deinitConnection(Set<BlockPos> set) {
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Tearing down all connections in " + set);
        }
        for (BlockPos p : set) {
            posToConnection.remove(p);
        }
        if (isServer && set.size() > 0) {
            List<BlockPos> positions = new ArrayList<>(set);
            MessageMarker message = new MessageMarker(false, true, cacheId, positions);
            sendToDimension(message);
        }
    }

    private void initConnection(C connection, Set<BlockPos> lastSeen) {
        if (DEBUG_FULL) {
            BCLog.logger.info("[lib.marker.full] Setting up a connection with " + lastSeen);
        }
        if (lastSeen.size() < 2) {
            connectionToPos.remove(connection);
            for (BlockPos p : lastSeen) {
                posToConnection.remove(p);
            }
            return;
        }

        connectionToPos.put(connection, lastSeen);
        for (BlockPos p : lastSeen) {
            posToConnection.put(p, connection);
        }
        if (isServer && lastSeen.size() > 0) {
            List<BlockPos> positions = new ArrayList<>(connection.getMarkerPositions());
            MessageMarker message = new MessageMarker(true, true, cacheId, positions);
            sendToDimension(message);
        }
    }

    private void sendToDimension(MessageMarker message) {
        if (serverLevel != null) {
            PacketDistributor.sendToPlayersInDimension(serverLevel, message);
        }
    }

    public ImmutableList<C> getConnections() {
        return ImmutableList.copyOf(connectionToPos.keySet());
    }

    protected abstract void markSavedDataDirty();

    public abstract boolean tryConnect(BlockPos from, BlockPos to);

    public abstract boolean canConnect(BlockPos a, BlockPos b);

    public abstract ImmutableList<BlockPos> getValidConnections(BlockPos from);

    public abstract buildcraft.lib.client.render.laser.LaserData_BC8.LaserType getPossibleLaserType();

    public final void handleMessageMain(MessageMarker message) {
        if (handleMessage(message)) {
            return;
        }
        if (!message.connection()) {
            List<BlockPos> positions = message.positions();
            if (message.add()) {
                for (BlockPos p : positions) {
                    if (!hasLoadedOrUnloadedMarker(p)) {
                        loadMarker(p, null);
                    }
                }
            } else {
                for (BlockPos p : positions) {
                    removeMarker(p);
                }
            }
        }
    }

    protected abstract boolean handleMessage(MessageMarker message);
}
