package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.node.FormatString;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class GuideInlineLinkText extends GuidePart {
   private final String formattedText;
   private final List<GuideInlineLinkText.InlineLinkSpan> spans;
   private final List<List<GuiRectangle>> spanRects;

   public GuideInlineLinkText(GuiGuide gui, String formattedText, List<GuideInlineLinkText.InlineLinkSpan> spans) {
      super(gui);
      this.formattedText = formattedText;
      this.spans = spans;
      this.spanRects = new ArrayList<>(spans.size());

      for (int i = 0; i < spans.size(); i++) {
         this.spanRects.add(new ArrayList<>());
      }
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      return this.walk(x, y, width, height, current, index, true);
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      GuidePart.PagePosition end = this.walk(x, y, width, height, current, -1, false);
      if (this.wasHovered) {
         for (int i = 0; i < this.spans.size(); i++) {
            GuideInlineLinkText.InlineLinkSpan span = this.spans.get(i);

            for (GuiRectangle rect : this.spanRects.get(i)) {
               if (rect.contains(this.gui.mouse)) {
                  GuidePageFactory factory = span.factoryFn.apply(this.gui);
                  if (factory != null) {
                     this.gui.openPage(factory.createNew(this.gui));
                  }

                  return end;
               }
            }
         }
      }

      return end;
   }

   private GuidePart.PagePosition walk(int x, int y, int width, int height, GuidePart.PagePosition current, int pageRenderIndex, boolean draw) {
      this.wasHovered = false;

      for (List<GuiRectangle> list : this.spanRects) {
         list.clear();
      }

      IFontRenderer font = this.getFontRenderer();
      int allowedWidth = width;
      FormatString next = FormatString.split(this.formattedText);
      int neededSpace = font != null ? font.getFontHeight(this.formattedText) : 9;
      current = current.guaranteeSpace(neededSpace, height);
      int visibleConsumed = 0;
      List<GuideInlineLinkText.RowLayout> rows = new ArrayList<>();

      while (next != null) {
         FormatString[] strings = font != null ? next.wrap(font, allowedWidth) : new FormatString[]{next};
         String segText = strings[0].getFormatted();
         int segVisibleLen = visibleLengthOf(segText);
         int rowY = y + current.pixel;
         int rowTop = rowY - 5;
         boolean rendered = current.page == pageRenderIndex;
         Map<Integer, GuiRectangle> rectsBySpan = new HashMap<>();

         for (int i = 0; i < this.spans.size(); i++) {
            GuideInlineLinkText.InlineLinkSpan span = this.spans.get(i);
            int segVisibleStart = visibleConsumed;
            int segVisibleEnd = visibleConsumed + segVisibleLen;
            int spanVisibleStart = span.visibleStart;
            int spanVisibleEnd = span.visibleStart + span.visibleLen;
            int overlapStart = Math.max(spanVisibleStart, segVisibleStart);
            int overlapEnd = Math.min(spanVisibleEnd, segVisibleEnd);
            if (overlapStart < overlapEnd) {
               int linkStartInSegVisible = overlapStart - segVisibleStart;
               int linkEndInSegVisible = overlapEnd - segVisibleStart;
               int rawStart = visibleIndexToRawIndex(segText, linkStartInSegVisible);
               int rawEnd = visibleIndexToRawIndex(segText, linkEndInSegVisible);
               String beforeLink = segText.substring(0, rawStart);
               String linkText = segText.substring(rawStart, rawEnd);
               int offsetX = font != null ? font.getStringWidth(beforeLink) : 0;
               int linkWidth = font != null ? font.getStringWidth(linkText) : linkText.length() * 6;
               GuiRectangle rect = new GuiRectangle(x + offsetX, rowTop, linkWidth, 16.0);
               this.spanRects.get(i).add(rect);
               rectsBySpan.put(i, rect);
            }
         }

         rows.add(new GuideInlineLinkText.RowLayout(segText, rowY, rendered, rectsBySpan));
         visibleConsumed += segVisibleLen;
         next = strings.length == 1 ? null : strings[1];
         int fontHeight = font != null ? font.getFontHeight(segText) : 9;
         current = current.nextLine(fontHeight + 3, height);
      }

      boolean[] spanHovered = new boolean[this.spans.size()];

      for (int i = 0; i < this.spans.size(); i++) {
         for (GuiRectangle rect : this.spanRects.get(i)) {
            if (rect.contains(this.gui.mouse)) {
               spanHovered[i] = true;
               this.wasHovered = true;
               break;
            }
         }
      }

      boolean drewAny = false;
      if (draw) {
         BCGraphics g = GuiIcon.getGuiGraphics();

         for (GuideInlineLinkText.RowLayout row : rows) {
            if (row.rendered) {
               if (g != null) {
                  for (Entry<Integer, GuiRectangle> entry : row.rectsBySpan.entrySet()) {
                     if (spanHovered[entry.getKey()]) {
                        GuiRectangle rect = entry.getValue();
                        int rx = (int)rect.x;
                        int ry = (int)rect.y;
                        int rw = (int)rect.width;
                        g.fill(rx - 1, ry, rx + rw + 1, ry + 16, -2904724);
                     }
                  }
               }

               if (font != null) {
                  font.drawString(row.text, x, row.rowY, -16777216);
               }

               drewAny = true;
            }
         }
      }

      this.didRender = drewAny;
      int fontHeight = font != null ? font.getFontHeight(this.formattedText) : 9;
      int additional = 16 - fontHeight - 3;
      return current.nextLine(additional, height);
   }

   public static int visibleLengthOf(String s) {
      int count = 0;
      int i = 0;

      while (i < s.length()) {
         char c = s.charAt(i);
         if (c == 167 && i + 1 < s.length()) {
            i += 2;
         } else {
            count++;
            i++;
         }
      }

      return count;
   }

   public static int visibleIndexToRawIndex(String s, int visibleIndex) {
      int v = 0;
      int i = 0;

      while (i < s.length()) {
         char c = s.charAt(i);
         if (c == 167 && i + 1 < s.length()) {
            i += 2;
         } else {
            if (v == visibleIndex) {
               return i;
            }

            v++;
            i++;
         }
      }

      return s.length();
   }

   public static final class InlineLinkSpan {
      public final int visibleStart;
      public final int visibleLen;
      public final Function<GuiGuide, GuidePageFactory> factoryFn;
      public final String title;

      public InlineLinkSpan(int visibleStart, int visibleLen, Function<GuiGuide, GuidePageFactory> factoryFn, String title) {
         this.visibleStart = visibleStart;
         this.visibleLen = visibleLen;
         this.factoryFn = factoryFn;
         this.title = title;
      }
   }

   private static final class RowLayout {
      final String text;
      final int rowY;
      final boolean rendered;
      final Map<Integer, GuiRectangle> rectsBySpan;

      RowLayout(String text, int rowY, boolean rendered, Map<Integer, GuiRectangle> rectsBySpan) {
         this.text = text;
         this.rowY = rowY;
         this.rendered = rendered;
         this.rectsBySpan = rectsBySpan;
      }
   }
}
