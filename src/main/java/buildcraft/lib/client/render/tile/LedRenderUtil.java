package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.render.BCLibRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public final class LedRenderUtil {
   public static final int COLOUR_OFF = -14741477;
   public static final int COLOUR_GREEN_ON = -8921737;
   public static final int COLOUR_RED_ON = -14540067;

   public static VertexConsumer begin(MultiBufferSource bufferSource) {
      return bufferSource.getBuffer(BCLibRenderTypes.led());
   }

   public static void submit(PoseStack poseStack, SubmitNodeCollector collector, RenderPartCube led, Direction skipFace, int colour) {
      BcBerRenderUtil.submit(poseStack, collector, BCLibRenderTypes.led(), (pose, consumer) -> render(led, pose, consumer, skipFace, colour));
   }

   public static void render(RenderPartCube led, Pose pose, VertexConsumer consumer, Direction skipFace, int colour) {
      led.center.colouri(colour);
      led.render(pose, consumer, skipFace);
   }

   public static void flush(MultiBufferSource.BufferSource bufferSource) {
      bufferSource.endBatch(BCLibRenderTypes.led());
   }

   public static void setFacePosition(RenderPartCube led, Direction face, double insetBlocks, double sideOffset, double y) {
      double ledX;
      double ledZ;
      int dX;
      int dZ;
      if (face.getAxis() == Direction.Axis.X) {
         dX = 0;
         dZ = face.getAxisDirection().getStep();
         ledZ = 0.5;
         ledX = face == Direction.EAST ? 1.0 - insetBlocks : insetBlocks;
      } else {
         dX = -face.getAxisDirection().getStep();
         dZ = 0;
         ledX = 0.5;
         ledZ = face == Direction.SOUTH ? 1.0 - insetBlocks : insetBlocks;
      }

      led.center.positiond(ledX + dX * sideOffset, y, ledZ + dZ * sideOffset);
   }

   private LedRenderUtil() {
   }
}
