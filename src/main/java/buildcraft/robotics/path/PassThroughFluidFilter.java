/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.path;

import net.minecraft.world.level.material.Fluid;

public class PassThroughFluidFilter implements IFluidFilter {
   public static final PassThroughFluidFilter INSTANCE = new PassThroughFluidFilter();

   @Override
   public boolean matches(Fluid fluid) {
      return fluid != null;
   }
}
