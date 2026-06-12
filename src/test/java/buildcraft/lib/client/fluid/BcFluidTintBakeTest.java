package buildcraft.lib.client.fluid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BcFluidTintBakeTest {
   private record FluidBakeSpec(String regName, int texLight, int texDark, int heat, boolean gaseous) {}

   /** Mirrors {@code fluidData} + heat suffixes in {@code build.gradle.kts}. */
   private static List<FluidBakeSpec> allFluidSpecs() {
      List<FluidBakeSpec> bases = List.of(
         new FluidBakeSpec("oil", 0x505050, 0x050505, 0, false),
         new FluidBakeSpec("oil_residue", 0x100F10, 0x421042, 0, false),
         new FluidBakeSpec("oil_heavy", 0xA07A9F, 0x423820, 0, false),
         new FluidBakeSpec("oil_dense", 0x876E77, 0x422424, 0, false),
         new FluidBakeSpec("oil_distilled", 0xE4AF78, 0xB47F00, 0, false),
         new FluidBakeSpec("fuel_dense", 0xFFAF3F, 0xE07F00, 0, false),
         new FluidBakeSpec("fuel_mixed_heavy", 0xF2A700, 0xC48700, 0, false),
         new FluidBakeSpec("fuel_light", 0xFFFF30, 0xE4CF00, 0, false),
         new FluidBakeSpec("fuel_mixed_light", 0xF6D700, 0xC4B700, 0, false),
         new FluidBakeSpec("fuel_gaseous", 0xFAF630, 0xE0D900, 0, true)
      );
      List<FluidBakeSpec> all = new ArrayList<>();
      for (FluidBakeSpec base : bases) {
         all.add(base);
         all.add(new FluidBakeSpec(base.regName + "_heat_1", base.texLight, base.texDark, 1, base.gaseous));
         all.add(new FluidBakeSpec(base.regName + "_heat_2", base.texLight, base.texDark, 2, base.gaseous));
      }
      return all;
   }

   static Stream<FluidBakeSpec> everyBakedFluid() {
      return allFluidSpecs().stream();
   }

   static Stream<FluidBakeSpec> everyLiquidFluid() {
      return allFluidSpecs().stream().filter(spec -> !spec.gaseous);
   }

   static Stream<FluidBakeSpec> everyGaseousFluid() {
      return allFluidSpecs().stream().filter(FluidBakeSpec::gaseous);
   }

   static Stream<Integer> everyHeatLevel() {
      return Stream.of(0, 1, 2);
   }

   private static int gradleRecolor(int basePixel, int light, int dark, boolean gaseous) {
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
      int outA = gaseous ? Math.clamp((int)(alpha * 0.42), 24, 255) : alpha;
      return outA << 24 | outR << 16 | outG << 8 | outB;
   }

   private static int adjLight(int light, int dark, int heat) {
      int tintR = ((light >> 16 & 0xFF) + (dark >> 16 & 0xFF)) / 2 + heat * 0x10;
      int tintG = ((light >> 8 & 0xFF) + (dark >> 8 & 0xFF)) / 2 + heat * 0x10;
      int tintB = ((light & 0xFF) + (dark & 0xFF)) / 2 + heat * 0x10;
      return 0xFF000000 | Math.min(tintR, 0xFF) << 16 | Math.min(tintG, 0xFF) << 8 | Math.min(tintB, 0xFF);
   }

   @ParameterizedTest(name = "{0}")
   @MethodSource("everyBakedFluid")
   void bakedStillMatchesGradleAdjLightBake(FluidBakeSpec spec) throws IOException {
      String stillTemplatePath = "/assets/buildcraftenergy/textures/block/fluids/heat_" + spec.heat + "_still.png";
      String bakedStillPath = "/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + ".png";
      try (InputStream templateIn = getClass().getResourceAsStream(stillTemplatePath);
           InputStream bakedIn = getClass().getResourceAsStream(bakedStillPath)) {
         BufferedImage template = ImageIO.read(templateIn);
         BufferedImage baked = ImageIO.read(bakedIn);
         int src = template.getRGB(8, 8);
         int fromAdj = gradleRecolor(src, adjLight(spec.texLight, spec.texDark, spec.heat), spec.texDark, spec.gaseous);
         assertEquals(fromAdj, baked.getRGB(8, 8), spec.regName);
      }
   }

   @ParameterizedTest(name = "{0}")
   @MethodSource("everyLiquidFluid")
   void bakedFlowIsFullyOpaque(FluidBakeSpec spec) throws IOException {
      String bakedFlowPath = "/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + "_flow.png";
      try (InputStream bakedIn = getClass().getResourceAsStream(bakedFlowPath)) {
         BufferedImage baked = ImageIO.read(bakedIn);
         int opaque = 0;
         int translucent = 0;
         for (int y = 0; y < baked.getHeight(); y++) {
            for (int x = 0; x < baked.getWidth(); x++) {
               int alpha = baked.getRGB(x, y) >>> 24 & 0xFF;
               if (alpha == 0) {
                  continue;
               }
               if (alpha == 0xFF) {
                  opaque++;
               } else {
                  translucent++;
               }
            }
         }
         assertEquals(0, translucent, spec.regName + " flow must not inherit water alpha");
         assertTrue(opaque > 0, spec.regName);
      }
   }

   @ParameterizedTest(name = "{0}")
   @MethodSource("everyGaseousFluid")
   void bakedGaseousFlowIsTranslucent(FluidBakeSpec spec) throws IOException {
      String bakedFlowPath = "/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + "_flow.png";
      try (InputStream bakedIn = getClass().getResourceAsStream(bakedFlowPath)) {
         BufferedImage baked = ImageIO.read(bakedIn);
         int translucent = 0;
         for (int y = 0; y < baked.getHeight(); y++) {
            for (int x = 0; x < baked.getWidth(); x++) {
               int alpha = baked.getRGB(x, y) >>> 24 & 0xFF;
               if (alpha > 0 && alpha < 0xFF) {
                  translucent++;
               }
            }
         }
         assertTrue(translucent > 0, spec.regName + " gaseous flow must stay translucent");
      }
   }

   @ParameterizedTest(name = "{0}")
   @MethodSource("everyBakedFluid")
   void bakedFlowAverageRgbMatchesStill(FluidBakeSpec spec) throws IOException {
      String stillPath = "/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + ".png";
      String flowPath = "/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + "_flow.png";
      try (InputStream stillIn = getClass().getResourceAsStream(stillPath);
           InputStream flowIn = getClass().getResourceAsStream(flowPath)) {
         double[] stillAvg = averageOpaqueRgb(ImageIO.read(stillIn));
         double[] flowAvg = averageOpaqueRgb(ImageIO.read(flowIn));
         for (int channel = 0; channel < 3; channel++) {
            assertEquals(stillAvg[channel], flowAvg[channel], 1.0, spec.regName + " RGB channel " + channel);
         }
      }
   }

   @ParameterizedTest(name = "{0}")
   @MethodSource("everyBakedFluid")
   void bakedFlowContrastMatchesStill(FluidBakeSpec spec) throws IOException {
      String stillPath = "/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + ".png";
      String flowPath = "/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + "_flow.png";
      try (InputStream stillIn = getClass().getResourceAsStream(stillPath);
           InputStream flowIn = getClass().getResourceAsStream(flowPath)) {
         BufferedImage still = ImageIO.read(stillIn);
         BufferedImage flow = ImageIO.read(flowIn);
         int stillSpread = rgbSpreadInFrame(still, 16, 0);
         int flowSpread = rgbSpreadInFrame(flow, 32, 0);
         assertTrue(flowSpread >= stillSpread / 2, spec.regName + " flow tonal range too flat");
      }
   }

   @ParameterizedTest
   @MethodSource("everyHeatLevel")
   void bakedFlowLuminanceMapsConsistentlyAcrossQuadrants(int heat) throws IOException {
      String templatePath = "/assets/buildcraftenergy/textures/block/fluids/heat_" + heat + "_flow.png";
      String bakedPath = "/assets/buildcraftenergy/textures/block/fluids/baked/oil" + heatSuffix(heat) + "_flow.png";
      try (InputStream templateIn = getClass().getResourceAsStream(templatePath);
           InputStream bakedIn = getClass().getResourceAsStream(bakedPath)) {
         BufferedImage template = ImageIO.read(templateIn);
         BufferedImage baked = ImageIO.read(bakedIn);
         HashMap<Integer, Integer> lumToColor = new HashMap<>();
         int[][] quadrants = new int[][] { {0, 0}, {16, 0}, {0, 16}, {16, 16} };
         for (int[] quadrant : quadrants) {
            int qx = quadrant[0];
            int qy = quadrant[1];
            for (int y = 0; y < 16; y++) {
               for (int x = 0; x < 16; x++) {
                  int templateArgb = template.getRGB(qx + x, qy + y);
                  int bakedArgb = baked.getRGB(qx + x, qy + y);
                  if ((templateArgb >>> 24 & 0xFF) == 0 || (bakedArgb >>> 24 & 0xFF) == 0) {
                     continue;
                  }
                  int lum = templateArgb >> 16 & 0xFF;
                  int bakedRgb = bakedArgb & 0xFFFFFF;
                  Integer prior = lumToColor.putIfAbsent(lum, bakedRgb);
                  if (prior != null) {
                     assertEquals(prior.intValue(), bakedRgb, "heat " + heat + " luminance " + lum);
                  }
               }
            }
         }
      }
   }

   @ParameterizedTest
   @MethodSource("everyHeatLevel")
   void bakedFlowPreservesVanillaWaterScroll(int heat) throws IOException {
      String templatePath = "/assets/buildcraftenergy/textures/block/fluids/heat_" + heat + "_flow.png";
      String bakedPath = "/assets/buildcraftenergy/textures/block/fluids/baked/fuel_light" + heatSuffix(heat) + "_flow.png";
      try (InputStream templateIn = getClass().getResourceAsStream(templatePath);
           InputStream bakedIn = getClass().getResourceAsStream(bakedPath)) {
         BufferedImage template = ImageIO.read(templateIn);
         BufferedImage baked = ImageIO.read(bakedIn);
         int templateShift = bestVerticalFrameShift(template, 32, 0);
         int bakedShift = bestVerticalFrameShift(baked, 32, 0);
         assertEquals(templateShift, bakedShift, "heat " + heat);
         assertEquals(1, bakedShift, "heat " + heat);
      }
   }

   @Test
   void allThirtyBakedFluidsAreGenerated() {
      assertEquals(30, allFluidSpecs().size());
      for (FluidBakeSpec spec : allFluidSpecs()) {
         assertTrue(
            getClass().getResource("/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + ".png") != null,
            "missing still " + spec.regName
         );
         assertTrue(
            getClass().getResource("/assets/buildcraftenergy/textures/block/fluids/baked/" + spec.regName + "_flow.png") != null,
            "missing flow " + spec.regName
         );
      }
   }

   @ParameterizedTest
   @MethodSource("everyHeatLevel")
   void bakedFlowMcmetaMatchesVanillaWaterFrametime(int heat) throws IOException {
      String mcmetaPath = "/assets/buildcraftenergy/textures/block/fluids/baked/oil_dense" + heatSuffix(heat) + "_flow.png.mcmeta";
      try (InputStream in = getClass().getResourceAsStream(mcmetaPath)) {
         String mcmeta = new String(in.readAllBytes(), StandardCharsets.UTF_8);
         assertTrue(mcmeta.contains("\"frametime\": 1"), "heat " + heat);
         assertTrue(mcmeta.contains("\"width\": 32"));
         assertTrue(mcmeta.contains("\"height\": 32"));
         assertTrue(!mcmeta.contains("interpolate"), "heat " + heat);
      }
   }

   @Test
   void gradleBakeDiffersFromLegacyRuntimeBakeColors() throws IOException {
      int light = 0x505050;
      int dark = 0x050505;
      try (InputStream templateIn = getClass().getResourceAsStream("/assets/buildcraftenergy/textures/block/fluids/heat_0_still.png")) {
         int src = ImageIO.read(templateIn).getRGB(8, 8);
         int legacy = BcFluidTintUtil.bakeAtlasArgb(src, light, dark);
         int gradle = gradleRecolor(src, adjLight(light, dark, 0), dark, false);
         assertNotEquals(legacy, gradle, "gradle adjLight bake must differ from legacy raw texLight bake");
      }
   }

   private static String heatSuffix(int heat) {
      return switch (heat) {
         case 0 -> "";
         case 1 -> "_heat_1";
         default -> "_heat_2";
      };
   }

   private static int bestVerticalFrameShift(BufferedImage image, int frameSize, int frameIndex) {
      double bestError = Double.MAX_VALUE;
      int bestShift = 0;
      for (int shift = -4; shift <= 4; shift++) {
         double error = 0.0D;
         int samples = 0;
         for (int y = 0; y < frameSize; y++) {
            int y2 = y + shift;
            if (y2 < 0 || y2 >= frameSize) {
               continue;
            }
            for (int x = 0; x < frameSize; x++) {
               int from = image.getRGB(x, frameIndex * frameSize + y);
               int to = image.getRGB(x, (frameIndex + 1) * frameSize + y2);
               if ((from >>> 24 & 0xFF) == 0 || (to >>> 24 & 0xFF) == 0) {
                  continue;
               }
               int fromGray = from >> 16 & 0xFF;
               int toGray = to >> 16 & 0xFF;
               error += Math.abs(fromGray - toGray);
               samples++;
            }
         }
         if (samples > 0) {
            error /= samples;
            if (error < bestError) {
               bestError = error;
               bestShift = shift;
            }
         }
      }
      return bestShift;
   }

   private static int rgbSpreadInFrame(BufferedImage image, int frameSize, int frameIndex) {
      int min = 255;
      int max = 0;
      int y0 = frameIndex * frameSize;
      for (int y = y0; y < y0 + frameSize; y++) {
         for (int x = 0; x < frameSize; x++) {
            int argb = image.getRGB(x, y);
            if ((argb >>> 24 & 0xFF) == 0) {
               continue;
            }
            int gray = argb >> 16 & 0xFF;
            min = Math.min(min, gray);
            max = Math.max(max, gray);
         }
      }
      return max - min;
   }

   private static double[] averageOpaqueRgb(BufferedImage image) {
      long r = 0;
      long g = 0;
      long b = 0;
      int count = 0;
      for (int y = 0; y < image.getHeight(); y++) {
         for (int x = 0; x < image.getWidth(); x++) {
            int argb = image.getRGB(x, y);
            int alpha = argb >>> 24 & 0xFF;
            if (alpha == 0) {
               continue;
            }
            r += argb >> 16 & 0xFF;
            g += argb >> 8 & 0xFF;
            b += argb & 0xFF;
            count++;
         }
      }
      return new double[] { (double) r / count, (double) g / count, (double) b / count };
   }
}
