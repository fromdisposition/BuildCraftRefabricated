/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface IZone {
   double distanceTo(BlockPos var1);

   double distanceToSquared(BlockPos var1);

   boolean contains(Vec3 var1);

   /** Equivalent to {@link #contains(Vec3)}; called for thousands of positions per tick by robot scans, so override allocation-free. */
   default boolean contains(double x, double y, double z) {
      return this.contains(new Vec3(x, y, z));
   }

   BlockPos getRandomBlockPos(Random var1);

   /**
    * Horizontal extent of the zone, or {@code null} if none. Lets entity/item lookups use a finite box instead of
    * an unbounded guess; {@link #contains} still does the real clamp.
    */
   default net.minecraft.world.phys.AABB horizontalBounds() {
      return null;
   }
}
