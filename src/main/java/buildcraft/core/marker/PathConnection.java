package buildcraft.core.marker;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.client.render.laser.BcLaserRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.VecUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class PathConnection extends MarkerConnection<PathConnection> {
   private static final double RENDER_SCALE = 0.06230529595015576;
   private static final Vec3 VEC_HALF = new Vec3(0.5, 0.5, 0.5);
   private final Deque<BlockPos> positions = new LinkedList<>();
   private boolean loop = false;

   public static boolean tryCreateConnection(PathSubCache subCache, BlockPos from, BlockPos to) {
      PathConnection connection = new PathConnection(subCache);
      connection.positions.add(from);
      connection.positions.add(to);
      subCache.addConnection(connection);
      return true;
   }

   public PathConnection(MarkerSubCache<PathConnection> subCache) {
      super(subCache);
   }

   public PathConnection(PathSubCache subCache, List<BlockPos> positions) {
      super(subCache);

      for (BlockPos p : positions) {
         if (p.equals(this.positions.peekFirst())) {
            this.loop = true;
            break;
         }

         this.positions.addLast(p);
      }
   }

   @Override
   public void removeMarker(BlockPos pos) {
      if (this.positions.getFirst().equals(pos)) {
         this.positions.removeFirst();
         this.loop = false;
         if (this.positions.size() < 2) {
            this.positions.clear();
         }

         this.subCache.refreshConnection(this);
      } else if (this.positions.getLast().equals(pos)) {
         this.positions.removeLast();
         this.loop = false;
         if (this.positions.size() < 2) {
            this.positions.clear();
         }

         this.subCache.refreshConnection(this);
      } else if (this.positions.contains(pos)) {
         List<BlockPos> a = new ArrayList<>();
         List<BlockPos> b = new ArrayList<>();
         boolean hasReached = false;

         for (BlockPos p : this.positions) {
            if (p.equals(pos)) {
               hasReached = true;
            } else if (hasReached) {
               b.add(p);
            } else {
               a.add(p);
            }
         }

         this.loop = false;
         PathConnection conA = new PathConnection(this.subCache);
         PathConnection conB = new PathConnection(this.subCache);
         conA.positions.addAll(a);
         conB.positions.addAll(b);
         this.positions.clear();
         this.subCache.destroyConnection(this);
         this.subCache.addConnection(conA);
         this.subCache.addConnection(conB);
      }
   }

   public boolean addMarker(BlockPos from, BlockPos toAdd) {
      if (this.loop) {
         return false;
      }

      boolean contains = this.positions.contains(toAdd);
      if (this.positions.getFirst().equals(from)) {
         if (this.positions.getLast().equals(toAdd)) {
            this.loop = true;
         } else {
            if (contains) {
               return false;
            }

            this.positions.addFirst(toAdd);
         }

         this.subCache.refreshConnection(this);
         return true;
      } else if (this.positions.getLast().equals(from)) {
         if (this.positions.getFirst().equals(toAdd)) {
            this.loop = true;
            return true;
         } else if (!contains) {
            this.positions.addLast(toAdd);
            this.subCache.refreshConnection(this);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean canAddMarker(BlockPos from, BlockPos toAdd) {
      if (this.loop) {
         return false;
      } else {
         boolean contains = this.positions.contains(toAdd);
         if (this.positions.getFirst().equals(from)) {
            return contains ? this.positions.getLast().equals(toAdd) : true;
         } else if (this.positions.getLast().equals(from)) {
            return contains ? this.positions.getLast().equals(toAdd) : true;
         } else {
            return false;
         }
      }
   }

   public boolean mergeWith(PathConnection conTo, BlockPos from, BlockPos to) {
      if (this.loop || conTo.loop) {
         return false;
      }

      if (conTo == this) {
         if (this.positions.size() <= 2) {
            return false;
         } else if (this.positions.getFirst().equals(to) && this.positions.getLast().equals(from)) {
            this.loop = true;
            this.subCache.refreshConnection(this);
            return true;
         } else {
            return false;
         }
      } else if (this.positions.getLast().equals(from) && conTo.positions.getFirst().equals(to)) {
         this.subCache.destroyConnection(conTo);
         this.positions.addAll(conTo.positions);
         this.subCache.refreshConnection(this);
         return true;
      } else {
         return false;
      }
   }

   public boolean canMergeWith(PathConnection conTo, BlockPos from, BlockPos to) {
      if (this.loop || conTo.loop) {
         return false;
      } else {
         return conTo == this
            ? this.positions.size() > 2 && this.positions.getFirst().equals(to) && this.positions.getLast().equals(from)
            : this.positions.getLast().equals(from) && conTo.positions.getFirst().equals(to);
      }
   }

   public ImmutableList<BlockPos> getMarkerPositions() {
      if (this.loop && this.positions.size() > 0) {
         Builder<BlockPos> list = ImmutableList.builder();
         list.addAll(this.positions);
         list.add(this.positions.getFirst());
         return list.build();
      } else {
         return ImmutableList.copyOf(this.positions);
      }
   }

   public void reverseDirection() {
      Deque<BlockPos> list = new LinkedList<>();

      while (!this.positions.isEmpty()) {
         list.addFirst(this.positions.removeFirst());
      }

      this.positions.clear();
      this.positions.addAll(list);
      this.subCache.refreshConnection(this);
   }

   @Override
   public void renderInWorld() {
      BlockPos last = null;

      for (BlockPos p : this.positions) {
         if (last == null) {
            last = p;
         } else {
            renderLaser(VecUtil.add(VEC_HALF, last), VecUtil.add(VEC_HALF, p));
            last = p;
         }
      }

      if (this.loop) {
         BlockPos from = this.positions.getLast();
         BlockPos to = this.positions.getFirst();
         renderLaser(VecUtil.add(VEC_HALF, from), VecUtil.add(VEC_HALF, to));
      }
   }

   private static void renderLaser(Vec3 from, Vec3 to) {
      Vec3 one = offset(from, to);
      Vec3 two = offset(to, from);
      LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.MARKER_PATH_CONNECTED, one, two, 0.06230529595015576, false, false, 0);
      BcLaserRenderer.renderLaserStatic(MarkerRenderer.getPoseStack(), data, MarkerRenderer.getCameraPos());
   }

   private static Vec3 offset(Vec3 from, Vec3 to) {
      Vec3 dir = to.subtract(from).normalize();
      return from.add(VecUtil.scale(dir, 0.125));
   }
}
