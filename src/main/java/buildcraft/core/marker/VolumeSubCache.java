package buildcraft.core.marker;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
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

public class VolumeSubCache extends MarkerSubCache<VolumeConnection> {
   private VolumeSavedData savedData;

   @Override
   public LaserData_BC8.LaserType getPossibleLaserType() {
      return BuildCraftLaserManager.MARKER_VOLUME_POSSIBLE;
   }

   public VolumeSubCache(Level world) {
      super(world, MarkerCache.CACHES.indexOf(VolumeCache.INSTANCE));
      VolumeSavedData data = VolumeSavedData.getOrCreate(world);
      this.savedData = data;

      for (BlockPos pos : data.markerPositions) {
         this.loadMarker(pos, null);
      }

      for (List<BlockPos> connectionPositions : data.markerConnections) {
         if (connectionPositions.size() >= 2) {
            this.addConnection(new VolumeConnection(this, connectionPositions));
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
      List<BlockPos> positions = message.positions();
      if (message.connection()) {
         if (message.add()) {
            for (BlockPos p : positions) {
               VolumeConnection existing = this.getConnection(p);
               this.destroyConnection(existing);
            }

            VolumeConnection con = new VolumeConnection(this, positions);
            this.addConnection(con);
         } else {
            for (BlockPos p : positions) {
               VolumeConnection existing = this.getConnection(p);
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
