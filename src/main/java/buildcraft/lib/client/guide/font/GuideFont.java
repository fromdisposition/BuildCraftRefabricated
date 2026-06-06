package buildcraft.lib.client.guide.font;

import java.awt.Font;
import java.util.List;

public class GuideFont implements IFontRenderer {
   public GuideFont(Font font) {
   }

   @Override
   public int getStringWidth(String text) {
      return MinecraftFont.INSTANCE.getStringWidth(text);
   }

   @Override
   public int getFontHeight(String text) {
      return MinecraftFont.INSTANCE.getFontHeight(text);
   }

   @Override
   public int getMaxFontHeight() {
      return MinecraftFont.INSTANCE.getMaxFontHeight();
   }

   @Override
   public int drawString(String text, int x, int y, int colour, boolean shadow, boolean centered, float scale) {
      return MinecraftFont.INSTANCE.drawString(text, x, y, colour, shadow, centered, scale);
   }

   @Override
   public List<String> wrapString(String text, int maxWidth, boolean shadow, float scale) {
      return MinecraftFont.INSTANCE.wrapString(text, maxWidth, shadow, scale);
   }
}
