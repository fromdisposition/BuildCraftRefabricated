package buildcraft.core.marker;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.net.MessageMarker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PathSubCache extends MarkerSubCache<PathConnection> {
   private PathSavedData savedData;

   @Override
   public LaserData_BC8.LaserType getPossibleLaserType() {
      return BuildCraftLaserManager.MARKER_PATH_POSSIBLE;
   }

   public PathSubCache(Level world) {
      super(world, MarkerCache.CACHES.indexOf(PathCache.INSTANCE));
      PathSavedData data = PathSavedData.getOrCreate(world);
      this.savedData = data;

      for (BlockPos pos : data.markerPositions) {
         this.loadMarker(pos, null);
      }

      for (List<BlockPos> connectionPositions : data.markerConnections) {
         if (connectionPositions.size() >= 2) {
            this.addConnection(new PathConnection(this, connectionPositions));
         }
      }

      data.setSubCache(this);
      data.setDirty();
   }

   @Override
   protected void markSavedDataDirty() {
      if (this.savedData != null) {
         this.savedData.setDirty();
      }
   }

   @Override
   public boolean tryConnect(BlockPos from, BlockPos to) {
      PathConnection conFrom = this.getConnection(from);
      PathConnection conTo = this.getConnection(to);
      if (conFrom == null) {
         return conTo == null ? PathConnection.tryCreateConnection(this, from, to) : conTo.addMarker(from, to);
      } else {
         return conTo == null ? conFrom.addMarker(from, to) : conFrom.mergeWith(conTo, from, to);
      }
   }

   @Override
   public boolean canConnect(BlockPos from, BlockPos to) {
      PathConnection conFrom = this.getConnection(from);
      PathConnection conTo = this.getConnection(to);
      if (conFrom == null) {
         return conTo == null ? true : conTo.canAddMarker(from, to);
      } else {
         return conTo == null ? conFrom.canAddMarker(from, to) : conFrom.canMergeWith(conTo, from, to);
      }
   }

   @Override
   public ImmutableList<BlockPos> getValidConnections(BlockPos from) {
      Builder<BlockPos> list = ImmutableList.builder();
      int max = BCCoreConfig.markerMaxDistance.get();
      int maxLengthSquared = max * max;
      UnmodifiableIterator var5 = this.getAllMarkers().iterator();

      while (var5.hasNext()) {
         BlockPos pos = (BlockPos)var5.next();
         if (!pos.equals(from) && !(pos.distSqr(from) > maxLengthSquared) && (this.canConnect(from, pos) || this.canConnect(pos, from))) {
            list.add(pos);
         }
      }

      return list.build();
   }

   @Override
   protected boolean handleMessage(MessageMarker message) {
      List<BlockPos> positions = message.positions();
      if (message.connection()) {
         if (message.add()) {
            for (BlockPos p : positions) {
               PathConnection existing = this.getConnection(p);
               this.destroyConnection(existing);
            }

            PathConnection con = new PathConnection(this, positions);
            this.addConnection(con);
         } else {
            for (BlockPos p : positions) {
               PathConnection existing = this.getConnection(p);
               if (existing != null) {
                  existing.removeMarker(p);
                  this.refreshConnection(existing);
               }
            }
         }
      }

      return false;
   }
}
