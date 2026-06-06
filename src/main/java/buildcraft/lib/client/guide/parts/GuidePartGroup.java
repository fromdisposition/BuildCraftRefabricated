package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.ref.GuideGroupSet;
import buildcraft.lib.gui.ISimpleDrawable;
import java.util.List;

public class GuidePartGroup extends GuidePart {
   public final GuideGroupSet group;
   private final GuideText[] texts;
   private final Object[] values;

   public GuidePartGroup(GuiGuide gui, GuideGroupSet group, GuideGroupSet.GroupDirection direction) {
      super(gui);
      this.group = group;
      List<PageValue<?>> groupValues = group.getValues(direction);
      this.values = new Object[groupValues.size()];

      for (int i = 0; i < this.values.length; i++) {
         this.values[i] = groupValues.get(i).value;
      }

      this.texts = new GuideText[1 + this.values.length];
      this.texts[0] = new GuideText(gui, group.getTitle(direction));
      int i = 1;

      for (PageValue<?> single : groupValues) {
         ISimpleDrawable icon = single.createDrawable();
         this.texts[i++] = new GuideText(gui, new PageLine(icon, icon, 1, single.title, true, single::getTooltip));
      }
   }

   @Override
   public int hashCode() {
      return this.group.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         GuidePartGroup other = (GuidePartGroup)obj;
         return this.group == other.group;
      } else {
         return false;
      }
   }

   @Override
   public void setFontRenderer(IFontRenderer fontRenderer) {
      super.setFontRenderer(fontRenderer);

      for (GuideText text : this.texts) {
         text.setFontRenderer(fontRenderer);
      }
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      if (this.getFontRenderer() != null) {
         current = current.guaranteeSpace(this.getFontRenderer().getMaxFontHeight() * 4, height);
      }

      for (GuideText text : this.texts) {
         current = text.renderIntoArea(x, y, width, height, current, index);
      }

      return current;
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      if (this.getFontRenderer() != null) {
         current = current.guaranteeSpace(this.getFontRenderer().getMaxFontHeight() * 4, height);
      }

      for (int i = 0; i < this.texts.length; i++) {
         GuideText text = this.texts[i];
         current = text.handleMouseClick(x, y, width, height, current, index, mouseX, mouseY);
         if (text.wasHovered && current.page == index && i > 0) {
            Object value = this.values[i - 1];
            GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(value);
            if (factory != null) {
               GuidePageBase newPage = factory.createNew(this.gui);
               if (newPage != null) {
                  this.gui.openPage(newPage);
                  return new GuidePart.PagePosition(536870911, 0);
               }
            }
         }
      }

      return current;
   }
}
