/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

public final class FluidPipeSectionLimits {
   private FluidPipeSectionLimits() {
   }

   public static int maxFilled(int capacity, int amount, int transferPerTick, int incomingAtCurrentTime) {
      int availableTotal = capacity - amount;
      int availableThisTick = transferPerTick - incomingAtCurrentTime;
      return Math.min(availableTotal, availableThisTick);
   }

   public static int maxDrained(int amount, int incomingTotalCache, int transferPerTick) {
      return Math.min(amount - incomingTotalCache, transferPerTick);
   }
}
