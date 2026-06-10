/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public final class BcFluidTintUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int RENDER_TINT_WHITE = -1;
   private static final int[] TEMPLATE_AVG_GRAY = new int[]{43, 43, 43};
   private static final int[][][] HEAT_STILL_LUMINANCE = new int[3][][];
   private static final int[][][] HEAT_STILL_ALPHA = new int[3][][];
   private static final int[][][] HEAT_FLOW_LUMINANCE = new int[3][][];
   private static final int[][][] HEAT_FLOW_ALPHA = new int[3][][];
   private static boolean clientTemplatesLoaded;

   private BcFluidTintUtil() {
   }

   public static Identifier heatStillSpriteId(int heat) {
      int h = Math.clamp(heat, 0, 2);
      return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/heat_" + h + "_still");
   }

   public static Identifier heatFlowSpriteId(int heat) {
      int h = Math.clamp(heat, 0, 2);
      return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/heat_" + h + "_flow");
   }

   public static Identifier heatStillWhiteSpriteId(int heat) {
      return heatStillSpriteId(heat).withSuffix("_white");
   }

   public static Identifier worldStillSpriteId(String fluidRegName) {
      return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/" + fluidRegName + "_still");
   }

   public static Identifier worldFlowSpriteId(String fluidRegName) {
      return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/" + fluidRegName + "_flowing");
   }

   public static Identifier bakedStillSpriteId(String fluidRegName) {
      return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/baked/" + fluidRegName);
   }

   public static Identifier bakedFlowSpriteId(String fluidRegName) {
      return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/baked/" + fluidRegName + "_flow");
   }

   public static int bakeAtlasArgb(int srcArgb, int texLight, int texDark) {
      int a = srcArgb >> 24 & 0xFF;
      if (a == 0) {
         return 0;
      }

      int r = recolorChannel(texDark >> 16 & 0xFF, texLight >> 16 & 0xFF, srcArgb >> 16 & 0xFF);
      int g = recolorChannel(texDark >> 8 & 0xFF, texLight >> 8 & 0xFF, srcArgb >> 8 & 0xFF);
      int b = recolorChannel(texDark & 0xFF, texLight & 0xFF, srcArgb & 0xFF);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public static void bakeHeatImage(NativeImage src, NativeImage dst, int texLight, int texDark) {
      int w = src.getWidth();
      int h = src.getHeight();

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            dst.setPixel(x, y, bakeAtlasArgb(src.getPixel(x, y), texLight, texDark));
         }
      }

      makeTileable(dst);
   }

   public static void makeTileable(NativeImage img) {
      int w = img.getWidth();
      int frameH = w;
      int frames = img.getHeight() / frameH;

      for (int f = 0; f < frames; f++) {
         int y0 = f * frameH;

         for (int pass = 0; pass < 3; pass++) {
            for (int x = 0; x < w; x++) {
               int blended = blendArgb(img.getPixel(x, y0), img.getPixel(x, y0 + frameH - 1));
               img.setPixel(x, y0, blended);
               img.setPixel(x, y0 + frameH - 1, blended);
            }

            for (int y = y0; y < y0 + frameH; y++) {
               int blended = blendArgb(img.getPixel(0, y), img.getPixel(w - 1, y));
               img.setPixel(0, y, blended);
               img.setPixel(w - 1, y, blended);
            }

            for (int dx = 0; dx < 2; dx++) {
               for (int dy = 0; dy < 2; dy++) {
                  int tl = img.getPixel(dx, y0 + dy);
                  int tr = img.getPixel(w - 1 - dx, y0 + dy);
                  int bl = img.getPixel(dx, y0 + frameH - 1 - dy);
                  int br = img.getPixel(w - 1 - dx, y0 + frameH - 1 - dy);
                  int avg = blendArgb(blendArgb(tl, tr), blendArgb(bl, br));

                  img.setPixel(dx, y0 + dy, avg);
                  img.setPixel(w - 1 - dx, y0 + dy, avg);
                  img.setPixel(dx, y0 + frameH - 1 - dy, avg);
                  img.setPixel(w - 1 - dx, y0 + frameH - 1 - dy, avg);
               }
            }
         }
      }
   }

   private static int blendArgb(int a, int b) {
      int outA = ((a >> 24 & 0xFF) + (b >> 24 & 0xFF)) / 2;
      int outR = ((a >> 16 & 0xFF) + (b >> 16 & 0xFF)) / 2;
      int outG = ((a >> 8 & 0xFF) + (b >> 8 & 0xFF)) / 2;
      int outB = ((a & 0xFF) + (b & 0xFF)) / 2;
      return outA << 24 | outR << 16 | outG << 8 | outB;
   }

   public static int computeAverageGuiTint(int texLight, int texDark, int heat) {
      int h = Math.clamp(heat, 0, 2);
      int avgGray = Math.max(1, TEMPLATE_AVG_GRAY[h]);
      int r = recolorChannel(texDark >> 16 & 0xFF, texLight >> 16 & 0xFF, avgGray);
      int g = recolorChannel(texDark >> 8 & 0xFF, texLight >> 8 & 0xFF, avgGray);
      int b = recolorChannel(texDark & 0xFF, texLight & 0xFF, avgGray);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public static int recolorChannel(int dark, int light, int v) {
      return (dark * (256 - v) + light * v) / 256;
   }

   public static int recolorRgb(int texLight, int texDark, int gray) {
      int r = recolorChannel(texDark >> 16 & 0xFF, texLight >> 16 & 0xFF, gray);
      int g = recolorChannel(texDark >> 8 & 0xFF, texLight >> 8 & 0xFF, gray);
      int b = recolorChannel(texDark & 0xFF, texLight & 0xFF, gray);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public static int vertexColorFromTemplate(int texLight, int texDark, int heat, float u, float v) {
      return vertexColorFromTemplate(texLight, texDark, heat, u, v, false);
   }

   public static int vertexColorFromTemplate(int texLight, int texDark, int heat, float u, float v, boolean flowing) {
      ensureClientTemplatesLoaded();
      int h = Math.clamp(heat, 0, 2);
      int lum = sampleLuminance(h, u, v, flowing);
      int alpha = sampleAlpha(h, u, v, flowing);
      if (lum > 0 && alpha > 0) {
         int r = recolorChannel(texDark >> 16 & 0xFF, texLight >> 16 & 0xFF, lum);
         int g = recolorChannel(texDark >> 8 & 0xFF, texLight >> 8 & 0xFF, lum);
         int b = recolorChannel(texDark & 0xFF, texLight & 0xFF, lum);
         return alpha << 24 | r << 16 | g << 8 | b;
      } else {
         return 0;
      }
   }

   public static float normalizedU(float atlasU, float u0, float u1) {
      return u1 <= u0 ? 0.0F : (atlasU - u0) / (u1 - u0);
   }

   public static float normalizedV(float atlasV, float v0, float v1) {
      return v1 <= v0 ? 0.0F : (atlasV - v0) / (v1 - v0);
   }

   public static void reloadTemplates(ResourceManager manager) {
      for (int heat = 0; heat < 3; heat++) {
         loadTemplateFromManager(manager, heatStillSpriteId(heat), HEAT_STILL_LUMINANCE, HEAT_STILL_ALPHA, heat, true);
         loadTemplateFromManager(manager, heatFlowSpriteId(heat), HEAT_FLOW_LUMINANCE, HEAT_FLOW_ALPHA, heat, false);
      }

      clientTemplatesLoaded = true;
   }

   public static void ensureClientTemplatesLoaded() {
      if (!clientTemplatesLoaded) {
         for (int heat = 0; heat < 3; heat++) {
            loadTemplateFromClasspath(heatStillSpriteId(heat), HEAT_STILL_LUMINANCE, HEAT_STILL_ALPHA, heat, true);
            loadTemplateFromClasspath(heatFlowSpriteId(heat), HEAT_FLOW_LUMINANCE, HEAT_FLOW_ALPHA, heat, false);
         }

         clientTemplatesLoaded = true;
      }
   }

   private static void loadTemplateFromManager(
      ResourceManager manager, Identifier id, int[][][] luminanceOut, int[][][] alphaOut, int heat, boolean updateAvgGray
   ) {
      Identifier textureId = Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png");

      try {
         Resource resource = (Resource)manager.getResource(textureId).orElse(null);
         if (resource == null) {
            LOGGER.warn("Missing heat fluid template {}", textureId);
            luminanceOut[heat] = null;
            alphaOut[heat] = null;
            return;
         }

         try (InputStream in = resource.open()) {
            decodeTemplate(in, luminanceOut, alphaOut, heat, updateAvgGray);
         }
      } catch (IOException e) {
         LOGGER.warn("Failed to load heat fluid template {}", textureId, e);
         luminanceOut[heat] = null;
         alphaOut[heat] = null;
      }
   }

   private static void loadTemplateFromClasspath(Identifier id, int[][][] luminanceOut, int[][][] alphaOut, int heat, boolean updateAvgGray) {
      String path = "/assets/" + id.getNamespace() + "/textures/" + id.getPath() + ".png";

      try (InputStream in = BcFluidTintUtil.class.getResourceAsStream(path)) {
         if (in != null) {
            decodeTemplate(in, luminanceOut, alphaOut, heat, updateAvgGray);
         } else {
            LOGGER.warn("Missing heat fluid template {}", path);
            luminanceOut[heat] = null;
            alphaOut[heat] = null;
         }
      } catch (IOException e) {
         LOGGER.warn("Failed to load heat fluid template for heat {}", heat, e);
         luminanceOut[heat] = null;
         alphaOut[heat] = null;
      }
   }

   private static void decodeTemplate(InputStream in, int[][][] luminanceOut, int[][][] alphaOut, int heat, boolean updateAvgGray) throws IOException {
      BufferedImage img = ImageIO.read(in);
      int w = img.getWidth();
      int frameH = w;
      int frames = Math.max(1, img.getHeight() / frameH);
      luminanceOut[heat] = new int[frames][w * frameH];
      alphaOut[heat] = new int[frames][w * frameH];
      long sum = 0L;
      long count = 0L;

      for (int f = 0; f < frames; f++) {
         int[] lumFrame = luminanceOut[heat][f];
         int[] alphaFrame = alphaOut[heat][f];
         int i = 0;

         for (int y = f * frameH; y < (f + 1) * frameH; y++) {
            for (int x = 0; x < w; x++) {
               int argb = img.getRGB(x, y);
               int a = argb >> 24 & 0xFF;
               int r = argb >> 16 & 0xFF;
               int g = argb >> 8 & 0xFF;
               int b = argb & 0xFF;
               int lum = a == 0 ? 0 : (r + g + b) / 3;
               lumFrame[i] = lum;
               alphaFrame[i] = a;
               if (updateAvgGray && f == 0 && lum > 0) {
                  sum += lum;
                  count++;
               }

               i++;
            }
         }
      }

      if (updateAvgGray && count > 0L) {
         TEMPLATE_AVG_GRAY[heat] = (int)(sum / count);
      }
   }

   private static int sampleLuminance(int heat, float u, float v, boolean flowing) {
      return sampleChannel(flowing ? HEAT_FLOW_LUMINANCE : HEAT_STILL_LUMINANCE, heat, u, v, TEMPLATE_AVG_GRAY[Math.clamp(heat, 0, 2)]);
   }

   private static int sampleAlpha(int heat, float u, float v, boolean flowing) {
      return sampleChannel(flowing ? HEAT_FLOW_ALPHA : HEAT_STILL_ALPHA, heat, u, v, 255);
   }

   private static int sampleChannel(int[][][] framesByHeat, int heat, float u, float v, int fallback) {
      int[][] frames = framesByHeat[heat];
      if (frames != null && frames.length != 0) {
         int frame = 0;
         int[] pixels = frames[frame];
         int size = (int)Math.sqrt(pixels.length);
         if (size <= 0) {
            return fallback;
         }

         float fu = u - (float)Math.floor(u);
         float fv = v - (float)Math.floor(v);
         int x = Math.clamp((int)(fu * size), 0, size - 1);
         int y = Math.clamp((int)(fv * size), 0, size - 1);
         return pixels[y * size + x];
      } else {
         return fallback;
      }
   }
}
