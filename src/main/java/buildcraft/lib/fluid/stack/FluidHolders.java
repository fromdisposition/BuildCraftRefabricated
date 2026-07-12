/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.stack;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidHolders {
   private FluidHolders() {
   }

   public static Holder<Fluid> fluidHolder(Fluid fluid) {
      return BuiltInRegistries.FLUID.wrapAsHolder(fluid);
   }

   public static Holder<Fluid> emptyFluidHolder() {
      return fluidHolder(Fluids.EMPTY);
   }

   public static boolean isEmptyFluid(Holder<Fluid> holder) {
      return holder.value().isSame(Fluids.EMPTY);
   }

   public static Optional<Identifier> registryId(Holder<?> holder) {
      return holder.unwrapKey().map(buildcraft.lib.misc.RegistryKeyUtil::id);
   }
}
