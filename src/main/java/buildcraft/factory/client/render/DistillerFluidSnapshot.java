/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.fluid.registry.FluidSmoother;
import buildcraft.lib.fluid.stack.FluidStack;
import javax.annotation.Nullable;

public record DistillerFluidSnapshot(FluidStack fluid, double amount, int capacity, BcFluidAppearance appearance) {
   @Nullable
   public static DistillerFluidSnapshot from(FluidSmoother smoother, float partialTick) {
      FluidSmoother.FluidStackInterp interp = smoother.getFluidForRender(partialTick);
      if (interp == null || interp.amount() <= 0.0) {
         return null;
      }

      FluidStack fluid = interp.fluid();
      int capacity = smoother.getCapacity();
      if (capacity <= 0) {
         return null;
      }

      BcFluidAppearance appearance = BcFluidAppearanceCache.get(fluid);
      return appearance == null ? null : new DistillerFluidSnapshot(fluid, interp.amount(), capacity, appearance);
   }
}
