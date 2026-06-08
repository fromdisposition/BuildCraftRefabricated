/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.render.BCLibRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class AddonDefaultRenderer<T extends Addon> implements IFastAddonRenderer<T> {
   private ISprite sprite;

   public AddonDefaultRenderer() {
   }

   public AddonDefaultRenderer(ISprite sprite) {
      this.sprite = sprite;
   }

   @Override
   public void renderAddonFast(T addon, Player player, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
      VertexConsumer builder = bufferSource.getBuffer(BCLibRenderTypes.entityTranslucent(BcTextureAtlases.BLOCKS_TEXTURE));
      AABB bb = addon.getBoundingBox();
      Matrix4f pose = poseStack.last().pose();
      float u0 = this.sprite != null ? (float)this.sprite.getInterpU(0.0) : 0.0F;
      float u1 = this.sprite != null ? (float)this.sprite.getInterpU(1.0) : 1.0F;
      float v0 = this.sprite != null ? (float)this.sprite.getInterpV(0.0) : 0.0F;
      float v1 = this.sprite != null ? (float)this.sprite.getInterpV(1.0) : 1.0F;
      this.vertex(builder, pose, bb.minX, bb.maxY, bb.minZ, 204, 204, 204, 255, u0, v0, 0.0F, 0.0F, -1.0F);
      this.vertex(builder, pose, bb.maxX, bb.maxY, bb.minZ, 204, 204, 204, 255, u0, v1, 0.0F, 0.0F, -1.0F);
      this.vertex(builder, pose, bb.maxX, bb.minY, bb.minZ, 204, 204, 204, 255, u1, v1, 0.0F, 0.0F, -1.0F);
      this.vertex(builder, pose, bb.minX, bb.minY, bb.minZ, 204, 204, 204, 255, u1, v0, 0.0F, 0.0F, -1.0F);
      this.vertex(builder, pose, bb.minX, bb.minY, bb.maxZ, 204, 204, 204, 255, u0, v0, 0.0F, 0.0F, 1.0F);
      this.vertex(builder, pose, bb.maxX, bb.minY, bb.maxZ, 204, 204, 204, 255, u0, v1, 0.0F, 0.0F, 1.0F);
      this.vertex(builder, pose, bb.maxX, bb.maxY, bb.maxZ, 204, 204, 204, 255, u1, v1, 0.0F, 0.0F, 1.0F);
      this.vertex(builder, pose, bb.minX, bb.maxY, bb.maxZ, 204, 204, 204, 255, u1, v0, 0.0F, 0.0F, 1.0F);
      this.vertex(builder, pose, bb.minX, bb.minY, bb.minZ, 127, 127, 127, 255, u0, v0, 0.0F, -1.0F, 0.0F);
      this.vertex(builder, pose, bb.maxX, bb.minY, bb.minZ, 127, 127, 127, 255, u0, v1, 0.0F, -1.0F, 0.0F);
      this.vertex(builder, pose, bb.maxX, bb.minY, bb.maxZ, 127, 127, 127, 255, u1, v1, 0.0F, -1.0F, 0.0F);
      this.vertex(builder, pose, bb.minX, bb.minY, bb.maxZ, 127, 127, 127, 255, u1, v0, 0.0F, -1.0F, 0.0F);
      this.vertex(builder, pose, bb.minX, bb.maxY, bb.maxZ, 255, 255, 255, 255, u0, v0, 0.0F, 1.0F, 0.0F);
      this.vertex(builder, pose, bb.maxX, bb.maxY, bb.maxZ, 255, 255, 255, 255, u0, v1, 0.0F, 1.0F, 0.0F);
      this.vertex(builder, pose, bb.maxX, bb.maxY, bb.minZ, 255, 255, 255, 255, u1, v1, 0.0F, 1.0F, 0.0F);
      this.vertex(builder, pose, bb.minX, bb.maxY, bb.minZ, 255, 255, 255, 255, u1, v0, 0.0F, 1.0F, 0.0F);
      this.vertex(builder, pose, bb.minX, bb.minY, bb.maxZ, 153, 153, 153, 255, u0, v0, -1.0F, 0.0F, 0.0F);
      this.vertex(builder, pose, bb.minX, bb.maxY, bb.maxZ, 153, 153, 153, 255, u0, v1, -1.0F, 0.0F, 0.0F);
      this.vertex(builder, pose, bb.minX, bb.maxY, bb.minZ, 153, 153, 153, 255, u1, v1, -1.0F, 0.0F, 0.0F);
      this.vertex(builder, pose, bb.minX, bb.minY, bb.minZ, 153, 153, 153, 255, u1, v0, -1.0F, 0.0F, 0.0F);
      this.vertex(builder, pose, bb.maxX, bb.minY, bb.minZ, 153, 153, 153, 255, u0, v0, 1.0F, 0.0F, 0.0F);
      this.vertex(builder, pose, bb.maxX, bb.maxY, bb.minZ, 153, 153, 153, 255, u0, v1, 1.0F, 0.0F, 0.0F);
      this.vertex(builder, pose, bb.maxX, bb.maxY, bb.maxZ, 153, 153, 153, 255, u1, v1, 1.0F, 0.0F, 0.0F);
      this.vertex(builder, pose, bb.maxX, bb.minY, bb.maxZ, 153, 153, 153, 255, u1, v0, 1.0F, 0.0F, 0.0F);
   }

   private void vertex(
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
