package buildcraft.factory.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
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

   @Override
   protected void extract(TileMiningWell tile, MiningWellRenderState state, float partialTick) {
      BlockState blockState = tile.getBlockState();
      state.facing = blockState.is(BCFactoryBlocks.MINING_WELL) ? (Direction)blockState.getValue(BuildCraftProperties.BLOCK_FACING) : Direction.NORTH;
      float percentFilled = tile.getPercentFilledForRender();
      state.powerColour = COLOUR_POWER[(int)(percentFilled * (COLOUR_POWER.length - 1))];
      state.statusColour = tile.isComplete() ? -14741477 : -8921737;
   }

   public void submit(MiningWellRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      poseStack.pushPose();
      LedRenderUtil.setFacePosition(LED_POWER, renderState.facing, 0.0125, 0.15625, 0.34375);
      LedRenderUtil.setFacePosition(LED_STATUS, renderState.facing, 0.0125, 0.28125, 0.34375);
      Direction skipFace = renderState.facing.getOpposite();
      LedRenderUtil.submit(poseStack, collector, LED_POWER, skipFace, renderState.powerColour);
      LedRenderUtil.submit(poseStack, collector, LED_STATUS, skipFace, renderState.statusColour);
      poseStack.popPose();
   }

   static {
      for (int i = 0; i < COLOUR_POWER.length; i++) {
         int c = i * 64 / COLOUR_POWER.length & 0xFF;
         int r = (i * 176 / COLOUR_POWER.length & 0xFF) + 79;
         COLOUR_POWER[i] = 0xFF000000 | c << 16 | c << 8 | r;
      }
   }
}
