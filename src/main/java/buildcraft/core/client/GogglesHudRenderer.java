/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client;

import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.item.ItemGoggles;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Renders a compact MJ energy bar on the HUD when the player wears goggles and looks at a machine
 * that exposes {@link IMjReadable} via {@link MjAPI#CAP_READABLE}.
 *
 * Registered with {@code HudElementRegistry.addLast()} in BCCoreFabricClient.
 */
public final class GogglesHudRenderer {
   private static final int BAR_W = 100;
   private static final int BAR_H = 6;
   private static final int PADDING = 4;
   private static final int TEXT_COLOR = 0xFFE0E0E0;
   private static final int BAR_BG_COLOR = 0xFF333333;
   private static final int BAR_FG_COLOR = 0xFF00AA44;
   private static final int PANEL_BG_COLOR = 0xAA111111;

   private GogglesHudRenderer() {
   }

   public static void render(GuiGraphicsExtractor vanillaGraphics, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      if (player == null || mc.level == null || !ItemGoggles.isWearing(player)) {
         return;
      }

      HitResult hit = mc.hitResult;
      if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS) {
         return;
      }

      BlockPos pos = blockHit.getBlockPos();
      Direction face = blockHit.getDirection();
      Level level = mc.level;

      IMjReadable readable = MjAPI.CAP_READABLE.find(level, pos, null, null, face);
      if (readable == null) {
         return;
      }

      long stored = readable.getStored();
      long capacity = readable.getCapacity();
      if (capacity <= 0) {
         return;
      }

      BCGraphics g = new BCGraphics(vanillaGraphics);
      Font font = mc.font;

      String storedStr = MjAPI.formatMj(stored) + " / " + MjAPI.formatMj(capacity) + " MJ";
      int textW = font.width(storedStr);
      int totalW = Math.max(BAR_W, textW) + PADDING * 2;
      int textH = font.lineHeight;
      int totalH = textH + PADDING + BAR_H + PADDING * 2;

      int screenW = mc.getWindow().getGuiScaledWidth();
      int screenH = mc.getWindow().getGuiScaledHeight();
      int panelX = (screenW - totalW) / 2;
      int panelY = screenH / 2 + 22;

      g.fill(panelX, panelY, panelX + totalW, panelY + totalH, PANEL_BG_COLOR);

      int textX = panelX + PADDING;
      int textY = panelY + PADDING;
      g.text(font, storedStr, textX, textY, TEXT_COLOR, false);

      int barX = panelX + PADDING;
      int barY = textY + textH + PADDING;
      int barW = totalW - PADDING * 2;
      g.fill(barX, barY, barX + barW, barY + BAR_H, BAR_BG_COLOR);
      int fillW = (int) (barW * Math.min(1.0, (double) stored / capacity));
      if (fillW > 0) {
         g.fill(barX, barY, barX + fillW, barY + BAR_H, BAR_FG_COLOR);
      }
   }
}
