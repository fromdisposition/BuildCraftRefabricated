package buildcraft.lib.client.sprite;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.core.render.ISprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

public class SpriteHolderRegistry {
   public void registerInitialSprites() {
   }

   public static SpriteHolderRegistry.SpriteHolder getHolder(String location) {
      return new SpriteHolderRegistry.SpriteHolder(location);
   }

   private static final class AtlasLookup {
      static SpriteId[] candidates(Identifier texture) {
         return new SpriteId[]{
            Sheets.BLOCKS_MAPPER.apply(texture), Sheets.ITEMS_MAPPER.apply(texture), new SpriteId(Sheets.GUI_SHEET, texture)
         };
      }
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
         return sprite != null ? sprite.atlasLocation() : BcTextureAtlases.BLOCKS_TEXTURE;
      }

      private TextureAtlasSprite resolveSprite() {
         SpriteGetter sprites = BcTextureAtlases.blocksSpriteGetter(Minecraft.getInstance());
         Identifier missingId = MissingTextureAtlasSprite.getLocation();
         TextureAtlasSprite firstMissing = null;

         for (SpriteId spriteId : SpriteHolderRegistry.AtlasLookup.candidates(this.resourceLocation)) {
            TextureAtlasSprite sprite = sprites.get(spriteId);
            if (!sprite.contents().name().equals(missingId)) {
               return sprite;
            }

            if (firstMissing == null) {
               firstMissing = sprite;
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
