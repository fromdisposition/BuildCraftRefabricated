package buildcraft.lib.client.fluid;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class BcFluidGuiDrawer {
   private BcFluidGuiDrawer() {
   }

   public static void drawFluidStack(BCGraphics graphics, int x, int y, int width, int height, FluidStack stack) {
      if (stack != null && !stack.isEmpty()) {
         FluidClientCache.Appearance appearance = FluidClientCache.get(stack);
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
            graphics.blit(
               RenderPipelines.GUI_TEXTURED,
               TextureAtlas.LOCATION_BLOCKS,
               tileX,
               tileY,
               sprite.getU0() * atlasWidth,
               sprite.getV0() * atlasHeight,
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
