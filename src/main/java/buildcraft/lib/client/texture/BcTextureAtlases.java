/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

public final class BcTextureAtlases {
   public static final Identifier BLOCKS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
   public static final Identifier ITEMS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/items.png");
   public static final Identifier BLOCKS = AtlasIds.BLOCKS;
   public static final Identifier ITEMS = AtlasIds.ITEMS;

   private BcTextureAtlases() {
   }

   public static TextureAtlasSprite getBlockSprite(Identifier texture) {
      return getBlockSprite(Minecraft.getInstance(), texture);
   }

   public static TextureAtlasSprite getBlockSprite(Minecraft minecraft, Identifier texture) {
      return resolveFromAtlas(blocksAtlas(minecraft), texture, "block/", "entity/", "block/");
   }

   public static TextureAtlasSprite getItemSprite(Minecraft minecraft, Identifier texture) {
      return resolveFromAtlas(itemsAtlas(minecraft), texture, "item/", "item/");
   }

   public static TextureAtlasSprite getGuiSprite(Minecraft minecraft, Identifier texture) {
      TextureAtlasSprite sprite = guiAtlas(minecraft).getSprite(texture);
      return isMissing(sprite) ? guiAtlas(minecraft).getSprite(MissingTextureAtlasSprite.getLocation()) : sprite;
   }

   public static SpriteId blockSpriteId(Identifier texture) {
      return new SpriteId(BLOCKS_TEXTURE, blockSpriteIdCandidate(texture));
   }

   public static boolean isMissing(TextureAtlasSprite sprite) {
      return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation());
   }

   private static Identifier blockSpriteIdCandidate(Identifier texture) {
      TextureAtlas atlas = blocksAtlas(Minecraft.getInstance());
      if (hasPathPrefix(texture, "block/", "entity/")) {
         return texture;
      }

      if (!isMissing(atlas.getSprite(texture))) {
         return texture;
      }

      return texture.withPrefix("block/");
   }

   private static TextureAtlasSprite resolveFromAtlas(TextureAtlas atlas, Identifier texture, String fallbackPrefix, String... existingPrefixes) {
      if (hasPathPrefix(texture, existingPrefixes)) {
         return atlas.getSprite(texture);
      }

      TextureAtlasSprite direct = atlas.getSprite(texture);
      if (!isMissing(direct)) {
         return direct;
      }

      return atlas.getSprite(texture.withPrefix(fallbackPrefix));
   }

   private static boolean hasPathPrefix(Identifier texture, String... prefixes) {
      String path = texture.getPath();

      for (String prefix : prefixes) {
         if (path.startsWith(prefix)) {
            return true;
         }
      }

      return false;
   }

   private static TextureAtlas blocksAtlas(Minecraft minecraft) {
      return minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
   }

   private static TextureAtlas itemsAtlas(Minecraft minecraft) {
      return minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS);
   }

   private static TextureAtlas guiAtlas(Minecraft minecraft) {
      return minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
   }
}
