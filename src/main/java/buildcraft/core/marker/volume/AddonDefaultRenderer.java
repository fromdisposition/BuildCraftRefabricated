/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.MultiBufferSource;
import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import buildcraft.api.core.render.ISprite;

@SuppressWarnings("deprecation")
public class AddonDefaultRenderer<T extends Addon> implements IFastAddonRenderer<T> {
    private ISprite sprite;

    public AddonDefaultRenderer() {
    }

    public AddonDefaultRenderer(ISprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public void renderAddonFast(T addon, Player player, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer builder = bufferSource.getBuffer(BCLibRenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        AABB bb = addon.getBoundingBox();
        Matrix4f pose = poseStack.last().pose();

        float u0 = sprite != null ? (float) sprite.getInterpU(0) : 0;
        float u1 = sprite != null ? (float) sprite.getInterpU(1) : 1;
        float v0 = sprite != null ? (float) sprite.getInterpV(0) : 0;
        float v1 = sprite != null ? (float) sprite.getInterpV(1) : 1;

        vertex(builder, pose, bb.minX, bb.maxY, bb.minZ, 204, 204, 204, 255, u0, v0, 0, 0, -1);
        vertex(builder, pose, bb.maxX, bb.maxY, bb.minZ, 204, 204, 204, 255, u0, v1, 0, 0, -1);
        vertex(builder, pose, bb.maxX, bb.minY, bb.minZ, 204, 204, 204, 255, u1, v1, 0, 0, -1);
        vertex(builder, pose, bb.minX, bb.minY, bb.minZ, 204, 204, 204, 255, u1, v0, 0, 0, -1);

        vertex(builder, pose, bb.minX, bb.minY, bb.maxZ, 204, 204, 204, 255, u0, v0, 0, 0, 1);
        vertex(builder, pose, bb.maxX, bb.minY, bb.maxZ, 204, 204, 204, 255, u0, v1, 0, 0, 1);
        vertex(builder, pose, bb.maxX, bb.maxY, bb.maxZ, 204, 204, 204, 255, u1, v1, 0, 0, 1);
        vertex(builder, pose, bb.minX, bb.maxY, bb.maxZ, 204, 204, 204, 255, u1, v0, 0, 0, 1);

        vertex(builder, pose, bb.minX, bb.minY, bb.minZ, 127, 127, 127, 255, u0, v0, 0, -1, 0);
        vertex(builder, pose, bb.maxX, bb.minY, bb.minZ, 127, 127, 127, 255, u0, v1, 0, -1, 0);
        vertex(builder, pose, bb.maxX, bb.minY, bb.maxZ, 127, 127, 127, 255, u1, v1, 0, -1, 0);
        vertex(builder, pose, bb.minX, bb.minY, bb.maxZ, 127, 127, 127, 255, u1, v0, 0, -1, 0);

        vertex(builder, pose, bb.minX, bb.maxY, bb.maxZ, 255, 255, 255, 255, u0, v0, 0, 1, 0);
        vertex(builder, pose, bb.maxX, bb.maxY, bb.maxZ, 255, 255, 255, 255, u0, v1, 0, 1, 0);
        vertex(builder, pose, bb.maxX, bb.maxY, bb.minZ, 255, 255, 255, 255, u1, v1, 0, 1, 0);
        vertex(builder, pose, bb.minX, bb.maxY, bb.minZ, 255, 255, 255, 255, u1, v0, 0, 1, 0);

        vertex(builder, pose, bb.minX, bb.minY, bb.maxZ, 153, 153, 153, 255, u0, v0, -1, 0, 0);
        vertex(builder, pose, bb.minX, bb.maxY, bb.maxZ, 153, 153, 153, 255, u0, v1, -1, 0, 0);
        vertex(builder, pose, bb.minX, bb.maxY, bb.minZ, 153, 153, 153, 255, u1, v1, -1, 0, 0);
        vertex(builder, pose, bb.minX, bb.minY, bb.minZ, 153, 153, 153, 255, u1, v0, -1, 0, 0);

        vertex(builder, pose, bb.maxX, bb.minY, bb.minZ, 153, 153, 153, 255, u0, v0, 1, 0, 0);
        vertex(builder, pose, bb.maxX, bb.maxY, bb.minZ, 153, 153, 153, 255, u0, v1, 1, 0, 0);
        vertex(builder, pose, bb.maxX, bb.maxY, bb.maxZ, 153, 153, 153, 255, u1, v1, 1, 0, 0);
        vertex(builder, pose, bb.maxX, bb.minY, bb.maxZ, 153, 153, 153, 255, u1, v0, 1, 0, 0);
    }

    private void vertex(VertexConsumer vb, Matrix4f pose, double x, double y, double z,
                         int r, int g, int b, int a, float u, float v,
                         float nx, float ny, float nz) {
        vb.addVertex(pose, (float) x, (float) y, (float) z)
            .setColor(r, g, b, a)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);
    }
}
