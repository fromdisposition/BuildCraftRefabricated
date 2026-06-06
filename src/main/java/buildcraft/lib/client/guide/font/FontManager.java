package buildcraft.lib.client.guide.font;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public enum FontManager {
   INSTANCE;

   private static final boolean ENABLE_SMOOTH_FONT = Boolean.getBoolean("buildcraft.guide.smooth_font");
   private final Map<String, IFontRenderer> fonts = new HashMap<>();

   public IFontRenderer getOrLoadFont(String name, int size) {
      return ENABLE_SMOOTH_FONT ? new GuideFont(new Font(name, 0, size)) : MinecraftFont.INSTANCE;
   }

   public void registerFont(String name, IFontRenderer font) {
      if (font == null) {
         throw new NullPointerException("font");
      }

      if (this.fonts.containsKey(name)) {
         throw new IllegalStateException("Cannot register the font \"" + name + "\" twice!");
      }

      this.fonts.put(name, font);
   }
}
