package buildcraft.lib.gui.pos;

import java.util.function.DoubleSupplier;

public class PositionCallable implements IGuiPosition {
   private final DoubleSupplier x;
   private final DoubleSupplier y;

   public PositionCallable(DoubleSupplier x, double y) {
      this(x, () -> y);
   }

   public PositionCallable(double x, DoubleSupplier y) {
      this(() -> x, y);
   }

   public PositionCallable(DoubleSupplier x, DoubleSupplier y) {
      this.x = x;
      this.y = y;
   }

   @Override
   public double getX() {
      return this.x.getAsDouble();
   }

   @Override
   public double getY() {
      return this.y.getAsDouble();
   }

   @Override
   public String toString() {
      return "{ " + this.x + ", " + this.y + " }";
   }
}
