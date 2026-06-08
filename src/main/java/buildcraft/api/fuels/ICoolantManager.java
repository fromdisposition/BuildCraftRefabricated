/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.fuels;

import buildcraft.lib.fluids.FluidStack;
import java.util.Collection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface ICoolantManager {
   ICoolant addCoolant(ICoolant var1);

   ICoolant addCoolant(FluidStack var1, float var2);

   default ICoolant addCoolant(Fluid fluid, float degreesCoolingPerMb) {
      return this.addCoolant(new FluidStack(fluid, 1), degreesCoolingPerMb);
   }

   ISolidCoolant addSolidCoolant(ISolidCoolant var1);

   ISolidCoolant addSolidCoolant(ItemStack var1, FluidStack var2, float var3);

   Collection<ICoolant> getCoolants();

   Collection<ISolidCoolant> getSolidCoolants();

   ICoolant getCoolant(FluidStack var1);

   float getDegreesPerMb(FluidStack var1, float var2);

   ISolidCoolant getSolidCoolant(ItemStack var1);
}
