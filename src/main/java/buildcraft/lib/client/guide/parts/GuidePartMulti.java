package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import java.util.List;
import java.util.function.BooleanSupplier;

public class GuidePartMulti extends GuidePart {
   public final List<GuidePart> parts;
   public final BooleanSupplier visibleFuncion;

   public GuidePartMulti(GuiGuide gui, List<GuidePart> subParts, BooleanSupplier isVisible) {
      super(gui);
      this.parts = subParts;
      this.visibleFuncion = isVisible;
   }

   @Override
   public void setFontRenderer(IFontRenderer fontRenderer) {
      super.setFontRenderer(fontRenderer);

      for (GuidePart part : this.parts) {
         part.setFontRenderer(fontRenderer);
      }
   }

   protected boolean isVisible() {
      return this.visibleFuncion.getAsBoolean();
   }

   @Override
   public void updateScreen() {
      super.updateScreen();

      for (GuidePart part : this.parts) {
         part.updateScreen();
      }
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      if (this.isVisible()) {
         for (GuidePart part : this.parts) {
            current = part.renderIntoArea(x, y, width, height, current, index);
         }
      }

      return current;
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      if (this.isVisible()) {
         for (GuidePart part : this.parts) {
            current = part.handleMouseClick(x, y, width, height, current, index, mouseX, mouseY);
         }
      }

      return current;
   }
}
