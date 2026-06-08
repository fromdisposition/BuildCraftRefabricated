/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.transfer.fabric;

import buildcraft.lib.fabric.transfer.TransferCommits;
import buildcraft.lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public final class TransferConvert {
   public static final long DROPLETS_PER_MB = 81L;

   private TransferConvert() {
   }

   public static FluidVariant toVariant(FluidStack stack) {
      return stack.isEmpty() ? FluidVariant.blank() : FluidVariant.of(stack.getFluid(), stack.getComponentsPatch());
   }

   public static FluidStack toFluidStack(FluidVariant variant) {
      return variant.isBlank() ? FluidStack.EMPTY : new FluidStack(variant.getFluid(), 1, variant.getComponentsPatch());
   }

   public static FluidStack toFluidStack(FluidVariant variant, long droplets) {
      return !variant.isBlank() && droplets > 0L
         ? new FluidStack(variant.getFluid(), TransferCommits.saturateMb(dropletsToMb(droplets)), variant.getComponentsPatch())
         : FluidStack.EMPTY;
   }

   public static long dropletsToMb(long droplets) {
      return droplets / 81L;
   }

   public static long mbToDroplets(long millibuckets) {
      return millibuckets * 81L;
   }
}
