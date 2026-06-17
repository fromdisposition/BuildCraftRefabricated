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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class AddonRendererFillerPlanner implements IFastAddonRenderer<AddonFillerPlanner> {
   //? if >= 26.1.3 {
   /*@Override
   public void renderAddonFast(AddonFillerPlanner addon, Player player, float partialTicks, PoseStack poseStack) {
      if (addon.buildingInfo != null) {
         List<BlockPos> cached = addon.getCachedPreviewPositions();
         if (!cached.isEmpty()) {
            List<BlockPos> toDraw = new ArrayList<>(cached);
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
            VertexConsumer vb = bufferSource.getBuffer(BCLibRenderTypes.debugFilled());
            Matrix4f pose = poseStack.last().pose();
            List<BlockPos> toDraw = new ArrayList<>(cached);
            toDraw.sort(Comparator.<BlockPos>comparingDouble(px -> player.position().distanceToSqr(Vec3.atCenterOf(px))).reversed());

            for (BlockPos p : toDraw) {
               drawPreviewCube(vb, pose, p);
            }
         }
      }
   }
   //?}

   private static void drawPreviewCube(VertexConsumer vb, Matrix4f pose, BlockPos p) {
      AABB bb = new AABB(Vec3.atLowerCornerOf(p), Vec3.atLowerCornerOf(p.offset(1, 1, 1))).inflate(-0.1);
      vertex(vb, pose, bb.minX, bb.maxY, bb.minZ, 204, 204, 204, 127, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F);
      vertex(vb, pose, bb.maxX, bb.maxY, bb.minZ, 204, 204, 204, 127, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F);
      vertex(vb, pose, bb.maxX, bb.minY, bb.minZ, 204, 204, 204, 127, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F);
      vertex(vb, pose, bb.minX, bb.minY, bb.minZ, 204, 204, 204, 127, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F);
      vertex(vb, pose, bb.minX, bb.minY, bb.maxZ, 204, 204, 204, 127, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      vertex(vb, pose, bb.maxX, bb.minY, bb.maxZ, 204, 204, 204, 127, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F);
      vertex(vb, pose, bb.maxX, bb.maxY, bb.maxZ, 204, 204, 204, 127, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
      vertex(vb, pose, bb.minX, bb.maxY, bb.maxZ, 204, 204, 204, 127, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      vertex(vb, pose, bb.minX, bb.minY, bb.minZ, 127, 127, 127, 127, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F);
      vertex(vb, pose, bb.maxX, bb.minY, bb.minZ, 127, 127, 127, 127, 0.0F, 1.0F, 0.0F, -1.0F, 0.0F);
      vertex(vb, pose, bb.maxX, bb.minY, bb.maxZ, 127, 127, 127, 127, 1.0F, 1.0F, 0.0F, -1.0F, 0.0F);
      vertex(vb, pose, bb.minX, bb.minY, bb.maxZ, 127, 127, 127, 127, 1.0F, 0.0F, 0.0F, -1.0F, 0.0F);
      vertex(vb, pose, bb.minX, bb.maxY, bb.maxZ, 255, 255, 255, 127, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F);
      vertex(vb, pose, bb.maxX, bb.maxY, bb.maxZ, 255, 255, 255, 127, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
      vertex(vb, pose, bb.maxX, bb.maxY, bb.minZ, 255, 255, 255, 127, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F);
      vertex(vb, pose, bb.minX, bb.maxY, bb.minZ, 255, 255, 255, 127, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F);
      vertex(vb, pose, bb.minX, bb.minY, bb.maxZ, 153, 153, 153, 127, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F);
      vertex(vb, pose, bb.minX, bb.maxY, bb.maxZ, 153, 153, 153, 127, 0.0F, 1.0F, -1.0F, 0.0F, 0.0F);
      vertex(vb, pose, bb.minX, bb.maxY, bb.minZ, 153, 153, 153, 127, 1.0F, 1.0F, -1.0F, 0.0F, 0.0F);
      vertex(vb, pose, bb.minX, bb.minY, bb.minZ, 153, 153, 153, 127, 1.0F, 0.0F, -1.0F, 0.0F, 0.0F);
      vertex(vb, pose, bb.maxX, bb.minY, bb.minZ, 153, 153, 153, 127, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
      vertex(vb, pose, bb.maxX, bb.maxY, bb.minZ, 153, 153, 153, 127, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
      vertex(vb, pose, bb.maxX, bb.maxY, bb.maxZ, 153, 153, 153, 127, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
      vertex(vb, pose, bb.maxX, bb.minY, bb.maxZ, 153, 153, 153, 127, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F);
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
