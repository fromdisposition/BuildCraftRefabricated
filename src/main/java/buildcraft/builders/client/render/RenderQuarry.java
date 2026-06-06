package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileQuarry;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RenderQuarry extends BcBlockEntityRenderer<TileQuarry, QuarryRenderState> {
   private static final double LED_INSET = 0.025;
   private static final double GREEN_OFFSET = 0.09375;
   private static final double RED_OFFSET = 0.21875;
   private static final double Y = 0.84375;
   private static final RenderPartCube[] LED_GREEN = new RenderPartCube[4];
   private static final RenderPartCube[] LED_RED = new RenderPartCube[4];

   public RenderQuarry(Context context) {
   }

   public QuarryRenderState createRenderState() {
      return new QuarryRenderState();
   }

   public void submit(QuarryRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      TileQuarry tile = renderState.tile;
      if (tile != null) {
         BlockState state = tile.getBlockState();
         Direction front = state.hasProperty(HorizontalDirectionalBlock.FACING)
            ? (Direction)state.getValue(HorizontalDirectionalBlock.FACING)
            : Direction.NORTH;
         Direction rear = front.getOpposite();
         poseStack.pushPose();
         BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
         this.renderLEDs(tile, rear, poseStack, bufferSource);
         LedRenderUtil.flush(bufferSource);
         poseStack.popPose();
      }
   }

   private void renderLEDs(TileQuarry tile, Direction rear, PoseStack poseStack, BufferSource bufferSource) {
      boolean hasPower = tile.hasPower();
      boolean hasTask = tile.isMining();
      boolean greenOn = hasPower || !hasTask;
      boolean redOn = !hasPower || !hasTask;
      int greenColour = greenOn ? -8921737 : -14741477;
      int redColour = redOn ? -14540067 : -14741477;
      VertexConsumer consumer = LedRenderUtil.begin(bufferSource);
      Pose pose = poseStack.last();

      for (int i = 0; i < 4; i++) {
         Direction dir = Direction.from2DDataValue(i);
         if (dir != rear) {
            Direction skipFace = dir.getOpposite();
            LedRenderUtil.render(LED_GREEN[i], pose, consumer, skipFace, greenColour);
            LedRenderUtil.render(LED_RED[i], pose, consumer, skipFace, redColour);
         }
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
