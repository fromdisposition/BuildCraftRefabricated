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

   BlockPos getRandomBlockPos(Random var1);
}
