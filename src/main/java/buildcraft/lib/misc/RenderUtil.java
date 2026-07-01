/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

public final class RenderUtil {
   private RenderUtil() {
   }

   public static int swapARGBforABGR(int argb) {
      int a = argb >>> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb >> 0 & 0xFF;
      return a << 24 | b << 16 | g << 8 | r;
   }
}
