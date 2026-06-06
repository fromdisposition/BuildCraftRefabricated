package buildcraft.lib.client.guide.font;

import java.util.List;

public interface IFontRenderer {
   int getStringWidth(String var1);

   int getFontHeight(String var1);

   int getMaxFontHeight();

   default int drawString(String text, int x, int y, int colour) {
      return this.drawString(text, x, y, colour, false, false, 1.0F);
   }

   default int drawString(String text, int x, int y, int colour, boolean shadow, boolean centered) {
      return this.drawString(text, x, y, colour, shadow, centered, 1.0F);
   }

   int drawString(String var1, int var2, int var3, int var4, boolean var5, boolean var6, float var7);

   List<String> wrapString(String var1, int var2, boolean var3, float var4);
}
