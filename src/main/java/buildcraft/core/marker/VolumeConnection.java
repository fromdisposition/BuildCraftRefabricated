package buildcraft.core.marker;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.Box;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class VolumeConnection extends MarkerConnection<VolumeConnection> {
   private static final double RENDER_SCALE = 0.06230529595015576;
   private final Set<BlockPos> makeup = new HashSet<>();
   private final Box box = new Box();

   public static boolean tryCreateConnection(VolumeSubCache subCache, BlockPos from, BlockPos to) {
      if (canCreateConnection(subCache, from, to)) {
         VolumeConnection connection = new VolumeConnection(subCache);
         connection.makeup.add(from);
         connection.makeup.add(to);
         connection.createBox();
         subCache.addConnection(connection);
         return true;
      } else {
         return false;
      }
   }

   public static boolean canCreateConnection(VolumeSubCache subCache, BlockPos from, BlockPos to) {
      Direction directOffset = PositionUtil.getDirectFacingOffset(from, to);
      if (directOffset == null) {
         return false;
      }

      for (int i = 1; i <= BCCoreConfig.markerMaxDistance.get(); i++) {
         BlockPos offset = from.relative(directOffset, i);
         if (offset.equals(to)) {
            return true;
         }

         if (subCache.hasLoadedOrUnloadedMarker(offset)) {
            return false;
         }
      }

      return false;
   }

   public VolumeConnection(VolumeSubCache subCache) {
      super(subCache);
   }

   public VolumeConnection(VolumeSubCache subCache, Collection<BlockPos> positions) {
      super(subCache);
      this.makeup.addAll(positions);
      this.createBox();
   }

   @Override
   public void removeMarker(BlockPos pos) {
      this.makeup.remove(pos);
      if (this.makeup.size() < 2) {
         this.makeup.clear();
      }

      this.createBox();
   }

   public boolean addMarker(BlockPos pos) {
      if (this.canAddMarker(pos)) {
         this.makeup.add(pos);
         this.createBox();
         this.subCache.refreshConnection(this);
         return true;
      } else {
         return false;
      }
   }

   public boolean canAddMarker(BlockPos to) {
      Set<Axis> taken = this.getConnectedAxis();

      for (BlockPos from : this.makeup) {
         Direction direct = PositionUtil.getDirectFacingOffset(from, to);
         if (direct != null && !taken.contains(direct.getAxis())) {
            return true;
         }
      }

      return !this.makeup.contains(to) && this.box.isCorner(to);
   }

   public boolean mergeWith(VolumeConnection other) {
      if (this.canMergeWith(other)) {
         this.makeup.addAll(other.makeup);
         other.makeup.clear();
         this.createBox();
         this.subCache.refreshConnection(other);
         this.subCache.refreshConnection(this);
         return true;
      } else {
         return false;
      }
   }

   public boolean canMergeWith(VolumeConnection other) {
      EnumSet<Axis> us = this.getConnectedAxis();
      EnumSet<Axis> them = other.getConnectedAxis();
      if (us.size() == 1 && them.size() == 1) {
         if (us.equals(them)) {
            return false;
         }

         Set<Axis> blacklisted = EnumSet.copyOf(us);
         blacklisted.addAll(them);

         for (BlockPos from : this.makeup) {
            for (BlockPos to : other.makeup) {
               Direction offset = PositionUtil.getDirectFacingOffset(from, to);
               if (offset != null && !blacklisted.contains(offset.getAxis())) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public EnumSet<Axis> getConnectedAxis() {
      EnumSet<Axis> taken = EnumSet.noneOf(Axis.class);

      for (BlockPos a : this.getMarkerPositions()) {
         for (BlockPos b : this.getMarkerPositions()) {
            Direction offset = PositionUtil.getDirectFacingOffset(a, b);
            if (offset != null) {
               taken.add(offset.getAxis());
            }
         }
      }

      return taken;
   }

   @Override
   public Collection<BlockPos> getMarkerPositions() {
      return this.makeup;
   }

   private void createBox() {
      this.box.reset();

      for (BlockPos p : this.makeup) {
         this.box.extendToEncompass(p);
      }
   }

   public Box getBox() {
      return new Box(this.box.min(), this.box.max());
   }

   @Override
   public void renderInWorld() {
      LaserBoxRenderer.renderLaserBoxStatic(
         MarkerRenderer.getPoseStack(), this.box, BuildCraftLaserManager.MARKER_VOLUME_CONNECTED, true, false, MarkerRenderer.getCameraPos()
      );
   }
}
