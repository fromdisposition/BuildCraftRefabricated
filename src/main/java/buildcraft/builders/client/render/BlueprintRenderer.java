package buildcraft.builders.client.render;

import buildcraft.builders.client.render.pip.BlueprintPipRenderState;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.fabric.client.GuiGraphicsCompat;
import buildcraft.lib.gui.BCGraphics;

public class BlueprintRenderer {
   private static final float FIT_ENVELOPE = 1.05F;

   public static void renderSnapshot(BCGraphics graphics, Snapshot snapshot, int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
      if (snapshot != null) {
         int sizeX = Math.max(1, snapshot.size.getX());
         int sizeY = Math.max(1, snapshot.size.getY());
         int sizeZ = Math.max(1, snapshot.size.getZ());
         float diagonal = (float)Math.sqrt((double)sizeX * sizeX + (double)sizeY * sizeY + (double)sizeZ * sizeZ);
         float viewportSpan = Math.min(viewportWidth, viewportHeight);
         float scale = viewportSpan / (diagonal * 1.05F);
         BlueprintPipRenderState state = new BlueprintPipRenderState(
            snapshot, viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight, scale, GuiGraphicsCompat.peekScissorStack(graphics.raw)
         );
         GuiGraphicsCompat.submitPictureInPictureRenderState(graphics.raw, state);
      }
   }
}
