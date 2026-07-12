/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import buildcraft.lib.fluid.stack.FluidStack;
import net.minecraft.world.item.ItemStack;

public record CombustionCoolantJei(ItemStack item, FluidStack fluid, float coolingPerMb) {
   public boolean isSolid() {
      return !this.item.isEmpty();
   }
}
