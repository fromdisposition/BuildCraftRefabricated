/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport;

import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public enum EnumWirePart {
   EAST_UP_SOUTH(true, true, true),
   EAST_UP_NORTH(true, true, false),
   EAST_DOWN_SOUTH(true, false, true),
   EAST_DOWN_NORTH(true, false, false),
   WEST_UP_SOUTH(false, true, true),
   WEST_UP_NORTH(false, true, false),
   WEST_DOWN_SOUTH(false, false, true),
   WEST_DOWN_NORTH(false, false, false);

   public static final EnumWirePart[] VALUES = values();
   public final AxisDirection x;
   public final AxisDirection y;
   public final AxisDirection z;
   public final AABB boundingBox;
   public final AABB boundingBoxPossible;

   EnumWirePart(boolean x, boolean y, boolean z) {
      this.x = x ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
      this.y = y ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
      this.z = z ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
      double x1 = this.x.getStep() * 0.3125 + 0.5;
      double y1 = this.y.getStep() * 0.3125 + 0.5;
      double z1 = this.z.getStep() * 0.3125 + 0.5;
      double x2 = this.x.getStep() * 0.25 + 0.5;
      double y2 = this.y.getStep() * 0.25 + 0.5;
      double z2 = this.z.getStep() * 0.25 + 0.5;
      this.boundingBox = new AABB(x1, y1, z1, x2, y2, z2);
      Vec3 center = new Vec3(0.5, 0.5, 0.5);
      Vec3 edge = new Vec3(x ? 0.75 : 0.25, y ? 0.75 : 0.25, z ? 0.75 : 0.25);
      this.boundingBoxPossible = new AABB(center.x, center.y, center.z, edge.x, edge.y, edge.z);
   }

   public AxisDirection getDirection(Axis axis) {
      switch (axis) {
         case X:
            return this.x;
         case Y:
            return this.y;
         case Z:
            return this.z;
         default:
            return null;
      }
   }

   public static EnumWirePart get(int x, int y, int z) {
      boolean bx = (x % 2 + 2) % 2 == 1;
      boolean by = (y % 2 + 2) % 2 == 1;
      boolean bz = (z % 2 + 2) % 2 == 1;
      return get(bx, by, bz);
   }

   public static EnumWirePart get(boolean x, boolean y, boolean z) {
      if (x) {
         if (y) {
            return z ? EAST_UP_SOUTH : EAST_UP_NORTH;
         } else {
            return z ? EAST_DOWN_SOUTH : EAST_DOWN_NORTH;
         }
      } else if (y) {
         return z ? WEST_UP_SOUTH : WEST_UP_NORTH;
      } else {
         return z ? WEST_DOWN_SOUTH : WEST_DOWN_NORTH;
      }
   }
}
