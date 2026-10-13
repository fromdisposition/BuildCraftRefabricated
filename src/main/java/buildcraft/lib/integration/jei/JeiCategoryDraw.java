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

public final class JeiCategoryDraw {
   public static final int CARD_PAD = 12;
   private static final int TEXT_COLOR = -12566464;

   private JeiCategoryDraw() {
   }

   public static int cardH(int bgH) {
      return bgH + CARD_PAD;
   }

   public static void mjPower(GuiGraphicsExtractor graphics, String langKey, long microJoules, int alignWidth, int bgH) {
      String mj = LocaleUtil.localizeMj(microJoules);
      if (!mj.isEmpty()) {
         textRight(graphics, LocaleUtil.localize(langKey, mj), alignWidth, bgH + 2);
      }
   }

   public static void text(GuiGraphicsExtractor graphics, String text, int x, int y) {
      Font font = Minecraft.getInstance().font;
      new BCGraphics(graphics).text(font, text, x, y, TEXT_COLOR, false);
   }

   public static void textRight(GuiGraphicsExtractor graphics, String text, int alignWidth, int y) {
      Font font = Minecraft.getInstance().font;
      int x = Math.max(2, alignWidth - font.width(text) - 2);
      new BCGraphics(graphics).text(font, text, x, y, TEXT_COLOR, false);
   }
}
