package buildcraft.lib.marker;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.tile.TileMarker;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

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
      if (this.isServer) {
         if (!this.tileCache.isEmpty()) {
            List<BlockPos> positions = new ArrayList<>(this.tileCache.keySet());
            MessageMarker message = new MessageMarker(true, false, this.cacheId, positions);
            BcPacketDistributor.sendToPlayer(player, message);
         }

         for (C connection : this.connectionToPos.keySet()) {
            List<BlockPos> positions = new ArrayList<>(connection.getMarkerPositions());
            MessageMarker message = new MessageMarker(true, true, this.cacheId, positions);
            BcPacketDistributor.sendToPlayer(player, message);
         }
      }
   }

   public boolean hasLoadedOrUnloadedMarker(BlockPos pos) {
      return this.tileCache.containsKey(pos);
   }

   @Nullable
   public TileMarker<C> getMarker(BlockPos pos) {
      Optional<TileMarker<C>> op = this.tileCache.get(pos);
      return op == null ? null : op.orElse(null);
   }

   public void loadMarker(BlockPos pos, @Nullable TileMarker<C> marker) {
      boolean did = this.tileCache.containsKey(pos);
      this.tileCache.put(pos, Optional.ofNullable(marker));
      if (DEBUG_FULL) {
         BCLog.logger.info("[lib.marker.full] Set a marker at " + pos + " as " + marker);
      }

      if (this.isServer && !did) {
         List<BlockPos> positions = new ArrayList<>();
         positions.add(pos);
         MessageMarker message = new MessageMarker(true, false, this.cacheId, positions);
         this.sendToDimension(message);
         this.markSavedDataDirty();
      }
   }

   public void unloadMarker(BlockPos pos) {
      this.loadMarker(pos, null);
   }

   public void removeMarker(BlockPos pos) {
      if (DEBUG_FULL) {
         BCLog.logger.info("[lib.marker.full] Removed a marker at " + pos);
      }

      this.tileCache.remove(pos);
      C connection = this.getConnection(pos);
      if (connection != null) {
         connection.removeMarker(pos);
         this.refreshConnection(connection);
      }

      if (this.isServer) {
         List<BlockPos> positions = new ArrayList<>();
         positions.add(pos);
         MessageMarker message = new MessageMarker(false, false, this.cacheId, positions);
         this.sendToDimension(message);
         this.markSavedDataDirty();
      }
   }

   public ImmutableList<BlockPos> getAllMarkers() {
      return ImmutableList.copyOf(this.tileCache.keySet());
   }

   @Nullable
   public C getConnection(BlockPos pos) {
      return this.posToConnection.get(pos);
   }

   public void destroyConnection(@Nullable C connection) {
      if (connection != null) {
         Set<BlockPos> set = this.connectionToPos.remove(connection);
         if (set != null) {
            this.deinitConnection(set);
         }

         if (this.isServer) {
            this.markSavedDataDirty();
         }

         if (DEBUG_FULL) {
            this.validateAllConnections();
         }
      }
   }

   public void addConnection(@Nonnull C connection) {
      Set<BlockPos> lastSeen = new HashSet<>(connection.getMarkerPositions());
      this.initConnection(connection, lastSeen);
      if (this.isServer) {
         this.markSavedDataDirty();
      }

      if (DEBUG_FULL) {
         this.validateAllConnections();
      }
   }

   public void refreshConnection(@Nonnull C connection) {
      Set<BlockPos> lastSeen = this.connectionToPos.get(connection);
      if (DEBUG_FULL) {
         BCLog.logger.info("[lib.marker.full] Refreshing a connection");
         BCLog.logger.info("[lib.marker.full]    - Old = " + lastSeen);
         BCLog.logger.info("[lib.marker.full]    - New = " + connection.getMarkerPositions());
      }

      if (lastSeen == null) {
         this.addConnection(connection);
      } else {
         Set<BlockPos> invalid = new HashSet<>(lastSeen);
         lastSeen = new HashSet<>(connection.getMarkerPositions());
         invalid.removeAll(lastSeen);
         this.deinitConnection(invalid);
         this.initConnection(connection, lastSeen);
         if (lastSeen.isEmpty()) {
            this.connectionToPos.remove(connection);
         }
      }

      if (this.isServer) {
         this.markSavedDataDirty();
      }

      if (DEBUG_FULL) {
         this.validateAllConnections();
      }
   }

   private void validateAllConnections() {
      String logStart = "[lib.marker.full][" + this.cacheId + "]";
      Set<C> visited = new HashSet<>();
      Set<BlockPos> visitedPos = new HashSet<>();

      for (Entry<C, Set<BlockPos>> entry : this.connectionToPos.entrySet()) {
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
               s = s + (positions.contains(p) ? "R" : "_");
               s = s + (actual.contains(p) ? "S" : "_");
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

      for (Entry<BlockPos, C> entry : this.posToConnection.entrySet()) {
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
         this.posToConnection.remove(p);
      }

      if (this.isServer && set.size() > 0) {
         List<BlockPos> positions = new ArrayList<>(set);
         MessageMarker message = new MessageMarker(false, true, this.cacheId, positions);
         this.sendToDimension(message);
      }
   }

   private void initConnection(C connection, Set<BlockPos> lastSeen) {
      if (DEBUG_FULL) {
         BCLog.logger.info("[lib.marker.full] Setting up a connection with " + lastSeen);
      }

      if (lastSeen.size() < 2) {
         this.connectionToPos.remove(connection);

         for (BlockPos p : lastSeen) {
            this.posToConnection.remove(p);
         }
      } else {
         this.connectionToPos.put(connection, lastSeen);

         for (BlockPos p : lastSeen) {
            this.posToConnection.put(p, connection);
         }

         if (this.isServer && lastSeen.size() > 0) {
            List<BlockPos> positions = new ArrayList<>(connection.getMarkerPositions());
            MessageMarker message = new MessageMarker(true, true, this.cacheId, positions);
            this.sendToDimension(message);
         }
      }
   }

   private void sendToDimension(MessageMarker message) {
      this.sendNearMarkers(message, message.positions());
   }

   private void sendNearMarkers(MessageMarker message, Iterable<BlockPos> nearPositions) {
      if (this.serverLevel != null) {
         Set<ServerPlayer> sent = new HashSet<>();

         for (BlockPos pos : nearPositions) {
            for (ServerPlayer player : PlayerLookup.tracking(this.serverLevel, pos)) {
               if (sent.add(player)) {
                  BcPacketDistributor.sendToPlayer(player, message);
               }
            }
         }
      }
   }

   public ImmutableList<C> getConnections() {
      return ImmutableList.copyOf(this.connectionToPos.keySet());
   }

   protected abstract void markSavedDataDirty();

   public abstract boolean tryConnect(BlockPos var1, BlockPos var2);

   public abstract boolean canConnect(BlockPos var1, BlockPos var2);

   public abstract ImmutableList<BlockPos> getValidConnections(BlockPos var1);

   public abstract LaserData_BC8.LaserType getPossibleLaserType();

   public final void handleMessageMain(MessageMarker message) {
      if (!this.handleMessage(message)) {
         if (!message.connection()) {
            List<BlockPos> positions = message.positions();
            if (message.add()) {
               for (BlockPos p : positions) {
                  if (!this.hasLoadedOrUnloadedMarker(p)) {
                     this.loadMarker(p, null);
                  }
               }
            } else {
               for (BlockPos p : positions) {
                  this.removeMarker(p);
               }
            }
         }
      }
   }

   protected abstract boolean handleMessage(MessageMarker var1);
}
