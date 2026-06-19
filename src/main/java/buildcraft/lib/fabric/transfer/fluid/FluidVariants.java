/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer.fluid;

import buildcraft.lib.fabric.transfer.TransferCommits;
import buildcraft.lib.fluid.stack.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public final class FluidVariants {
   public static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000L;

   private FluidVariants() {
   }

   private static net.minecraft.core.component.DataComponentPatch componentsOf(FluidVariant variant) {
      //? if >= 26.1 {
      return variant.getComponentsPatch();
      //?} else {
      /*return variant.getComponents();
      *///?}
   }

   public static FluidVariant toVariant(FluidStack stack) {
      return stack.isEmpty() ? FluidVariant.blank() : FluidVariant.of(stack.getFluid(), stack.getComponentsPatch());
   }

   public static FluidStack toStack(FluidVariant variant) {
      return variant.isBlank() ? FluidStack.EMPTY : new FluidStack(variant.getFluid(), 1, componentsOf(variant));
   }

   public static FluidStack toStack(FluidVariant variant, long droplets) {
      return !variant.isBlank() && droplets > 0L
         ? new FluidStack(variant.getFluid(), TransferCommits.saturateMb(dropletsToMb(droplets)), componentsOf(variant))
         : FluidStack.EMPTY;
   }

   public static long dropletsToMb(long droplets) {
      return droplets / DROPLETS_PER_MB;
   }

   public static long mbToDroplets(long millibuckets) {
      return millibuckets * DROPLETS_PER_MB;
   }
}
