/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.items;

import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fluid.stack.FluidStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class FluidItemDrops {
   public static IItemFluidShard item;

   public static void addFluidDrops(NonNullList<ItemStack> toDrop, FluidStack... fluids) {
      if (item != null) {
         for (FluidStack fluid : fluids) {
            item.addFluidDrops(toDrop, fluid);
         }
      }
   }

   @SafeVarargs
   public static void addFluidDrops(NonNullList<ItemStack> toDrop, SingleFluidTank... tanks) {
      if (item != null) {
         for (SingleFluidTank tank : tanks) {
            if (tank != null && !tank.isEmpty()) {
               item.addFluidDrops(toDrop, tank.getFluidStack());
            }
         }
      }
   }
}
