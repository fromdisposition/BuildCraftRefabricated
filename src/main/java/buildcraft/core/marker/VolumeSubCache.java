package buildcraft.core.marker;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.net.MessageMarker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;

public class VolumeSubCache extends SavedDataBackedMarkerSubCache<VolumeConnection, VolumeSavedData> {
   public VolumeSubCache(Level world) {
      super(world, MarkerCache.CACHES.indexOf(VolumeCache.INSTANCE), VolumeSavedData.getOrCreate(world), (cache, positions) -> new VolumeConnection((VolumeSubCache)cache, positions));
   }

   @Override
   public LaserData_BC8.LaserType getPossibleLaserType() {
      return BuildCraftLaserManager.MARKER_VOLUME_POSSIBLE;
   }

   @Override
   public boolean tryConnect(BlockPos from, BlockPos to) {
      VolumeConnection fromConnection = this.getConnection(from);
      VolumeConnection toConnection = this.getConnection(to);
      if (fromConnection == null) {
         return toConnection == null ? VolumeConnection.tryCreateConnection(this, from, to) : toConnection.addMarker(from);
      } else {
         return toConnection == null ? fromConnection.addMarker(to) : fromConnection.mergeWith(toConnection);
      }
   }

   @Override
   public boolean canConnect(BlockPos from, BlockPos to) {
      VolumeConnection fromConnection = this.getConnection(from);
      VolumeConnection toConnection = this.getConnection(to);
      if (fromConnection == null) {
         return toConnection == null ? VolumeConnection.canCreateConnection(this, from, to) : toConnection.canAddMarker(from);
      } else {
         return toConnection == null ? fromConnection.canAddMarker(to) : fromConnection.canMergeWith(toConnection);
      }
   }

   @Override
   public ImmutableList<BlockPos> getValidConnections(BlockPos from) {
      VolumeConnection existing = this.getConnection(from);
      Set<Axis> taken = EnumSet.noneOf(Axis.class);
      if (existing != null) {
         taken.addAll(existing.getConnectedAxis());
      }

      Builder<BlockPos> valids = ImmutableList.builder();

      for (Direction face : Direction.values()) {
         if (!taken.contains(face.getAxis())) {
            for (int i = 1; i <= BCCoreConfig.markerMaxDistance.get(); i++) {
               BlockPos toTry = from.relative(face, i);
               if (this.hasLoadedOrUnloadedMarker(toTry)) {
                  if (this.canConnect(from, toTry)) {
                     valids.add(toTry);
                  }
                  break;
               }
            }
         }
      }

      return valids.build();
   }

   @Override
   protected boolean handleMessage(MessageMarker message) {
      return this.handleConnectionMessage(message, (cache, positions) -> new VolumeConnection((VolumeSubCache)cache, positions));
   }
}
