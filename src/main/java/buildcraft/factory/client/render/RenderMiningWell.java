package buildcraft.factory.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileMiningWell;
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
import net.minecraft.world.level.block.state.BlockState;

public class RenderMiningWell extends BcBlockEntityRenderer<TileMiningWell, MiningWellRenderState> {
   private static final int[] COLOUR_POWER = new int[16];
   private static final double LED_INSET = 0.0125;
   private static final double POWER_OFFSET = 0.15625;
   private static final double STATUS_OFFSET = 0.28125;
   private static final double Y = 0.34375;
   private static final RenderPartCube LED_POWER = new RenderPartCube();
   private static final RenderPartCube LED_STATUS = new RenderPartCube();

   public RenderMiningWell(Context context) {
   }

   public MiningWellRenderState createRenderState() {
      return new MiningWellRenderState();
   }

   public void submit(MiningWellRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      TileMiningWell tile = renderState.tile;
      if (tile != null) {
         poseStack.pushPose();
         BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
         this.renderLEDs(tile, poseStack, bufferSource);
         LedRenderUtil.flush(bufferSource);
         poseStack.popPose();
      }
   }

   private void renderLEDs(TileMiningWell tile, PoseStack poseStack, BufferSource bufferSource) {
      BlockState state = tile.getBlockState();
      Direction facing = state.is(BCFactoryBlocks.MINING_WELL) ? (Direction)state.getValue(BuildCraftProperties.BLOCK_FACING) : Direction.NORTH;
      float percentFilled = tile.getPercentFilledForRender();
      int powerColour = COLOUR_POWER[(int)(percentFilled * (COLOUR_POWER.length - 1))];
      boolean complete = tile.isComplete();
      int statusColour = complete ? -14741477 : -8921737;
      LedRenderUtil.setFacePosition(LED_POWER, facing, 0.0125, 0.15625, 0.34375);
      LedRenderUtil.setFacePosition(LED_STATUS, facing, 0.0125, 0.28125, 0.34375);
      VertexConsumer consumer = LedRenderUtil.begin(bufferSource);
      Pose pose = poseStack.last();
      Direction skipFace = facing.getOpposite();
      LedRenderUtil.render(LED_POWER, pose, consumer, skipFace, powerColour);
      LedRenderUtil.render(LED_STATUS, pose, consumer, skipFace, statusColour);
   }

   static {
      for (int i = 0; i < COLOUR_POWER.length; i++) {
         int c = i * 64 / COLOUR_POWER.length & 0xFF;
         int r = (i * 176 / COLOUR_POWER.length & 0xFF) + 79;
         COLOUR_POWER[i] = 0xFF000000 | c << 16 | c << 8 | r;
      }
   }
}
