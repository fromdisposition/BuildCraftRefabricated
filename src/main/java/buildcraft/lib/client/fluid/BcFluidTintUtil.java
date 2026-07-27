/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

import net.minecraft.resources.Identifier;

public final class BcFluidTintUtil {
   public static final int RENDER_TINT_WHITE = -1;
   private static final int[] TEMPLATE_AVG_GRAY = new int[]{43, 43, 43};

   private BcFluidTintUtil() {
   }

   public static Identifier bakedStillSpriteId(String fluidRegName) {
      return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/baked/" + fluidRegName);
   }

   public static Identifier bakedFlowSpriteId(String fluidRegName) {
      return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/baked/" + fluidRegName + "_flow");
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
}
