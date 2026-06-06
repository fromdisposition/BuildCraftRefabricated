package buildcraft.lib.gui;

import buildcraft.lib.gui.pos.IGuiPosition;

@FunctionalInterface
public interface ISimpleDrawable {
   void drawAt(double var1, double var3);

   default void drawAt(IGuiPosition element) {
      this.drawAt(element.getX(), element.getY());
   }

   default ISimpleDrawable andThen(ISimpleDrawable after) {
      ISimpleDrawable t = this;
      return (x, y) -> {
         t.drawAt(x, y);
         after.drawAt(x, y);
      };
   }
}
