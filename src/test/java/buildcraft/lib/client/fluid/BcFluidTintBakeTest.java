package buildcraft.lib.client.fluid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class BcFluidTintBakeTest {
   private static int gradleRecolor(int basePixel, int light, int dark) {
      int alpha = basePixel >>> 24 & 0xFF;
      if (alpha == 0) {
         return 0;
      }
      int wr = basePixel >> 16 & 0xFF;
      int lr = light >> 16 & 0xFF;
      int dr = dark >> 16 & 0xFF;
      int outR = (dr * (256 - wr) + lr * wr) / 256;
      int outG = ((dark >> 8 & 0xFF) * (256 - (basePixel >> 8 & 0xFF)) + (light >> 8 & 0xFF) * (basePixel >> 8 & 0xFF)) / 256;
      int outB = ((dark & 0xFF) * (256 - (basePixel & 0xFF)) + (light & 0xFF) * (basePixel & 0xFF)) / 256;
      return alpha << 24 | outR << 16 | outG << 8 | outB;
   }

   private static int adjLight(int light, int dark, int heat) {
      int tintR = ((light >> 16 & 0xFF) + (dark >> 16 & 0xFF)) / 2 + heat * 0x10;
      int tintG = ((light >> 8 & 0xFF) + (dark >> 8 & 0xFF)) / 2 + heat * 0x10;
      int tintB = ((light & 0xFF) + (dark & 0xFF)) / 2 + heat * 0x10;
      return 0xFF000000 | Math.min(tintR, 0xFF) << 16 | Math.min(tintG, 0xFF) << 8 | Math.min(tintB, 0xFF);
   }

   @Test
   void bakedOilMatchesGradleAdjLightBake() throws IOException {
      int light = 0x505050;
      int dark = 0x050505;
      try (InputStream templateIn = getClass().getResourceAsStream("/assets/buildcraftenergy/textures/block/fluids/heat_0_still.png");
           InputStream bakedIn = getClass().getResourceAsStream("/assets/buildcraftenergy/textures/block/fluids/baked/oil.png")) {
         BufferedImage template = ImageIO.read(templateIn);
         BufferedImage baked = ImageIO.read(bakedIn);
         int src = template.getRGB(8, 8);
         int fromAdj = gradleRecolor(src, adjLight(light, dark, 0), dark);
         assertEquals(fromAdj, baked.getRGB(8, 8));
      }
   }

   @Test
   void gradleBakeDiffersFromLegacyRuntimeBakeColors() throws IOException {
      int light = 0x505050;
      int dark = 0x050505;
      try (InputStream templateIn = getClass().getResourceAsStream("/assets/buildcraftenergy/textures/block/fluids/heat_0_still.png")) {
         int src = ImageIO.read(templateIn).getRGB(8, 8);
         int legacy = BcFluidTintUtil.bakeAtlasArgb(src, light, dark);
         int gradle = gradleRecolor(src, adjLight(light, dark, 0), dark);
         assertNotEquals(legacy, gradle, "gradle adjLight bake must differ from legacy raw texLight bake");
      }
   }
}
