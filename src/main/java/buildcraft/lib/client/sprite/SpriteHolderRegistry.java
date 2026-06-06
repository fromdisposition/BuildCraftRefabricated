package buildcraft.lib.client.sprite;

import buildcraft.api.core.render.ISprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

public class SpriteHolderRegistry {
   public void registerInitialSprites() {
   }

   public static SpriteHolderRegistry.SpriteHolder getHolder(String location) {
      return new SpriteHolderRegistry.SpriteHolder(location);
   }

   private static final class AtlasLookup {
      static final Identifier[] ORDER = new Identifier[]{TextureAtlas.LOCATION_BLOCKS, TextureAtlas.LOCATION_ITEMS, Sheets.GUI_SHEET};
   }

   public static class SpriteHolder implements ISprite {
      private final String location;
      private final Identifier resourceLocation;
      private TextureAtlasSprite cachedSprite;

      public SpriteHolder(String location) {
         this.location = location;
         this.resourceLocation = Identifier.parse(location);
      }

      public String getLocation() {
         return this.location;
      }

      public Identifier getResourceLocation() {
         return this.resourceLocation;
      }

      public TextureAtlasSprite getSprite() {
         if (this.cachedSprite == null) {
            try {
               this.cachedSprite = this.resolveSprite();
            } catch (Exception e) {
               return null;
            }
         }

         return this.cachedSprite;
      }

      public Identifier getAtlasLocation() {
         TextureAtlasSprite sprite = this.getSprite();
         return sprite != null ? sprite.atlasLocation() : TextureAtlas.LOCATION_BLOCKS;
      }

      private TextureAtlasSprite resolveSprite() {
         TextureManager tm = Minecraft.getInstance().getTextureManager();
         Identifier missingId = MissingTextureAtlasSprite.getLocation();
         TextureAtlasSprite firstMissing = null;

         for (Identifier atlasId : SpriteHolderRegistry.AtlasLookup.ORDER) {
            if (tm.getTexture(atlasId) instanceof TextureAtlas atlas) {
               TextureAtlasSprite sprite = atlas.getSprite(this.resourceLocation);
               if (!sprite.contents().name().equals(missingId)) {
                  return sprite;
               }

               if (firstMissing == null) {
                  firstMissing = sprite;
               }
            }
         }

         return firstMissing;
      }

      public void invalidate() {
         this.cachedSprite = null;
      }

      @Override
      public void bindTexture() {
      }

      @Override
      public double getInterpU(double u) {
         TextureAtlasSprite sprite = this.getSprite();
         return sprite == null ? (float)u : sprite.getU((float)u);
      }

      @Override
      public double getInterpV(double v) {
         TextureAtlasSprite sprite = this.getSprite();
         return sprite == null ? (float)v : sprite.getV((float)v);
      }
   }
}
