/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.container;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.fluid.identity.FluidIdentity;
import buildcraft.lib.fluid.stack.FluidStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidContainers {
   private FluidContainers() {
   }

   public static ItemStack getFilledBucket(FluidStack fluidStack) {
      if (fluidStack != null && !fluidStack.isEmpty()) {
         if (fluidStack.getComponents().isEmpty()) {
            if (fluidStack.is(Fluids.WATER)) {
               return new ItemStack(Items.WATER_BUCKET);
            }

            if (fluidStack.is(Fluids.LAVA)) {
               return new ItemStack(Items.LAVA_BUCKET);
            }

            Fluid fluid = FluidIdentity.canonicalFluid(fluidStack.getFluid());
            BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluid);
            if (entry != null && entry.bucket() != null) {
               return new ItemStack(entry.bucket());
            }

            Item bucket = fluid.getBucket();
            if (bucket != null) {
               return new ItemStack(bucket);
            }
         }

         return ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }
}
