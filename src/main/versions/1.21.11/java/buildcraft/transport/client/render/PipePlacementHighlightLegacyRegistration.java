package buildcraft.transport.client.render;

import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.tile.TilePipeHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.21.x has no {@code LevelRenderer.extractBlockOutline}/{@code renderBlockOutline}; hook Fabric
 * {@link WorldRenderEvents} instead. Loaded only on 1.21.x builds via reflection from
 * {@link buildcraft.transport.platform.BCTransportFabricClient}.
 */
public final class PipePlacementHighlightLegacyRegistration {
   private PipePlacementHighlightLegacyRegistration() {
   }

   public static void register() {
      WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(PipePlacementHighlightLegacyRegistration::beforeBlockOutline);
   }

   private static boolean beforeBlockOutline(WorldRenderContext context, BlockOutlineRenderState outline) {
      Minecraft minecraft = Minecraft.getInstance();
      ClientLevel level = minecraft.level;
      LocalPlayer player = minecraft.player;
      if (level == null || player == null || !(minecraft.hitResult instanceof BlockHitResult hit)) {
         return true;
      }

      BlockPos pos = outline.pos();
      if (!pos.equals(hit.getBlockPos())) {
         return true;
      }

      BlockState state = level.getBlockState(pos);
      if (!(state.getBlock() instanceof BlockPipeHolder)) {
         return true;
      }

      if (!(level.getBlockEntity(pos) instanceof TilePipeHolder tile) || tile.getPipe() == null) {
         return true;
      }

      // Placement preview (holding a pluggable/wire), else the tight outline of the existing sub-part under the
      // crosshair; null means the pipe body -> let vanilla draw the whole-pipe outline.
      VoxelShape preview = PipePlacementHighlight.placementShape(tile, hit, player);
      if (preview == null) {
         preview = PipePlacementHighlight.hoveredPartShape(tile, hit);
      }

      if (preview == null) {
         return true;
      }

      LevelRenderState levelRenderState = context.worldState();
      Vec3 cam = levelRenderState.cameraRenderState.pos;
      PoseStack poseStack = context.matrices();
      VertexConsumer lines = context.consumers().getBuffer(BCLibRenderTypes.lines());
      ShapeRenderer.renderShape(poseStack, lines, preview, pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z, ARGB.black(102), 2.5F);
      return false;
   }
}
