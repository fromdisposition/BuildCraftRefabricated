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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public final class BlueprintTooltipOverlay implements ClientTooltipComponent {
   public static final int PREVIEW_SIZE = 100;
   private final Snapshot.Header header;

   public BlueprintTooltipOverlay(BlueprintPreviewTooltipComponent component) {
      this.header = component.header();
   }

   @Override
   //? if >= 1.21.10 {
   public int getHeight(Font font) {
   //?} else {
   /*public int getHeight() {
   *///?}
      return PREVIEW_SIZE;
   }

   @Override
   public int getWidth(Font font) {
      return PREVIEW_SIZE;
   }

   @Override
   //? if >= 26.1 {
   public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
   //?} else if >= 1.21.10 {
   /*public void renderImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
   *///?} else {
   /*public void renderImage(Font font, int x, int y, GuiGraphicsExtractor graphics) {
   *///?}
      Snapshot snapshot = ClientSnapshots.INSTANCE.getSnapshot(this.header.key);
      if (snapshot != null) {
         BlueprintRenderer.renderSnapshotForTooltip(new BCGraphics(graphics), snapshot, x, y, PREVIEW_SIZE, PREVIEW_SIZE);
      }
   }
}
