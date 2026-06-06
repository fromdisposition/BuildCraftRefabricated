package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class RenderArchitectTable implements BlockEntityRenderer<TileArchitectTable, ArchitectTableRenderState> {
   private static final int COLOUR_RED_HALF = -15658633;
   private static final double LED_INSET = 0.025;
   private static final double GREEN_OFFSET = 0.15625;
   private static final double RED_OFFSET = 0.28125;
   private static final double Y = 0.21875;
   private static final RenderPartCube LED_GREEN = new RenderPartCube();
   private static final RenderPartCube LED_RED = new RenderPartCube();

   public RenderArchitectTable(Context context) {
   }

   public ArchitectTableRenderState createRenderState() {
      return new ArchitectTableRenderState();
   }

   public void submit(ArchitectTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      Vec3 camPos = cameraState.pos;
      if (camPos != null) {
         Vector3f t = new Vector3f();
         poseStack.last().pose().getTranslation(t);
         BlockPos pos = new BlockPos(Math.round((float)(camPos.x + t.x)), Math.round((float)(camPos.y + t.y)), Math.round((float)(camPos.z + t.z)));
         Level level = Minecraft.getInstance().level;
         if (level != null) {
            if (level.getBlockEntity(pos) instanceof TileArchitectTable tile) {
               BlockState state = level.getBlockState(pos);
               if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                  Direction facing = (Direction)state.getValue(HorizontalDirectionalBlock.FACING);
                  poseStack.pushPose();
                  BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                  this.renderLEDs(tile, facing, poseStack, bufferSource);
                  bufferSource.endBatch();
                  poseStack.popPose();
               }
            }
         }
      }
   }

   private void renderLEDs(TileArchitectTable tile, Direction facing, PoseStack poseStack, BufferSource bufferSource) {
      boolean valid = tile.getIsValid();
      boolean hasInput = !tile.getSnapshotIn().isEmpty();
      boolean hasOutput = !tile.getSnapshotOut().isEmpty();
      int greenColour;
      int redColour;
      if (!valid) {
         greenColour = -14741477;
         redColour = -14540067;
      } else if (hasOutput) {
         greenColour = -8921737;
         redColour = -15658633;
      } else if (hasInput) {
         greenColour = -8921737;
         redColour = -14540067;
      } else {
         greenColour = -8921737;
         redColour = -14741477;
      }

      LedRenderUtil.setFacePosition(LED_GREEN, facing, 0.025, 0.15625, 0.21875);
      LedRenderUtil.setFacePosition(LED_RED, facing, 0.025, 0.28125, 0.21875);
      LED_GREEN.center.colouri(greenColour);
      LED_RED.center.colouri(redColour);
      VertexConsumer consumer = bufferSource.getBuffer(BCLibRenderTypes.led());
      Pose pose = poseStack.last();
      Direction skipFace = facing.getOpposite();
      LED_GREEN.render(pose, consumer, skipFace);
      LED_RED.render(pose, consumer, skipFace);
   }
}
