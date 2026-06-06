package buildcraft.lib.misc;

import buildcraft.lib.client.texture.BcTextureAtlases;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class SpriteUtil {
   private static final Identifier MISSING = Identifier.withDefaultNamespace("missingno");

   public static TextureAtlasSprite missingSprite() {
      return getSprite(MISSING);
   }

   public static TextureAtlasSprite getSprite(String name) {
      return getSprite(Identifier.parse(name));
   }

   public static TextureAtlasSprite getSprite(Identifier loc) {
      return BcTextureAtlases.getBlockSprite(loc);
   }
}
