package buildcraft.builders.client.render;

import buildcraft.api.tiles.IControllable;
import buildcraft.builders.tile.TileFiller;
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

public class RenderFiller extends BcBlockEntityRenderer<TileFiller, FillerRenderState> {
   private static final int COLOUR_GREEN_HALF = -12617921;
   private static final double LED_INSET = 0.025;
   private static final double GREEN_OFFSET = 0.09375;
   private static final double RED_OFFSET = 0.21875;
   private static final double Y = 0.84375;
   private static final RenderPartCube[] LED_GREEN = new RenderPartCube[4];
   private static final RenderPartCube[] LED_RED = new RenderPartCube[4];

   public RenderFiller(Context context) {
   }

   public FillerRenderState createRenderState() {
      return new FillerRenderState();
   }

   public void submit(FillerRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      TileFiller tile = renderState.tile;
      if (tile != null) {
         poseStack.pushPose();
         BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
         this.renderLEDs(tile, poseStack, bufferSource);
         LedRenderUtil.flush(bufferSource);
         poseStack.popPose();
      }
   }

   private void renderLEDs(TileFiller tile, PoseStack poseStack, BufferSource bufferSource) {
      IControllable.Mode controlMode = tile.getControlMode();
      boolean hasPower = tile.hasPower();
      boolean finished = tile.isFinished();
      int greenColour;
      int redColour;
      if (controlMode == IControllable.Mode.OFF) {
         greenColour = -14741477;
         redColour = -14741477;
      } else if (!hasPower) {
         greenColour = -14741477;
         redColour = -14540067;
      } else if (finished) {
         greenColour = -8921737;
         redColour = -14540067;
      } else if (controlMode == IControllable.Mode.LOOP) {
         greenColour = -12617921;
         redColour = -14741477;
      } else {
         greenColour = -8921737;
         redColour = -14741477;
      }

      VertexConsumer consumer = LedRenderUtil.begin(bufferSource);
      Pose pose = poseStack.last();

      for (int i = 0; i < 4; i++) {
         Direction skipFace = Direction.from2DDataValue(i).getOpposite();
         LedRenderUtil.render(LED_GREEN[i], pose, consumer, skipFace, greenColour);
         LedRenderUtil.render(LED_RED[i], pose, consumer, skipFace, redColour);
      }
   }

   static {
      for (int i = 0; i < 4; i++) {
         Direction face = Direction.from2DDataValue(i);
         LED_GREEN[i] = new RenderPartCube();
         LED_RED[i] = new RenderPartCube();
         LedRenderUtil.setFacePosition(LED_GREEN[i], face, 0.025, 0.09375, 0.84375);
         LedRenderUtil.setFacePosition(LED_RED[i], face, 0.025, 0.21875, 0.84375);
      }
   }
}
