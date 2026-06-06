package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import net.minecraft.client.resources.language.I18n;

public class GuideChapterContents extends GuideChapter {
   public GuideChapterContents(GuiGuide gui) {
      super(gui, I18n.get("buildcraft.guide.chapter.contents", new Object[0]));
   }

   @Override
   public void reset() {
      this.lastDrawn = GuideChapter.EnumGuiSide.LEFT;
   }

   @Override
   protected boolean onClick() {
      this.gui.goBackToMenu();
      return true;
   }
}
