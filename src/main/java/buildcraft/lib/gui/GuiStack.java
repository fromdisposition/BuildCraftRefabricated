package buildcraft.lib.gui;

import net.minecraft.world.item.ItemStack;

public class GuiStack implements ISimpleDrawable {
   private final ItemStack stack;
   private static BCGraphics currentGraphics;

   public static void setGuiGraphics(BCGraphics graphics) {
      currentGraphics = graphics;
   }

   public GuiStack(ItemStack stack) {
      this.stack = stack;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   @Override
   public void drawAt(double x, double y) {
      if (currentGraphics != null && this.stack != null && !this.stack.isEmpty()) {
         currentGraphics.fakeItem(this.stack, (int)x, (int)y);
      }
   }
}
