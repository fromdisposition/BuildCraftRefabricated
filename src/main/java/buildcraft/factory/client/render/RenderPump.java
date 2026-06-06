package buildcraft.factory.client.render;

import buildcraft.factory.tile.TilePump;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;

public class RenderPump extends BcBlockEntityRenderer<TilePump, PumpRenderState> {
   private static final int[] COLOUR_POWER = new int[16];
   private static final double LED_INSET = 0.025;
   private static final double POWER_OFFSET = 0.09375;
   private static final double STATUS_OFFSET = 0.21875;
   private static final double Y = 0.84375;
   private static final RenderPartCube[] LED_POWER = new RenderPartCube[4];
   private static final RenderPartCube[] LED_STATUS = new RenderPartCube[4];

   public RenderPump(Context context) {
   }

   public PumpRenderState createRenderState() {
      return new PumpRenderState();
   }

   public void submit(PumpRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      TilePump tile = renderState.tile;
      if (tile != null) {
         poseStack.pushPose();
         BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
         this.renderLEDs(tile, poseStack, bufferSource);
         LedRenderUtil.flush(bufferSource);
         poseStack.popPose();
      }
   }

   private void renderLEDs(TilePump tile, PoseStack poseStack, BufferSource bufferSource) {
      float percentFilled = tile.getPercentFilledForRender();
      int powerColour = COLOUR_POWER[(int)(percentFilled * (COLOUR_POWER.length - 1))];
      boolean complete = tile.isComplete();
      int statusColour = complete ? -14741477 : -8921737;
      VertexConsumer consumer = LedRenderUtil.begin(bufferSource);
      Pose pose = poseStack.last();

      for (int i = 0; i < 4; i++) {
         Direction skipFace = Direction.from2DDataValue(i).getOpposite();
         LedRenderUtil.render(LED_POWER[i], pose, consumer, skipFace, powerColour);
         LedRenderUtil.render(LED_STATUS[i], pose, consumer, skipFace, statusColour);
      }
   }

   static {
      for (int i = 0; i < COLOUR_POWER.length; i++) {
         int c = i * 64 / COLOUR_POWER.length;
         int r = i * 224 / COLOUR_POWER.length + 31;
         COLOUR_POWER[i] = 0xFF000000 | c << 16 | c << 8 | r;
      }

      for (int i = 0; i < 4; i++) {
         Direction face = Direction.from2DDataValue(i);
         LED_POWER[i] = new RenderPartCube();
         LED_STATUS[i] = new RenderPartCube();
         LedRenderUtil.setFacePosition(LED_POWER[i], face, 0.025, 0.09375, 0.84375);
         LedRenderUtil.setFacePosition(LED_STATUS[i], face, 0.025, 0.21875, 0.84375);
      }
   }
}
