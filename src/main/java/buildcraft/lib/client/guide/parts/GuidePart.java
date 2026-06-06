package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.node.FormatString;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import java.util.ArrayList;
import java.util.List;

public abstract class GuidePart {
   public static final int INDENT_WIDTH = 16;
   public static final int LINE_HEIGHT = 16;
   protected final GuiGuide gui;
   private IFontRenderer fontRenderer;
   protected boolean wasHovered = false;
   protected boolean wasIconHovered = false;
   protected boolean didRender = false;

   public GuidePart(GuiGuide gui) {
      this.gui = gui;
   }

   public IFontRenderer getFontRenderer() {
      return this.fontRenderer;
   }

   public void setFontRenderer(IFontRenderer fontRenderer) {
      this.fontRenderer = fontRenderer;
   }

   public boolean wasHovered() {
      return this.wasHovered;
   }

   public void updateScreen() {
   }

   protected void renderTextLine(String text, int x, int y, int colour) {
      if (this.fontRenderer != null) {
         this.fontRenderer.drawString(text, x, y + 8 - this.fontRenderer.getFontHeight(text) / 2, colour);
      }
   }

   public abstract GuidePart.PagePosition renderIntoArea(int var1, int var2, int var3, int var4, GuidePart.PagePosition var5, int var6);

   public abstract GuidePart.PagePosition handleMouseClick(int var1, int var2, int var3, int var4, GuidePart.PagePosition var5, int var6, int var7, int var8);

   public void handleMouseDragPartial(int startX, int startY, int currentX, int currentY, int button) {
   }

   public void handleMouseDragFinish(int startX, int startY, int endX, int endY, int button) {
   }

   protected GuidePart.PagePosition renderLine(GuidePart.PagePosition current, PageLine line, int x, int y, int width, int height, int pageRenderIndex) {
      this.wasHovered = false;
      this.wasIconHovered = false;
      int allowedWidth = width - 16 * line.indent;
      if (allowedWidth <= 0) {
         throw new IllegalStateException("Was indented too far");
      }

      String toRender = line.text;
      ISimpleDrawable icon = line.startIcon;
      FormatString next = FormatString.split(line.text);
      int neededSpace = this.fontRenderer != null ? this.fontRenderer.getFontHeight(line.text) : 9;
      if (icon != null) {
         neededSpace = Math.max(16, neededSpace);
      }

      current = current.guaranteeSpace(neededSpace, height);
      int _x = x + 16 * line.indent;
      int iconX = _x - 18;
      int iconY = y + current.pixel - 5;
      GuiRectangle iconRect = icon != null ? new GuiRectangle(_x - 20, iconY, 20.0, 16.0) : null;
      boolean iconVisibleHere = icon != null && current.page == pageRenderIndex;
      this.didRender = false;
      List<GuidePart.WrapSegment> segments = new ArrayList<>();
      GuidePart.PagePosition cursor = current;

      while (next != null) {
         FormatString[] strings = this.fontRenderer != null ? next.wrap(this.fontRenderer, allowedWidth) : new FormatString[]{next};
         String text = strings[0].getFormatted();
         int _y = y + cursor.pixel;
         int _w = this.fontRenderer != null ? this.fontRenderer.getStringWidth(text) : text.length() * 6;
         int rowTop = _y - 5;
         GuiRectangle rect = new GuiRectangle(_x - 2, rowTop, _w + 4, 16.0);
         boolean rendered = cursor.page == pageRenderIndex;
         segments.add(new GuidePart.WrapSegment(text, rect, _y, rowTop, _w, rendered));
         next = strings.length == 1 ? null : strings[1];
         int fontHeight = this.fontRenderer != null ? this.fontRenderer.getFontHeight(text) : 9;
         cursor = cursor.nextLine(fontHeight + 3, height);
      }

      current = cursor;
      boolean iconHovered = iconRect != null && iconRect.contains(this.gui.mouse);
      boolean entryHovered = iconHovered;
      if (!entryHovered) {
         for (GuidePart.WrapSegment seg : segments) {
            if (seg.rect.contains(this.gui.mouse)) {
               entryHovered = true;
               break;
            }
         }
      }

      this.wasHovered = entryHovered;
      this.wasIconHovered = iconHovered;
      boolean drewAny = false;
      if (entryHovered && line.link) {
         BCGraphics g = GuiIcon.getGuiGraphics();
         if (g != null) {
            boolean isFirstRendered = true;

            for (GuidePart.WrapSegment seg : segments) {
               if (seg.rendered) {
                  int fillLeft = isFirstRendered && iconVisibleHere ? _x - 20 : _x - 2;
                  g.fill(fillLeft, seg.rowTop, _x + seg.width + 2, seg.rowTop + 16, -2904724);
                  isFirstRendered = false;
               }
            }
         }
      }

      if (iconVisibleHere) {
         ISimpleDrawable toDraw = entryHovered && line.startIconHovered != null ? line.startIconHovered : icon;
         toDraw.drawAt(iconX, iconY);
      }

      for (GuidePart.WrapSegment seg : segments) {
         if (seg.rendered) {
            drewAny = true;
            if (this.fontRenderer != null) {
               this.fontRenderer.drawString(seg.text, _x, seg.y, -16777216);
            }
         }
      }

      this.didRender = drewAny;
      if (entryHovered && this.didRender) {
         this.renderTooltip();
      }

      int fontHeight = this.fontRenderer != null ? this.fontRenderer.getFontHeight(toRender) : 9;
      int additional = 16 - fontHeight - 3;
      return current.nextLine(additional, height);
   }

   protected GuidePart.PagePosition renderLines(Iterable<PageLine> lines, GuidePart.PagePosition part, int x, int y, int width, int height, int index) {
      for (PageLine line : lines) {
         part = this.renderLine(part, line, x, y, width, height, index);
      }

      return part;
   }

   protected GuidePart.PagePosition renderLines(Iterable<PageLine> lines, int x, int y, int width, int height, int index) {
      return this.renderLines(lines, new GuidePart.PagePosition(0, 0), x, y, width, height, index);
   }

   protected void renderTooltip() {
   }

   public static class PagePosition {
      public final int page;
      public final int pixel;

      public PagePosition(int page, int pixel) {
         this.page = page;
         this.pixel = pixel;
      }

      public GuidePart.PagePosition nextLine(int pixelDifference, int maxHeight) {
         int added = this.pixel + pixelDifference;
         return added >= maxHeight ? this.nextPage() : new GuidePart.PagePosition(this.page, added);
      }

      public GuidePart.PagePosition guaranteeSpace(int required, int maxPageHeight) {
         GuidePart.PagePosition next = this.nextLine(required, maxPageHeight);
         return next.page == this.page ? this : next;
      }

      public GuidePart.PagePosition nextPage() {
         return new GuidePart.PagePosition(this.page + 1, 0);
      }

      public GuidePart.PagePosition newPage() {
         return this.pixel != 0 ? this.nextPage() : this;
      }
   }

   private static final class WrapSegment {
      final String text;
      final GuiRectangle rect;
      final int y;
      final int rowTop;
      final int width;
      final boolean rendered;

      WrapSegment(String text, GuiRectangle rect, int y, int rowTop, int width, boolean rendered) {
         this.text = text;
         this.rect = rect;
         this.y = y;
         this.rowTop = rowTop;
         this.width = width;
         this.rendered = rendered;
      }
   }
}
