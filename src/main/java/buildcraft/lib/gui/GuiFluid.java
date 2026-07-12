/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.client.fluid.FluidGuiRenderer;
import buildcraft.lib.fluid.stack.FluidStack;

public class GuiFluid implements ISimpleDrawable {
   private final FluidStack stack;
   private static BCGraphics currentGraphics;

   public static void setGuiGraphics(BCGraphics graphics) {
      currentGraphics = graphics;
   }

   public GuiFluid(FluidStack stack) {
      this.stack = stack;
   }

   public FluidStack getStack() {
      return this.stack;
   }

   @Override
   public void drawAt(double x, double y) {
      if (currentGraphics != null && this.stack != null && !this.stack.isEmpty()) {
         FluidGuiRenderer.drawFluidStack(currentGraphics, (int)x, (int)y, 16, 16, this.stack);
      }
   }
}
