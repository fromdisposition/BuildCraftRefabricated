package buildcraft.core.marker;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.net.MessageMarker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PathSubCache extends SavedDataBackedMarkerSubCache<PathConnection, PathSavedData> {
   public PathSubCache(Level world) {
      super(world, MarkerCache.CACHES.indexOf(PathCache.INSTANCE), PathSavedData.getOrCreate(world), (cache, positions) -> new PathConnection((PathSubCache)cache, positions));
   }

   @Override
   public LaserData_BC8.LaserType getPossibleLaserType() {
      return BuildCraftLaserManager.MARKER_PATH_POSSIBLE;
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
      return this.handleConnectionMessage(message, (cache, positions) -> new PathConnection((PathSubCache)cache, positions));
   }
}
