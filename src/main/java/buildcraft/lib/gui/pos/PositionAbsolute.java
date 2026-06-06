package buildcraft.lib.gui.pos;

import java.util.Arrays;

public final class PositionAbsolute implements IGuiPosition {
   public static final PositionAbsolute ORIGIN = new PositionAbsolute(0.0, 0.0);
   private final double x;
   private final double y;

   public PositionAbsolute(double x, double y) {
      this.x = x;
      this.y = y;
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
   public IGuiPosition offset(double xOffset, double yOffset) {
      return new PositionAbsolute(xOffset + this.x, yOffset + this.y);
   }

   @Override
   public IGuiPosition offset(IGuiPosition by) {
      return by.offset(this.x, this.y);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(new double[]{this.x, this.y});
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      PositionAbsolute other = (PositionAbsolute)obj;
      return this.x == other.x && this.y == other.y;
   }

   @Override
   public String toString() {
      return "{ " + this.x + ", " + this.y + " }";
   }
}
