/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.lib.misc.VecUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public final class FaceDistance {
   public final Direction direction;
   public final int distance;

   public FaceDistance(Axis axis, int distance) {
      this.direction = VecUtil.getFacing(axis, distance > 0);
      this.distance = Math.abs(distance);
   }

   public FaceDistance(Direction direction, int distance) {
      this.direction = direction;
      this.distance = distance;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.direction == null ? 0 : this.direction.hashCode());
      return 31 * result + this.distance;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      FaceDistance other = (FaceDistance)obj;
      return this.direction != other.direction ? false : this.distance == other.distance;
   }

   @Override
   public String toString() {
      return "FaceDistance [direction=" + this.direction + ", distance=" + this.distance + "]";
   }
}
