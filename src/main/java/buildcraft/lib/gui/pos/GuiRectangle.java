package buildcraft.lib.gui.pos;

public final class GuiRectangle implements IGuiArea {
   public static final GuiRectangle ZERO = new GuiRectangle(0.0, 0.0, 0.0, 0.0);
   public final double x;
   public final double y;
   public final double width;
   public final double height;

   public GuiRectangle(double x, double y, double width, double height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public GuiRectangle(double width, double height) {
      this.x = 0.0;
      this.y = 0.0;
      this.width = width;
      this.height = height;
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
   public double getWidth() {
      return this.width;
   }

   @Override
   public double getHeight() {
      return this.height;
   }

   @Override
   public GuiRectangle asImmutable() {
      return this;
   }

   @Override
   public String toString() {
      return "Rectangle [x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", height=" + this.height + "]";
   }

   @Override
   public IGuiArea offset(IGuiPosition by) {
      return by instanceof PositionAbsolute ? this.offset(by.getX(), by.getY()) : IGuiArea.super.offset(by);
   }

   public GuiRectangle offset(double dx, double dy) {
      return new GuiRectangle(this.x + dx, this.y + dy, this.width, this.height);
   }

   public GuiRectangle expand(double dX, double dY) {
      return new GuiRectangle(this.x - dX, this.y - dY, this.width + dX * 2.0, this.height + dY * 2.0);
   }
}
