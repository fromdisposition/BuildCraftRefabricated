package buildcraft.core.marker.volume;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public enum EnumAddonSlot {
   EAST_UP_SOUTH(AxisDirection.POSITIVE, AxisDirection.POSITIVE, AxisDirection.POSITIVE),
   EAST_UP_NORTH(AxisDirection.POSITIVE, AxisDirection.POSITIVE, AxisDirection.NEGATIVE),
   EAST_DOWN_SOUTH(AxisDirection.POSITIVE, AxisDirection.NEGATIVE, AxisDirection.POSITIVE),
   EAST_DOWN_NORTH(AxisDirection.POSITIVE, AxisDirection.NEGATIVE, AxisDirection.NEGATIVE),
   WEST_UP_SOUTH(AxisDirection.NEGATIVE, AxisDirection.POSITIVE, AxisDirection.POSITIVE),
   WEST_UP_NORTH(AxisDirection.NEGATIVE, AxisDirection.POSITIVE, AxisDirection.NEGATIVE),
   WEST_DOWN_SOUTH(AxisDirection.NEGATIVE, AxisDirection.NEGATIVE, AxisDirection.POSITIVE),
   WEST_DOWN_NORTH(AxisDirection.NEGATIVE, AxisDirection.NEGATIVE, AxisDirection.NEGATIVE);

   public static final EnumAddonSlot[] VALUES = values();
   public final Map<Axis, AxisDirection> directions = new EnumMap<>(Axis.class);

   EnumAddonSlot(AxisDirection x, AxisDirection y, AxisDirection z) {
      this.directions.put(Axis.X, x);
      this.directions.put(Axis.Y, y);
      this.directions.put(Axis.Z, z);
   }

   public AABB getBoundingBox(VolumeBox volumeBox) {
      AABB aabb = volumeBox.box.getBoundingBox();
      Vec3 boxOffset = new Vec3(
         this.directions.get(Axis.X) == AxisDirection.POSITIVE ? aabb.maxX : aabb.minX,
         this.directions.get(Axis.Y) == AxisDirection.POSITIVE ? aabb.maxY : aabb.minY,
         this.directions.get(Axis.Z) == AxisDirection.POSITIVE ? aabb.maxZ : aabb.minZ
      );
      return new AABB(boxOffset.x, boxOffset.y, boxOffset.z, boxOffset.x, boxOffset.y, boxOffset.z).inflate(0.0625);
   }

   public static Pair<VolumeBox, EnumAddonSlot> getSelectingVolumeBoxAndSlot(Player player, List<VolumeBox> volumeBoxes) {
      Vec3 start = player.position().add(0.0, player.getEyeHeight(), 0.0);
      Vec3 end = start.add(player.getLookAngle().scale(4.0));
      VolumeBox bestVolumeBox = null;
      EnumAddonSlot bestSlot = null;
      double bestDist = Double.MAX_VALUE;

      for (VolumeBox volumeBox : volumeBoxes) {
         for (EnumAddonSlot slot : values()) {
            Optional<Vec3> ray = slot.getBoundingBox(volumeBox).clip(start, end);
            if (ray.isPresent()) {
               double dist = ray.get().distanceTo(start);
               if (bestDist > dist) {
                  bestDist = dist;
                  bestVolumeBox = volumeBox;
                  bestSlot = slot;
               }
            }
         }
      }

      return Pair.of(bestVolumeBox, bestSlot);
   }
}
