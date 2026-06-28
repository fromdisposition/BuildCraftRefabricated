/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
//? if >= 1.21.10 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}

public abstract class GuidePageBase extends GuidePart {
   private int index = 0;
   protected int numPages = -1;

   public GuidePageBase(GuiGuide gui) {
      super(gui);
   }

   protected void setupChapters() {
      List<GuideChapter> lastChapterAtLevel = new ArrayList<>();
      List<GuideChapter> chapters = this.getChapters();

      for (GuideChapter chapter : chapters) {
         chapter.parent = null;
         chapter.children.clear();
      }

      for (GuideChapter chapter : chapters) {
         int gap = chapter.level - lastChapterAtLevel.size();
         if (gap < 0) {
            lastChapterAtLevel.subList(chapter.level, lastChapterAtLevel.size()).clear();
         }

         for (int g = Math.min(chapter.level, lastChapterAtLevel.size()) - 1; g >= 0; g--) {
            GuideChapter parent = lastChapterAtLevel.get(g);
            if (parent != null) {
               parent.children.add(chapter);
               chapter.parent = parent;
               break;
            }
         }

         for (int g = 1; g < gap; g++) {
            lastChapterAtLevel.add(null);
         }

         lastChapterAtLevel.add(chapter);
      }

      int idx = 0;

      for (GuideChapter c : chapters) {
         if (!c.hasParent()) {
            c.colourIndex = idx++ % GuideChapter.COLOURS.length;
            if (c.hasChildren()) {
               c.assignChildIndices();
            }
         }
      }
   }

   protected final int getIndex() {
      return this.index;
   }

   public final void nextPage() {
      if (this.index + 2 < this.numPages) {
         this.index += 2;
      }
   }

   public final void lastPage() {
      this.index -= 2;
      if (this.index < 0) {
         this.index = 0;
      }
   }

   protected final void goToPage(int page) {
      if (this.numPages > 0 && page >= this.numPages) {
         page = this.numPages - 1;
      }

      this.index = page / 2;
      this.index *= 2;
      if (this.index < 0) {
         this.index = 0;
      }
   }

   public int getPage() {
      return this.index;
   }

   public int getPageCount() {
      return this.numPages;
   }

   public void tick() {
   }

   @Override
   public final GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      return current;
   }

   public abstract String getTitle();

   public boolean shouldPersistHistory() {
      return true;
   }

   @Nullable
   public GuidePageBase createReloaded() {
      return null;
   }

   public abstract List<GuideChapter> getChapters();

   protected GuidePart getClicked(Iterable<GuidePart> iterable, int x, int y, int width, int height, int mouseX, int mouseY, int index) {
      GuidePart.PagePosition pos = new GuidePart.PagePosition(0, 0);

      for (GuidePart part : iterable) {
         pos = part.renderIntoArea(x, y, width, height, pos, -1);
         if (pos.page == index && part.wasHovered) {
            return part;
         }

         if (pos.page > index) {
            return null;
         }
      }

      return null;
   }

   public void renderFirstPage(int x, int y, int width, int height) {
      this.renderPage(x, y, width, height, this.index);
   }

   public void renderSecondPage(int x, int y, int width, int height) {
      this.renderPage(x, y, width, height, this.index + 1);
   }

   protected void renderPage(int x, int y, int width, int height, int index) {
      IFontRenderer font = this.getFontRenderer();
      if (font != null && this.numPages > 0) {
         if (index % 2 == 0) {
            String text = index + 1 + " / " + this.numPages;
            double textX = x + GuiGuide.PAGE_LEFT_TEXT.getWidth() / 2.0 - font.getStringWidth(text) / 2;
            font.drawString(text, (int)textX, y + height + 6, -7306902);
         } else if (index + 1 <= this.numPages) {
            String text = index + 1 + " / " + this.numPages;
            double textX = x + (GuiGuide.PAGE_RIGHT_TEXT.getWidth() - font.getStringWidth(text)) / 2.0;
            font.drawString(text, (int)textX, y + height + 6, -7306902);
         }
      }
   }

   @Override
   public final GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      return current;
   }

   public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton, int index, boolean isEditing) {
   }

   @Override
   public final void handleMouseDragPartial(int startX, int startY, int currentX, int currentY, int button) {
   }

   @Override
   public final void handleMouseDragFinish(int startX, int startY, int endX, int endY, int button) {
   }

   public boolean keyTyped(char typedChar, int keyCode) {
      return false;
   }

   //? if >= 1.21.10 {
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      return false;
   }

   public boolean keyPressed(KeyEvent event) {
      return false;
   }

   public boolean charTyped(CharacterEvent event) {
      return false;
   }
   //?} else {
   /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return false;
   }

   public boolean charTyped(char chr, int modifiers) {
      return false;
   }
   *///?}
}
