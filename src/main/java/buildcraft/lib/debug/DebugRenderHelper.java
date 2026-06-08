/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class DebugRenderHelper {
   private DebugRenderHelper() {
   }

   public static void renderTranslucentBox(PoseStack poseStack, MultiBufferSource bufferSource, AABB box, Vec3 cameraPos, int argb) {
      renderBox(poseStack, bufferSource, box, cameraPos, argb, BCLibRenderTypes.debugFilled());
   }

   public static void renderSolidBox(PoseStack poseStack, MultiBufferSource bufferSource, AABB box, Vec3 cameraPos, int argb) {
      renderBox(poseStack, bufferSource, box, cameraPos, argb, BCLibRenderTypes.debugSolid());
   }

   private static void renderBox(PoseStack poseStack, MultiBufferSource bufferSource, AABB box, Vec3 cameraPos, int argb, RenderType type) {
      VertexConsumer consumer = bufferSource.getBuffer(type);
      RenderPartCube cube = new RenderPartCube();
      cube.center.positiond((box.minX + box.maxX) / 2.0 - cameraPos.x, (box.minY + box.maxY) / 2.0 - cameraPos.y, (box.minZ + box.maxZ) / 2.0 - cameraPos.z);
      cube.sizeX = box.getXsize();
      cube.sizeY = box.getYsize();
      cube.sizeZ = box.getZsize();
      cube.center.colouri(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24 & 0xFF);
      cube.render(poseStack.last(), consumer);
   }
}
