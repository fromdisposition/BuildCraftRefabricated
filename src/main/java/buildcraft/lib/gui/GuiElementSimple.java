package buildcraft.lib.gui;

import buildcraft.lib.gui.pos.IGuiArea;
import java.util.List;

public class GuiElementSimple implements IGuiElement {
   public final BuildCraftGui gui;
   private final IGuiArea element;
   public String name = null;

   public GuiElementSimple(BuildCraftGui gui, IGuiArea element) {
      this.gui = gui;
      this.element = element;
   }

   @Override
   public double getX() {
      return this.element.getX();
   }

   @Override
   public double getY() {
      return this.element.getY();
   }

   @Override
   public double getWidth() {
      return this.element.getWidth();
   }

   @Override
   public double getHeight() {
      return this.element.getHeight();
   }

   @Override
   public String getDebugInfo(List<String> info) {
      return this.name == null ? this.toString() : this.name;
   }
}
