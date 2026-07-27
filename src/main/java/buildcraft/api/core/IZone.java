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

   /** Same containment test as {@link #contains(Vec3)} without requiring a Vec3. Robot block scans call this for
    * thousands of candidate positions per tick, so implementations should override it allocation-free. */
   default boolean contains(double x, double y, double z) {
      return this.contains(new Vec3(x, y, z));
   }

   BlockPos getRandomBlockPos(Random var1);
}
