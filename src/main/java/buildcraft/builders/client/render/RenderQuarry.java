package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
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

   @Override
   protected void extract(TileQuarry tile, QuarryRenderState state, float partialTick) {
      BlockState blockState = tile.getBlockState();
      Direction front = blockState.hasProperty(HorizontalDirectionalBlock.FACING)
         ? (Direction)blockState.getValue(HorizontalDirectionalBlock.FACING)
         : Direction.NORTH;
      state.rear = front.getOpposite();
      boolean hasPower = tile.hasPower();
      boolean hasTask = tile.isMining();
      boolean greenOn = hasPower || !hasTask;
      boolean redOn = !hasPower || !hasTask;
      state.greenColour = greenOn ? -8921737 : -14741477;
      state.redColour = redOn ? -14540067 : -14741477;
   }

   public void submit(QuarryRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      poseStack.pushPose();

      for (int i = 0; i < 4; i++) {
         Direction dir = Direction.from2DDataValue(i);
         if (dir != renderState.rear) {
            Direction skipFace = dir.getOpposite();
            LedRenderUtil.submit(poseStack, collector, LED_GREEN[i], skipFace, renderState.greenColour);
            LedRenderUtil.submit(poseStack, collector, LED_RED[i], skipFace, renderState.redColour);
         }
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
