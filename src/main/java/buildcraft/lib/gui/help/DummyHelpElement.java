package buildcraft.lib.gui.help;

import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;
import java.util.List;

public class DummyHelpElement implements IGuiElement {
   public final IGuiArea area;
   public final ElementHelpInfo help;

   public DummyHelpElement(IGuiArea area, ElementHelpInfo help) {
      this.area = area;
      this.help = help;
   }

   @Override
   public double getX() {
      return this.area.getX();
   }

   @Override
   public double getY() {
      return this.area.getY();
   }

   @Override
   public double getWidth() {
      return this.area.getWidth();
   }

   @Override
   public double getHeight() {
      return this.area.getHeight();
   }

   @Override
   public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
      elements.add(this.help.target(this.area));
   }
}
