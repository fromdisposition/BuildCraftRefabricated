/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.fuels;

import buildcraft.lib.fluid.stack.FluidStack;

public interface IFuel {
   FluidStack getFluid();

   int getTotalBurningTime();

   long getPowerPerCycle();
}
