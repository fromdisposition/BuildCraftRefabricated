/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.core.render.ISprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class SpriteHolderRegistry {
   public void registerInitialSprites() {
   }

   public static SpriteHolderRegistry.SpriteHolder getHolder(String location) {
      return new SpriteHolderRegistry.SpriteHolder(location);
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
         Minecraft minecraft = Minecraft.getInstance();
         TextureAtlasSprite block = BcTextureAtlases.getBlockSprite(minecraft, this.resourceLocation);
         if (!BcTextureAtlases.isMissing(block)) {
            return block;
         }

         TextureAtlasSprite item = BcTextureAtlases.getItemSprite(minecraft, this.resourceLocation);
         if (!BcTextureAtlases.isMissing(item)) {
            return item;
         }

         return BcTextureAtlases.getGuiSprite(minecraft, this.resourceLocation);
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
