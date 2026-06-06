package buildcraft.lib.gui.pos;

import java.util.function.DoubleSupplier;

public class AreaCallable implements IGuiArea {
   public final DoubleSupplier x;
   public final DoubleSupplier y;
   public final DoubleSupplier width;
   public final DoubleSupplier height;

   public AreaCallable(DoubleSupplier x, DoubleSupplier y, DoubleSupplier width, DoubleSupplier height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public AreaCallable(DoubleSupplier width, DoubleSupplier height) {
      this(() -> 0.0, () -> 0.0, width, height);
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
   public double getWidth() {
      return this.width.getAsDouble();
   }

   @Override
   public double getHeight() {
      return this.height.getAsDouble();
   }
}
