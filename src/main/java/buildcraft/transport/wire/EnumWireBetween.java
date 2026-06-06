package buildcraft.transport.wire;

import buildcraft.api.transport.EnumWirePart;
import java.util.Arrays;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;

public enum EnumWireBetween {
   X_UP_SOUTH(Axis.X, true, true),
   X_UP_NORTH(Axis.X, true, false),
   X_DOWN_SOUTH(Axis.X, false, true),
   X_DOWN_NORTH(Axis.X, false, false),
   Y_SOUTH_EAST(Axis.Y, true, true),
   Y_SOUTH_WEST(Axis.Y, true, false),
   Y_NORTH_EAST(Axis.Y, false, true),
   Y_NORTH_WEST(Axis.Y, false, false),
   Z_UP_EAST(Axis.Z, true, true),
   Z_UP_WEST(Axis.Z, true, false),
   Z_DOWN_EAST(Axis.Z, false, true),
   Z_DOWN_WEST(Axis.Z, false, false),
   EAST_UP_SOUTH(Direction.EAST, true, true),
   EAST_UP_NORTH(Direction.EAST, true, false),
   EAST_DOWN_SOUTH(Direction.EAST, false, true),
   EAST_DOWN_NORTH(Direction.EAST, false, false),
   WEST_UP_SOUTH(Direction.WEST, true, true),
   WEST_UP_NORTH(Direction.WEST, true, false),
   WEST_DOWN_SOUTH(Direction.WEST, false, true),
   WEST_DOWN_NORTH(Direction.WEST, false, false),
   UP_SOUTH_EAST(Direction.UP, true, true),
   UP_SOUTH_WEST(Direction.UP, true, false),
   UP_NORTH_EAST(Direction.UP, false, true),
   UP_NORTH_WEST(Direction.UP, false, false),
   DOWN_SOUTH_EAST(Direction.DOWN, true, true),
   DOWN_SOUTH_WEST(Direction.DOWN, true, false),
   DOWN_NORTH_EAST(Direction.DOWN, false, true),
   DOWN_NORTH_WEST(Direction.DOWN, false, false),
   SOUTH_UP_EAST(Direction.SOUTH, true, true),
   SOUTH_UP_WEST(Direction.SOUTH, true, false),
   SOUTH_DOWN_EAST(Direction.SOUTH, false, true),
   SOUTH_DOWN_WEST(Direction.SOUTH, false, false),
   NORTH_UP_EAST(Direction.NORTH, true, true),
   NORTH_UP_WEST(Direction.NORTH, true, false),
   NORTH_DOWN_EAST(Direction.NORTH, false, true),
   NORTH_DOWN_WEST(Direction.NORTH, false, false);

   public static final EnumWireBetween[] VALUES = values();
   public static final EnumWireBetween[] CENTRES = Arrays.copyOfRange(VALUES, 0, 12, EnumWireBetween[].class);
   public static final EnumWireBetween[] CONNECTIONS = Arrays.copyOfRange(VALUES, 12, 36, EnumWireBetween[].class);
   public final Axis mainAxis;
   public final Direction to;
   public final boolean xy;
   public final boolean yz;
   public final AABB boundingBox;
   public final EnumWirePart[] parts;

   EnumWireBetween(Axis mainAxis, boolean xy, boolean yz) {
      this.mainAxis = mainAxis;
      this.to = null;
      this.xy = xy;
      this.yz = yz;
      int x1 = mainAxis == Axis.X ? 4 : (xy ? 12 : 3);
      int y1 = mainAxis == Axis.Y ? 4 : ((mainAxis == Axis.X ? !xy : !yz) ? 3 : 12);
      int z1 = mainAxis == Axis.Z ? 4 : (yz ? 12 : 3);
      int x2 = x1 + (mainAxis == Axis.X ? 8 : 1);
      int y2 = y1 + (mainAxis == Axis.Y ? 8 : 1);
      int z2 = z1 + (mainAxis == Axis.Z ? 8 : 1);
      this.boundingBox = new AABB(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
      this.parts = this.getParts();
   }

   EnumWireBetween(Direction to, boolean xy, boolean yz) {
      this.mainAxis = to.getAxis();
      this.to = to;
      this.xy = xy;
      this.yz = yz;
      int start = to.getAxisDirection() == AxisDirection.POSITIVE ? 13 : 0;
      int x1 = this.mainAxis == Axis.X ? start : (xy ? 12 : 3);
      int y1 = this.mainAxis == Axis.Y ? start : ((this.mainAxis == Axis.X ? !xy : !yz) ? 3 : 12);
      int z1 = this.mainAxis == Axis.Z ? start : (yz ? 12 : 3);
      int x2 = x1 + (this.mainAxis == Axis.X ? 3 : 1);
      int y2 = y1 + (this.mainAxis == Axis.Y ? 3 : 1);
      int z2 = z1 + (this.mainAxis == Axis.Z ? 3 : 1);
      this.boundingBox = new AABB(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
      this.parts = this.getParts();
   }

   private EnumWirePart[] getParts() {
      Function<AxisDirection[], EnumWirePart> getPartFromDirections = directionsx -> Arrays.stream(EnumWirePart.VALUES)
         .filter(part -> part.x == directionsx[0] && part.y == directionsx[1] && part.z == directionsx[2])
         .findFirst()
         .orElse(null);
      EnumWirePart[] arr = new EnumWirePart[2];

      for (int i = 0; i < arr.length; i++) {
         AxisDirection[] directions = new AxisDirection[3];
         boolean found = false;

         for (int j = 0; j < directions.length; j++) {
            if (this.mainAxis.ordinal() == j) {
               if (this.to == null) {
                  directions[j] = i == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE;
               } else {
                  directions[j] = i == 0 ? this.to.getAxisDirection() : this.to.getOpposite().getAxisDirection();
               }
            } else if (!found) {
               directions[j] = this.xy ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
               found = true;
            } else {
               directions[j] = this.yz ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
            }
         }

         arr[i] = getPartFromDirections.apply(directions);
      }

      return arr;
   }
}
