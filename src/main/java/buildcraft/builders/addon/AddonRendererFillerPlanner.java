/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.addon;

import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.lib.client.render.BCLibRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import buildcraft.lib.client.render.laser.LaserBatch;
//? if >= 26.1.3 {
//?} else {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class AddonRendererFillerPlanner implements IFastAddonRenderer<AddonFillerPlanner> {
   // Reused across frames — rebuilt only when the cached positions list reference changes,
   // then re-sorted every frame for correct back-to-front alpha order.
   private List<BlockPos> lastCached;
   private final List<BlockPos> toDraw = new ArrayList<>();

   //? if >= 26.1.3 {
   /*@Override
   public void renderAddonFast(AddonFillerPlanner addon, Player player, float partialTicks, PoseStack poseStack) {
      if (addon.buildingInfo != null) {
         List<BlockPos> cached = addon.getCachedPreviewPositions();
         if (!cached.isEmpty()) {
            rebuildDrawList(cached);
            toDraw.sort(Comparator.<BlockPos>comparingDouble(px -> player.position().distanceToSqr(Vec3.atCenterOf(px))).reversed());
            LaserBatch.submitGeometry(poseStack, BCLibRenderTypes.debugFilled(), (pose, vc) -> {
               for (BlockPos p : toDraw) {
                  drawPreviewCube(vc, pose.pose(), p);
               }
            });
         }
      }
   }*/
   //?} else {
   @Override
   public void renderAddonFast(AddonFillerPlanner addon, Player player, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
      if (addon.buildingInfo != null) {
         List<BlockPos> cached = addon.getCachedPreviewPositions();
         if (!cached.isEmpty()) {
            rebuildDrawList(cached);
            toDraw.sort(Comparator.<BlockPos>comparingDouble(px -> player.position().distanceToSqr(Vec3.atCenterOf(px))).reversed());
            VertexConsumer vb = bufferSource.getBuffer(BCLibRenderTypes.debugFilled());
            Matrix4f pose = poseStack.last().pose();
            for (BlockPos p : toDraw) {
               drawPreviewCube(vb, pose, p);
            }
         }
      }
   }
   //?}

   private void rebuildDrawList(List<BlockPos> cached) {
      if (cached != this.lastCached) {
         this.lastCached = cached;
         this.toDraw.clear();
         this.toDraw.addAll(cached);
      }
   }

   private static void drawPreviewCube(VertexConsumer vb, Matrix4f pose, BlockPos p) {
      // Inline the inflate(-0.1) math to avoid two AABB allocations per block per frame.
      double minX = p.getX() + 0.1, minY = p.getY() + 0.1, minZ = p.getZ() + 0.1;
      double maxX = p.getX() + 0.9, maxY = p.getY() + 0.9, maxZ = p.getZ() + 0.9;
      vertex(vb, pose, minX, maxY, minZ, 204, 204, 204, 127, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F);
      vertex(vb, pose, maxX, maxY, minZ, 204, 204, 204, 127, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F);
      vertex(vb, pose, maxX, minY, minZ, 204, 204, 204, 127, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F);
      vertex(vb, pose, minX, minY, minZ, 204, 204, 204, 127, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F);
      vertex(vb, pose, minX, minY, maxZ, 204, 204, 204, 127, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      vertex(vb, pose, maxX, minY, maxZ, 204, 204, 204, 127, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F);
      vertex(vb, pose, maxX, maxY, maxZ, 204, 204, 204, 127, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
      vertex(vb, pose, minX, maxY, maxZ, 204, 204, 204, 127, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      vertex(vb, pose, minX, minY, minZ, 127, 127, 127, 127, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F);
      vertex(vb, pose, maxX, minY, minZ, 127, 127, 127, 127, 0.0F, 1.0F, 0.0F, -1.0F, 0.0F);
      vertex(vb, pose, maxX, minY, maxZ, 127, 127, 127, 127, 1.0F, 1.0F, 0.0F, -1.0F, 0.0F);
      vertex(vb, pose, minX, minY, maxZ, 127, 127, 127, 127, 1.0F, 0.0F, 0.0F, -1.0F, 0.0F);
      vertex(vb, pose, minX, maxY, maxZ, 255, 255, 255, 127, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F);
      vertex(vb, pose, maxX, maxY, maxZ, 255, 255, 255, 127, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
      vertex(vb, pose, maxX, maxY, minZ, 255, 255, 255, 127, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F);
      vertex(vb, pose, minX, maxY, minZ, 255, 255, 255, 127, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F);
      vertex(vb, pose, minX, minY, maxZ, 153, 153, 153, 127, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F);
      vertex(vb, pose, minX, maxY, maxZ, 153, 153, 153, 127, 0.0F, 1.0F, -1.0F, 0.0F, 0.0F);
      vertex(vb, pose, minX, maxY, minZ, 153, 153, 153, 127, 1.0F, 1.0F, -1.0F, 0.0F, 0.0F);
      vertex(vb, pose, minX, minY, minZ, 153, 153, 153, 127, 1.0F, 0.0F, -1.0F, 0.0F, 0.0F);
      vertex(vb, pose, maxX, minY, minZ, 153, 153, 153, 127, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
      vertex(vb, pose, maxX, maxY, minZ, 153, 153, 153, 127, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
      vertex(vb, pose, maxX, maxY, maxZ, 153, 153, 153, 127, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
      vertex(vb, pose, maxX, minY, maxZ, 153, 153, 153, 127, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F);
   }

   private static void vertex(
      VertexConsumer vb, Matrix4f pose, double x, double y, double z, int r, int g, int b, int a, float u, float v, float nx, float ny, float nz
   ) {
      vb.addVertex(pose, (float)x, (float)y, (float)z)
         .setColor(r, g, b, a)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(nx, ny, nz);
   }
}
