package buildcraft.lib.gui.pos;

public class PositionAdded implements IGuiPosition {
   private final IGuiPosition a;
   private final IGuiPosition b;

   public PositionAdded(IGuiPosition a, IGuiPosition b) {
      this.a = a;
      this.b = b;
   }

   @Override
   public double getX() {
      return this.a.getX() + this.b.getX();
   }

   @Override
   public double getY() {
      return this.a.getY() + this.b.getY();
   }
}
