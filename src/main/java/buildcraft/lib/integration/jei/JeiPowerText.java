/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.integration.jei;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class JeiPowerText {
   private static final int COLOR = -12566464;

   private JeiPowerText() {
   }

   public static void drawRightAligned(GuiGraphicsExtractor graphics, String langKey, long microJoules, int panelWidth, int y) {
      String mjStr = LocaleUtil.localizeMj(microJoules);
      if (mjStr.isEmpty()) {
         return;
      }

      String text = LocaleUtil.localize(langKey, mjStr);
      Font font = Minecraft.getInstance().font;
      int x = Math.max(2, panelWidth - font.width(text) - 2);
      new BCGraphics(graphics).text(font, text, x, y, COLOR, false);
   }
}
