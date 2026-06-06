package buildcraft.lib.gui;

import buildcraft.lib.client.fluid.BcFluidGuiDrawer;
import buildcraft.lib.fluids.FluidStack;

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
         BcFluidGuiDrawer.drawFluidStack(currentGraphics, (int)x, (int)y, 16, 16, this.stack);
      }
   }
}
