package buildcraft.builders.client.render;

import buildcraft.api.tiles.IControllable;
import buildcraft.builders.tile.TileFiller;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
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

   @Override
   protected void extract(TileFiller tile, FillerRenderState state, float partialTick) {
      IControllable.Mode controlMode = tile.getControlMode();
      boolean hasPower = tile.hasPower();
      boolean finished = tile.isFinished();
      if (controlMode == IControllable.Mode.OFF) {
         state.greenColour = -14741477;
         state.redColour = -14741477;
      } else if (!hasPower) {
         state.greenColour = -14741477;
         state.redColour = -14540067;
      } else if (finished) {
         state.greenColour = -8921737;
         state.redColour = -14540067;
      } else if (controlMode == IControllable.Mode.LOOP) {
         state.greenColour = -12617921;
         state.redColour = -14741477;
      } else {
         state.greenColour = -8921737;
         state.redColour = -14741477;
      }
   }

   public void submit(FillerRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      poseStack.pushPose();

      for (int i = 0; i < 4; i++) {
         Direction skipFace = Direction.from2DDataValue(i).getOpposite();
         LedRenderUtil.submit(poseStack, collector, LED_GREEN[i], skipFace, renderState.greenColour);
         LedRenderUtil.submit(poseStack, collector, LED_RED[i], skipFace, renderState.redColour);
      }

      poseStack.popPose();
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
