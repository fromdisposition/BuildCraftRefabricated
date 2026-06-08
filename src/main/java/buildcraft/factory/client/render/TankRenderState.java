/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.fluid.FluidClientCache;
import buildcraft.lib.client.render.tile.BcBerState;
import buildcraft.lib.fluids.FluidStack;

public class TankRenderState extends BcBerState<TileTank> {
   public boolean hasFluid;
   public FluidClientCache.Appearance appearance;
   public FluidStack fluid;
   public double amount;
   public int capacity;
   public float minY;
   public float maxYFull;
   public boolean renderTop;
   public boolean renderBottom;
}
