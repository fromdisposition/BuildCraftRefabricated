/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RotationUtil {
   public static Vec3 rotateVec3(Vec3 vec, Rotation rotation) {
      switch (rotation) {
         case NONE:
         default:
            return vec;
         case CLOCKWISE_90:
            return new Vec3(1.0 - vec.z, vec.y, vec.x);
         case CLOCKWISE_180:
            return new Vec3(1.0 - vec.x, vec.y, 1.0 - vec.z);
         case COUNTERCLOCKWISE_90:
            return new Vec3(vec.z, vec.y, 1.0 - vec.x);
      }
   }

   public static Rotation invert(Rotation rotation) {
      switch (rotation) {
         case NONE:
            return Rotation.NONE;
         case CLOCKWISE_90:
            return Rotation.COUNTERCLOCKWISE_90;
         case CLOCKWISE_180:
            return Rotation.CLOCKWISE_180;
         case COUNTERCLOCKWISE_90:
            return Rotation.CLOCKWISE_90;
         default:
            throw new IllegalArgumentException();
      }
   }
}
