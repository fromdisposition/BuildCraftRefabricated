package buildcraft.lib.client.guide.parts;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.sprite.SpriteRaw;
import net.minecraft.resources.Identifier;

public class GuideImageFactory implements GuidePartFactory {
   private final ISprite sprite;
   private final int srcWidth;
   private final int srcHeight;
   private final int width;
   private final int height;

   public GuideImageFactory(String location) {
      this(location, -1, -1);
   }

   public GuideImageFactory(String location, int width, int height) {
      int sw = 16;
      int sh = 16;

      ISprite s;
      try {
         Identifier resLoc = resolveTexturePath(location);
         s = new SpriteRaw(resLoc, 0.0, 0.0, 1.0, 1.0);
      } catch (Exception e) {
         BCLog.logger.warn("[lib.guide.loader.image] Couldn't load image '" + location + "': " + e.getMessage());
         s = new SpriteRaw(Identifier.parse("buildcraftlib:missing"), 0.0, 0.0, 1.0, 1.0);
      }

      this.sprite = s;
      this.srcWidth = sw;
      this.srcHeight = sh;
      this.width = width <= 0 ? this.srcWidth : width;
      this.height = height <= 0 ? this.srcHeight : height;
   }

   private static Identifier resolveTexturePath(String location) {
      Identifier parsed = Identifier.parse(location);
      String path = parsed.getPath();
      if (!path.startsWith("textures/")) {
         path = "textures/" + path;
      }

      if (!path.endsWith(".png")) {
         path = path + ".png";
      }

      return Identifier.fromNamespaceAndPath(parsed.getNamespace(), path);
   }

   public GuideImage createNew(GuiGuide gui) {
      return new GuideImage(gui, this.sprite, this.srcWidth, this.srcHeight, this.width, this.height);
   }
}
