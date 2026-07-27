/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client;

import buildcraft.lib.gui.BCGraphics;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DebugOverlayRenderer {
   public static void render(GuiGraphicsExtractor vanillaGraphics, DeltaTracker deltaTracker) {
      BCGraphics graphics = new BCGraphics(vanillaGraphics);
      Minecraft mc = Minecraft.getInstance();
      //? if >= 1.21.10 {
      if (mc.debugEntries != null && mc.debugEntries.isOverlayVisible()) {
      //?} else {
      /*if (mc.gui.getDebugOverlay().showDebugScreen()) {
      *///?}
         List<String> leftLines = DebugOverlayHelper.getLeftLines();
         List<String> rightLines = DebugOverlayHelper.getRightLines();
         if (!leftLines.isEmpty() || !rightLines.isEmpty()) {
            Font font = mc.font;
            int lineHeight = 9 + 2;
            int leftHeight = leftLines.size() * lineHeight;
            int leftY = mc.getWindow().getGuiScaledHeight() / 2;
            if (leftY + leftHeight > mc.getWindow().getGuiScaledHeight()) {
               leftY = Math.max(5, mc.getWindow().getGuiScaledHeight() - leftHeight - 5);
            }

            for (String line : leftLines) {
               if (line.isEmpty()) {
                  leftY += lineHeight;
               } else {
                  int width = font.width(line);
                  graphics.fill(1, leftY - 1, 2 + width + 1, leftY + 9, -1873784752);
                  graphics.text(font, line, 2, leftY, -2039584, false);
                  leftY += lineHeight;
               }
            }

            int rightHeight = rightLines.size() * lineHeight;
            int rightY = mc.getWindow().getGuiScaledHeight() / 2;
            if (rightY + rightHeight > mc.getWindow().getGuiScaledHeight()) {
               rightY = Math.max(5, mc.getWindow().getGuiScaledHeight() - rightHeight - 5);
            }

            int screenWidth = mc.getWindow().getGuiScaledWidth();

            for (String line : rightLines) {
               if (line.isEmpty()) {
                  rightY += lineHeight;
               } else {
                  int width = font.width(line);
                  int x = screenWidth - 2 - width;
                  graphics.fill(x - 1, rightY - 1, x + width + 1, rightY + 9, -1873784752);
                  graphics.text(font, line, x, rightY, -2039584, false);
                  rightY += lineHeight;
               }
            }
         }
      }
   }
}
