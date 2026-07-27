/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.gui.pos.GuiRectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public abstract class GuideChapter extends GuidePart {
   public static final int[] COLOURS = new int[]{10343872, 16433524, 2598109};
   public static final int MAX_HOWEVER_PROGRESS = 5;
   public static final int MAX_HOVER_DISTANCE = 20;
   private static final boolean FOLLOW_SIDE = false;
   public final PageLine chapter;
   public final int level;
   @Nullable
   protected GuideChapter parent;
   protected final List<GuideChapter> children = new ArrayList<>();
   protected int colourIndex = -1;
   protected GuideChapter.EnumGuiSide lastDrawn = null;
   private int hoverProgress = 0;
   private int hoverProgressLast = 0;
   private boolean expanded = false;

   public GuideChapter(GuiGuide gui, String chapter) {
      this(gui, 0, chapter);
   }

   public GuideChapter(GuiGuide gui, int level, String text) {
      super(gui);
      this.level = Math.max(0, level);
      this.chapter = new PageLine(null, null, this.level + 1, text, false);
   }

   private int getColour() {
      return this.colourIndex < 0 ? this.chapter.text.hashCode() : COLOURS[this.colourIndex % COLOURS.length];
   }

   private static int asARGB(int rgb) {
      return 0xFF000000 | rgb & 16777215;
   }

   public void reset() {
      this.lastDrawn = GuideChapter.EnumGuiSide.LEFT;
   }

   public boolean hasParent() {
      return this.parent != null;
   }

   public boolean hasChildren() {
      return !this.children.isEmpty();
   }

   void assignChildIndices() {
      int cIdx = 0;

      for (GuideChapter child : this.children) {
         if (cIdx % COLOURS.length == this.colourIndex) {
            cIdx++;
         }

         child.colourIndex = cIdx++ % COLOURS.length;
         if (child.hasChildren()) {
            child.assignChildIndices();
         }
      }
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      if (this.getFontRenderer() != null) {
         current = current.guaranteeSpace(this.getFontRenderer().getMaxFontHeight() * 4, height);
      }

      int colour = this.getColour();
      if (current.page == index) {
         int _x = x + 12;
         int _y = y + current.pixel;
         GuidePart.PagePosition n2 = this.renderLine(current, this.chapter, x, y, width, height, -1);
         int _height = n2.pixel - current.pixel;
         GuiGuide.CHAPTER_MARKER_9.drawTinted(_x - 5, _y - 4, width - 24, _height, asARGB(colour));
      }

      GuidePart.PagePosition n = this.renderLine(current, this.chapter, x, y, width, height, index);
      int halfIndex = index / 2;
      if (n.page / 2 < halfIndex) {
         this.lastDrawn = GuideChapter.EnumGuiSide.LEFT;
      } else if (halfIndex == current.page / 2) {
         this.lastDrawn = GuideChapter.EnumGuiSide.LEFT;
      }

      if (this.lastDrawn != null && this.parent != null) {
         for (GuideChapter p = this.parent; p != null; p = p.parent) {
            if (p.lastDrawn == null) {
               p.lastDrawn = this.lastDrawn;
            }
         }
      }

      return n;
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      if (this.getFontRenderer() != null) {
         current = current.guaranteeSpace(this.getFontRenderer().getMaxFontHeight() * 4, height);
      }

      return this.renderLine(current, this.chapter, x, y, width, height, -1);
   }

   public int draw(int yIndex, float partialTicks, boolean drawCentral) {
      IFontRenderer font = this.gui.getCurrentFont();
      if (font == null) {
         return 1;
      }

      float hoverWidth = this.getHoverWidth(partialTicks);
      int colour = this.getColour();
      int argb = asARGB(colour);
      boolean hasChildren = !this.children.isEmpty();
      int arrowOffset = hasChildren ? 16 : 0;
      int lineStride = font.getMaxFontHeight() + 8;
      int baseY = drawCentral ? (int)GuiGuide.FLOATING_CHAPTER_MENU.getY() + 6 : this.gui.minY;
      int y = baseY + lineStride * (yIndex + 1);
      List<String> lines = drawCentral ? Collections.singletonList(this.chapter.text) : this.getWrappedTitle();
      int effectiveTextW = effectiveTextWidth(font, lines);
      int drawnCount = lines.size();
      int spriteFullHeight = lines.size() * lineStride - 2;
      float width = effectiveTextW + hoverWidth;
      float _width = effectiveTextW + 12 + hoverWidth + arrowOffset;
      int childHeight = 0;
      if (hasChildren && this.expanded) {
         childHeight = spriteFullHeight + this.getChildrenFullHeight();
      }

      if (!drawCentral && this.lastDrawn != GuideChapter.EnumGuiSide.RIGHT) {
         if (this.lastDrawn == GuideChapter.EnumGuiSide.LEFT) {
            float x = this.gui.minX - width + 5.0F;
            if (hasChildren) {
               x -= 16.0F;
            }

            if (childHeight > 0) {
               GuiGuide.CHAPTER_MARKER_9_LEFT.drawTinted(x + 10.0F, y + spriteFullHeight - 12, _width - 16.0F, childHeight, argb);
            }

            GuiGuide.CHAPTER_MARKER_9_LEFT.drawTinted(x - 6.0F, y - 4, _width, spriteFullHeight, argb);
            if (hasChildren) {
               (this.expanded ? GuiGuide.EXPANDED_ARROW : GuiGuide.CLOSED_ARROW).drawAt(x - 6.0F, y - 4);
               x += 16.0F;
               if (this.expanded) {
                  for (GuideChapter child : this.children) {
                     GuideChapter.EnumGuiSide old = child.lastDrawn;
                     child.lastDrawn = this.lastDrawn;
                     drawnCount += child.draw(yIndex + drawnCount, partialTicks, drawCentral);
                     child.lastDrawn = old;
                  }
               }
            }

            for (int i = 0; i < lines.size(); i++) {
               font.drawString(lines.get(i), (int)x, y + i * lineStride, -16777216);
            }
         }
      } else {
         float x;
         if (drawCentral) {
            x = (float)GuiGuide.FLOATING_CHAPTER_MENU.getX() + 4.0F + hoverWidth;
            _width -= hoverWidth;
            hoverWidth = 0.0F;
         } else {
            x = this.gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;
         }

         x += this.level * 14;

         for (GuideChapter p = this.parent; p != null; p = p.parent) {
            x += p.getHoverWidth(partialTicks);
         }

         SpriteNineSliced icon = drawCentral ? GuiGuide.CHAPTER_MARKER_9 : GuiGuide.CHAPTER_MARKER_9_RIGHT;
         if (childHeight > 0) {
            icon.drawTinted(x + 10.0F, y + spriteFullHeight - 12, _width - 16.0F, childHeight, argb);
         }

         icon.drawTinted(x, y - 4, _width, spriteFullHeight, argb);
         if (hasChildren) {
            (this.expanded ? GuiGuide.EXPANDED_ARROW : GuiGuide.CLOSED_ARROW).drawAt(x + hoverWidth, y - 4);
            x += 16.0F;
            if (this.expanded) {
               for (GuideChapter child : this.children) {
                  GuideChapter.EnumGuiSide old = child.lastDrawn;
                  child.lastDrawn = this.lastDrawn;
                  drawnCount += child.draw(yIndex + drawnCount, partialTicks, drawCentral);
                  child.lastDrawn = old;
               }
            }
         }

         for (int i = 0; i < lines.size(); i++) {
            font.drawString(lines.get(i), (int)(x + 6.0F + hoverWidth), y + i * lineStride, -16777216);
         }
      }

      return drawnCount;
   }

   public int getDrawnSlotCount() {
      return this.getWrappedTitle().size();
   }

   private List<String> getWrappedTitle() {
      IFontRenderer font = this.gui.getCurrentFont();
      if (font == null) {
         return Collections.singletonList(this.chapter.text);
      }

      if (this.lastDrawn == null) {
         return Collections.singletonList(this.chapter.text);
      }

      int maxLineW = this.computeMaxLineWidth();
      return maxLineW < 1073741823 && font.getStringWidth(this.chapter.text) > maxLineW
         ? font.wrapString(this.chapter.text, maxLineW, false, 1.0F)
         : Collections.singletonList(this.chapter.text);
   }

   private static int effectiveTextWidth(IFontRenderer font, List<String> lines) {
      int max = 0;

      for (String line : lines) {
         max = Math.max(max, font.getStringWidth(line));
      }

      return max;
   }

   private int computeMaxLineWidth() {
      int arrowOffset = this.hasChildren() ? 16 : 0;
      if (this.lastDrawn == GuideChapter.EnumGuiSide.LEFT) {
         return Math.max(20, this.gui.minX - 1 - arrowOffset);
      } else {
         return this.lastDrawn == GuideChapter.EnumGuiSide.RIGHT
            ? Math.max(20, this.gui.width - this.gui.minX - GuiGuide.PAGE_LEFT.width - GuiGuide.PAGE_RIGHT.width - 1 - arrowOffset)
            : Integer.MAX_VALUE;
      }
   }

   private int getChildrenFullHeight() {
      if (!this.expanded) {
         return 0;
      }

      int fullHeight = 0;
      IFontRenderer font = this.gui.getCurrentFont();
      if (font == null) {
         return 0;
      }

      int lineStride = font.getMaxFontHeight() + 8;

      for (GuideChapter c : this.children) {
         fullHeight += c.getDrawnSlotCount() * lineStride - 2;
         fullHeight += c.getChildrenFullHeight();
         fullHeight += 2;
      }

      return fullHeight;
   }

   protected int getMousePart() {
      IFontRenderer font = this.gui.getCurrentFont();
      if (font == null) {
         return 0;
      }

      for (GuideChapter p = this.parent; p != null; p = p.parent) {
         if (!p.expanded) {
            return 0;
         }
      }

      List<String> lines = this.getWrappedTitle();
      int effectiveTextW = effectiveTextWidth(font, lines);
      float hoverWidth = this.getHoverWidth(0.0F);
      float realHoverWidth = hoverWidth;
      int width = (int)(effectiveTextW + hoverWidth) + (this.children.isEmpty() ? 0 : 16);
      int chapterIndex = 1;

      for (GuideChapter c : this.gui.getChapters()) {
         if (c == this) {
            break;
         }

         boolean visible = true;
         GuideChapter cp = c.parent;

         while (true) {
            if (cp != null) {
               if (cp.expanded) {
                  cp = cp.parent;
                  continue;
               }

               visible = false;
            }

            if (visible) {
               chapterIndex += c.getDrawnSlotCount();
            }
            break;
         }
      }

      boolean isCentral = this.gui.isSmallScreen();
      int lineStride = font.getMaxFontHeight() + 8;
      int baseY = isCentral ? (int)GuiGuide.FLOATING_CHAPTER_MENU.getY() + 6 : this.gui.minY;
      int y = baseY + lineStride * chapterIndex;
      int rectHeight = lines.size() * lineStride - 1;
      if (!isCentral && this.lastDrawn != GuideChapter.EnumGuiSide.RIGHT) {
         if (this.lastDrawn == GuideChapter.EnumGuiSide.LEFT) {
            int x = this.gui.minX - width - 5;
            GuiRectangle drawRect = new GuiRectangle(x, y - 4, width + 16, rectHeight);
            if (drawRect.contains(this.gui.mouse)) {
               if (this.hasChildren() && new GuiRectangle(x, y - 4, 24.0, 16.0).contains(this.gui.mouse)) {
                  return 2;
               }

               return 1;
            }
         }
      } else {
         int x;
         if (isCentral) {
            x = (int)GuiGuide.FLOATING_CHAPTER_MENU.getX() + 4 + (int)hoverWidth;
            width -= (int)hoverWidth;
            hoverWidth = 0.0F;
         } else {
            x = this.gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;
         }

         x += this.level * 14;

         for (GuideChapter var17 = this.parent; var17 != null; var17 = var17.parent) {
            x += (int)var17.getHoverWidth(0.0F);
         }

         GuiRectangle drawRect = new GuiRectangle(x - realHoverWidth, y - 4, width + 16, rectHeight);
         if (drawRect.contains(this.gui.mouse)) {
            GuiRectangle arrowRect = new GuiRectangle(x - realHoverWidth, y - 4, 24.0F + realHoverWidth, 16.0);
            if (this.hasChildren() && arrowRect.contains(this.gui.mouse)) {
               return 2;
            }

            return 1;
         }
      }

      return 0;
   }

   private float getHoverWidth(float partialTicks) {
      if (this.hasChildren()) {
         return 0.0F;
      }

      float prog = partialTicks * this.hoverProgress + (1.0F - partialTicks) * this.hoverProgressLast;
      float raw = prog * 20.0F / 5.0F;
      return Math.min(raw, this.getMaxAllowedHoverWidth());
   }

   private float getMaxAllowedHoverWidth() {
      IFontRenderer font = this.gui.getCurrentFont();
      if (font == null) {
         return 20.0F;
      } else {
         int textW = effectiveTextWidth(font, this.getWrappedTitle());
         int arrowOffset = this.hasChildren() ? 16 : 0;
         if (this.lastDrawn == GuideChapter.EnumGuiSide.LEFT) {
            return Math.max(0, this.gui.minX - textW - 1 - arrowOffset);
         } else if (this.lastDrawn == GuideChapter.EnumGuiSide.RIGHT) {
            int rightLimit = this.gui.width - this.gui.minX - GuiGuide.PAGE_LEFT.width - GuiGuide.PAGE_RIGHT.width - textW - arrowOffset - 1;
            return Math.max(0, rightLimit);
         } else {
            return 20.0F;
         }
      }
   }

   public int handleClick() {
      int part = this.getMousePart();
      if (part == 1) {
         return this.onClick() ? 1 : 0;
      } else if (part == 2) {
         this.expanded = !this.expanded;
         return 2;
      } else {
         return 0;
      }
   }

   protected abstract boolean onClick();

   @Override
   public void updateScreen() {
      this.hoverProgressLast = this.hoverProgress;
      if (this.getMousePart() != 0) {
         this.hoverProgress += 2;
         if (this.hoverProgress > 5) {
            this.hoverProgress = 5;
         }
      } else {
         this.hoverProgress--;
         if (this.hoverProgress < 0) {
            this.hoverProgress = 0;
         }
      }
   }

   public enum EnumGuiSide {
      LEFT,
      RIGHT;
   }
}
