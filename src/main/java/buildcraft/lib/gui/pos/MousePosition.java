package buildcraft.lib.gui.pos;

public final class MousePosition implements IGuiPosition {
   private double x = -10.0;
   private double y = -10.0;

   public void setMousePosition(double mouseX, double mouseY) {
      this.x = mouseX;
      this.y = mouseY;
   }

   @Override
   public double getX() {
      return this.x;
   }

   @Override
   public double getY() {
      return this.y;
   }

   @Override
   public String toString() {
      return "InputConstants [" + this.x + "," + this.y + "]";
   }
}
