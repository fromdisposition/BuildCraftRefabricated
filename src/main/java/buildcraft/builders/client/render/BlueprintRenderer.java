/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.client.render.pip.BlueprintPipRenderState;
import buildcraft.builders.client.render.pip.TooltipBlueprintPipRenderState;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.fabric.client.GuiGraphicsCompat;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

public class BlueprintRenderer {
   private static final float FIT_ENVELOPE = 1.05F;

   public static void renderSnapshot(BCGraphics graphics, Snapshot snapshot, int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
      submitSnapshot(graphics, snapshot, viewportX, viewportY, viewportWidth, viewportHeight, false);
   }

   public static void renderSnapshotForTooltip(BCGraphics graphics, Snapshot snapshot, int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
      submitSnapshot(graphics, snapshot, viewportX, viewportY, viewportWidth, viewportHeight, true);
   }

   private static void submitSnapshot(
      BCGraphics graphics, Snapshot snapshot, int viewportX, int viewportY, int viewportWidth, int viewportHeight, boolean tooltip
   ) {
      if (snapshot != null) {
         float scale = scaleForViewport(snapshot, viewportWidth, viewportHeight);
         ScreenRectangle scissor = GuiGraphicsCompat.peekScissorStack(graphics.raw);
         PictureInPictureRenderState state = tooltip
            ? new TooltipBlueprintPipRenderState(snapshot, viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight, scale, scissor)
            : new BlueprintPipRenderState(snapshot, viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight, scale, scissor);
         GuiGraphicsCompat.submitPictureInPictureRenderState(graphics.raw, state);
      }
   }

   private static float scaleForViewport(Snapshot snapshot, int viewportWidth, int viewportHeight) {
      int sizeX = Math.max(1, snapshot.size.getX());
      int sizeY = Math.max(1, snapshot.size.getY());
      int sizeZ = Math.max(1, snapshot.size.getZ());
      float diagonal = (float)Math.sqrt((double)sizeX * sizeX + (double)sizeY * sizeY + (double)sizeZ * sizeZ);
      float viewportSpan = Math.min(viewportWidth, viewportHeight);
      return viewportSpan / (diagonal * FIT_ENVELOPE);
   }
}
