/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.tooltip;

import buildcraft.builders.client.render.BlueprintRenderer;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.tooltip.BlueprintPreviewTooltipComponent;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.misc.HashUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BlueprintTooltipOverlay implements ClientTooltipComponent {
   private static final Logger LOGGER = LogManager.getLogger("BCBlueprintTooltipOverlay");
   public static final int PREVIEW_SIZE = 100;
   private static final Set<String> LOGGED_KEYS = Collections.synchronizedSet(new HashSet<>());
   private final Snapshot.Header header;

   public BlueprintTooltipOverlay(BlueprintPreviewTooltipComponent component) {
      this.header = component.header();
   }

   @Override
   public int getHeight(Font font) {
      return PREVIEW_SIZE;
   }

   @Override
   public int getWidth(Font font) {
      return PREVIEW_SIZE;
   }

   @Override
   public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
      Snapshot snapshot = ClientSnapshots.INSTANCE.getSnapshot(this.header.key);
      TooltipRenderUtil.extractTooltipBackground(graphics, x, y, PREVIEW_SIZE, PREVIEW_SIZE, null);
      if (snapshot != null) {
         BlueprintRenderer.renderSnapshot(new BCGraphics(graphics), snapshot, x, y, PREVIEW_SIZE, PREVIEW_SIZE);
      }

      logOnce(this.header.key, snapshot, x, y);
   }

   private static void logOnce(Snapshot.Key key, Snapshot snapshot, int pX, int pY) {
      String hashHex = key.hash == null ? "null" : HashUtil.convertHashToString(key.hash);
      if (LOGGED_KEYS.add(hashHex)) {
         LOGGER.info(
            "Overlay: hash={} snapshot={} at ({}, {}) {}x{}", hashHex, snapshot == null ? "pending" : snapshot.getClass().getSimpleName(), pX, pY, 100, 100
         );
      }
   }
}
