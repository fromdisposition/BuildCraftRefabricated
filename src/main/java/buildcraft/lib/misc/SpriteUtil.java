/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
