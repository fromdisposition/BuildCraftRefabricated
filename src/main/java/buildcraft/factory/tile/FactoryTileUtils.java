/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.lib.fabric.transfer.fluid.SingleFluidTank;
import buildcraft.lib.fluid.stack.FluidStack;
import net.minecraft.world.level.storage.ValueInput;

public final class FactoryTileUtils {
   private FactoryTileUtils() {
   }

   public static void loadTank(SingleFluidTank tank, ValueInput input, String key) {
      FluidStack fluid = input.read(key, FluidStack.CODEC).orElse(FluidStack.EMPTY);
      if (fluid.isEmpty()) {
         tank.setContents(FluidStack.EMPTY);
      } else {
         tank.setContents(fluid);
      }
   }
}
