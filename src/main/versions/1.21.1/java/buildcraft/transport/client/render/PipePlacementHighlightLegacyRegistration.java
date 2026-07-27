package buildcraft.transport.client.render;

import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.tile.TilePipeHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.21.1 has neither {@code extractBlockOutline} (26.x) nor the {@code rendering.v1.world} events (1.21.10/11);
 * hook the classic Fabric {@link WorldRenderEvents#BLOCK_OUTLINE} instead. Loaded only on the 1.21.1 build via
 * reflection from {@link buildcraft.transport.platform.BCTransportFabricClient}.
 *
 * <p>Draws the pluggable/wire placement preview when one is held, otherwise the tight outline of the sub-part
 * (pluggable, wire, arm or centre) under the crosshair; returning {@code false} cancels the vanilla outline, which
 * would otherwise wrap the pipe's full composed shape. Lines are emitted into the same buffer vanilla uses for its
 * own outline ({@link WorldRenderContext.BlockOutlineContext#vertexConsumer()}), colour-matched to vanilla.
 */
public final class PipePlacementHighlightLegacyRegistration {
   private PipePlacementHighlightLegacyRegistration() {
   }

   public static void register() {
      WorldRenderEvents.BLOCK_OUTLINE.register(PipePlacementHighlightLegacyRegistration::onBlockOutline);
   }

   private static boolean onBlockOutline(WorldRenderContext context, WorldRenderContext.BlockOutlineContext outline) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      if (player == null || !(minecraft.hitResult instanceof BlockHitResult hit)) {
         return true;
      }

      BlockPos pos = outline.blockPos();
      if (!pos.equals(hit.getBlockPos()) || !(outline.blockState().getBlock() instanceof BlockPipeHolder)) {
         return true;
      }

      if (context.world() == null
         || !(context.world().getBlockEntity(pos) instanceof TilePipeHolder tile)
         || tile.getPipe() == null) {
         return true;
      }

      if (context.consumers() == null) {
         return true;
      }

      VoxelShape shape = PipePlacementHighlight.placementShape(tile, hit, player);
      if (shape == null) {
         shape = PipePlacementHighlight.hoveredPartShape(tile, hit);
      }

      renderShape(
         context.matrixStack(), context.consumers().getBuffer(RenderType.lines()), shape,
         pos.getX() - outline.cameraX(), pos.getY() - outline.cameraY(), pos.getZ() - outline.cameraZ()
      );
      return false;
   }

   /** Vanilla 1.21.1 LevelRenderer.renderShape: one colour-and-normal'd line per shape edge. */
   private static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z) {
      PoseStack.Pose pose = poseStack.last();
      shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
         float dx = (float)(x2 - x1);
         float dy = (float)(y2 - y1);
         float dz = (float)(z2 - z1);
         float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
         dx /= len;
         dy /= len;
         dz /= len;
         consumer.addVertex(pose, (float)(x1 + x), (float)(y1 + y), (float)(z1 + z)).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(pose, dx, dy, dz);
         consumer.addVertex(pose, (float)(x2 + x), (float)(y2 + y), (float)(z2 + z)).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(pose, dx, dy, dz);
      });
   }
}
