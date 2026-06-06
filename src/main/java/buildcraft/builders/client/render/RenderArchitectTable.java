package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileArchitectTable;
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

public class RenderArchitectTable extends BcBlockEntityRenderer<TileArchitectTable, ArchitectTableRenderState> {
   private static final int COLOUR_RED_HALF = -15658633;
   private static final RenderPartCube LED_GREEN = new RenderPartCube();
   private static final RenderPartCube LED_RED = new RenderPartCube();

   public RenderArchitectTable(Context context) {
   }

   public ArchitectTableRenderState createRenderState() {
      return new ArchitectTableRenderState();
   }

   @Override
   protected void extract(TileArchitectTable tile, ArchitectTableRenderState state, float partialTick) {
      BlockState blockState = tile.getBlockState();
      if (blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
         state.facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
         state.skipFace = state.facing.getOpposite();
         boolean valid = tile.getIsValid();
         boolean hasInput = !tile.getSnapshotIn().isEmpty();
         boolean hasOutput = !tile.getSnapshotOut().isEmpty();
         if (!valid) {
            state.greenColour = -14741477;
            state.redColour = -14540067;
         } else if (hasOutput) {
            state.greenColour = -8921737;
            state.redColour = COLOUR_RED_HALF;
         } else if (hasInput) {
            state.greenColour = -8921737;
            state.redColour = -14540067;
         } else {
            state.greenColour = -8921737;
            state.redColour = -14741477;
         }
      } else {
         state.facing = null;
      }
   }

   public void submit(ArchitectTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      if (renderState.facing != null) {
         Direction facing = renderState.facing;
         LedRenderUtil.setFacePosition(LED_GREEN, facing, 0.025, 0.15625, 0.21875);
         LedRenderUtil.setFacePosition(LED_RED, facing, 0.025, 0.28125, 0.21875);
         poseStack.pushPose();
         LedRenderUtil.submit(poseStack, collector, LED_GREEN, renderState.skipFace, renderState.greenColour);
         LedRenderUtil.submit(poseStack, collector, LED_RED, renderState.skipFace, renderState.redColour);
         poseStack.popPose();
      }
   }
}
