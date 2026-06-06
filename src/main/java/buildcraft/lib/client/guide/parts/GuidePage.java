package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.font.IFontRenderer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.List;

public class GuidePage extends GuidePageBase {
   public final ImmutableList<GuidePart> parts;
   public final String title;
   public final GuideChapter chapterContents;
   public final PageValue<?> entry;

   public GuidePage(GuiGuide gui, List<GuidePart> parts, PageValue<?> entry) {
      super(gui);
      this.title = entry.title;
      this.chapterContents = new GuideChapterContents(gui);
      this.entry = entry;
      List<GuidePart> allParts = new ArrayList<>();
      allParts.add(new GuideChapterWithin(gui, this.title, false));
      allParts.addAll(parts);
      addTypeSpecific(gui, allParts, entry);
      this.parts = ImmutableList.copyOf(allParts);
      this.setupChapters();
   }

   private static <T> void addTypeSpecific(GuiGuide gui, List<GuidePart> parts, PageValue<T> entry) {
      entry.type.addPageEntries(entry.value, gui, parts);
   }

   @Override
   public List<GuideChapter> getChapters() {
      List<GuideChapter> list = new ArrayList<>();
      list.add(this.chapterContents);
      UnmodifiableIterator var2 = this.parts.iterator();

      while (var2.hasNext()) {
         GuidePart part = (GuidePart)var2.next();
         if (part instanceof GuideChapter) {
            list.add((GuideChapter)part);
         }
      }

      return list;
   }

   @Override
   public String getTitle() {
      return this.title;
   }

   @Override
   public void setFontRenderer(IFontRenderer fontRenderer) {
      super.setFontRenderer(fontRenderer);
      UnmodifiableIterator var2 = this.parts.iterator();

      while (var2.hasNext()) {
         GuidePart part = (GuidePart)var2.next();
         part.setFontRenderer(fontRenderer);
      }
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
      UnmodifiableIterator var1 = this.parts.iterator();

      while (var1.hasNext()) {
         GuidePart part = (GuidePart)var1.next();
         part.updateScreen();
      }
   }

   @Override
   protected void renderPage(int x, int y, int width, int height, int index) {
      super.renderPage(x, y, width, height, index);
      GuidePart.PagePosition part = new GuidePart.PagePosition(0, 0);
      UnmodifiableIterator var7 = this.parts.iterator();

      while (var7.hasNext()) {
         GuidePart guidePart = (GuidePart)var7.next();
         part = guidePart.renderIntoArea(x, y, width, height, part, index);
         if (this.numPages != -1 && part.page > index) {
            break;
         }
      }

      if (this.numPages == -1) {
         this.numPages = part.newPage().page;
      }
   }

   @Override
   public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton, int index, boolean isEditing) {
      super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);
      GuidePart.PagePosition part = new GuidePart.PagePosition(0, 0);
      UnmodifiableIterator var11 = this.parts.iterator();

      while (var11.hasNext()) {
         GuidePart guidePart = (GuidePart)var11.next();
         part = guidePart.handleMouseClick(x, y, width, height, part, index, mouseX, mouseY);
         if (this.numPages != -1 && part.page > index) {
            break;
         }
      }
   }
}
