/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.core;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface IZone {

    double distanceTo(BlockPos pos);

    double distanceToSquared(BlockPos pos);

    boolean contains(Vec3 point);

    BlockPos getRandomBlockPos(Random rand);
}
