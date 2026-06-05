/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.power;

import net.minecraft.core.Direction;

public interface IEngine {

    boolean canReceiveFromEngine(Direction side);

    boolean receivePower(long microJoules, boolean simulate);
}
