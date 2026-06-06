package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

public class GuideChapterWithin extends GuideChapter {
   private int lastPage = -1;
   private final boolean inlineRender;

   public GuideChapterWithin(GuiGuide gui, int level, String text) {
      this(gui, level, text, true);
   }

   public GuideChapterWithin(GuiGuide gui, String chapter) {
      this(gui, 0, chapter, true);
   }

   public GuideChapterWithin(GuiGuide gui, int level, String text, boolean inlineRender) {
      super(gui, level, text);
      this.inlineRender = inlineRender;
   }

   public GuideChapterWithin(GuiGuide gui, String chapter, boolean inlineRender) {
      this(gui, 0, chapter, inlineRender);
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      if (!this.inlineRender) {
         this.lastPage = current.page;
         return current;
      }

      GuidePart.PagePosition pos = super.renderIntoArea(x, y, width, height, current, index);
      this.lastPage = pos.page;
      if (pos.pixel == 0) {
         this.lastPage = pos.page - 1;
      }

      return pos;
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      return !this.inlineRender ? current : super.handleMouseClick(x, y, width, height, current, index, mouseX, mouseY);
   }

   @Override
   protected boolean onClick() {
      if (this.lastPage != -1) {
         GuidePageBase page = this.gui.getCurrentPage();
         if (page != null && page.getChapters().contains(this)) {
            page.goToPage(this.lastPage);
            return true;
         }
      }

      return false;
   }
}
