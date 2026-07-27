/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class FluidGuiRenderer {
   private FluidGuiRenderer() {
   }

   public static void drawFluidStack(BCGraphics graphics, int x, int y, int width, int height, FluidStack stack) {
      if (stack != null && !stack.isEmpty()) {
         BcFluidAppearance appearance = BcFluidAppearanceCache.get(stack);
         if (appearance != null && appearance.sprite() != null) {
            drawTiled(graphics, x, y, width, height, appearance.sprite(), appearance.tint());
         }
      }
   }

   public static void drawTiled(BCGraphics graphics, int x, int y, int width, int height, TextureAtlasSprite sprite, int tintColor) {
      int spriteSize = 16;
      float uMin = sprite.getU0();
      float vMin = sprite.getV0();
      float uMax = sprite.getU1();
      float vMax = sprite.getV1();
      int atlasWidth = (int)(spriteSize / (uMax - uMin));
      int atlasHeight = (int)(spriteSize / (vMax - vMin));
      graphics.enableScissor(x, y, x + width, y + height);

      for (int tileY = y; tileY < y + height; tileY += spriteSize) {
         for (int tileX = x; tileX < x + width; tileX += spriteSize) {
            int drawW = Math.min(spriteSize, x + width - tileX);
            int drawH = Math.min(spriteSize, y + height - tileY);
            // Tinted sub-region blit of the block atlas: sample a drawW x drawH region (unscaled) at the
            // sprite's pixel offset, coloured by the fluid tint. The 12-arg overload carries the colour;
            // the previous 10-arg call bound the tint to the textureSize param, producing garbage UVs.
            graphics.blit(
               BcTextureAtlases.BLOCKS_TEXTURE,
               tileX,
               tileY,
               sprite.getU0() * atlasWidth,
               sprite.getV0() * atlasHeight,
               drawW,
               drawH,
               drawW,
               drawH,
               atlasWidth,
               atlasHeight,
               tintColor
            );
         }
      }

      graphics.disableScissor();
   }
}
