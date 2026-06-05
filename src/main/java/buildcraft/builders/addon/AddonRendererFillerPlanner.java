/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.addon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.client.render.BCLibRenderTypes;

import buildcraft.core.marker.volume.IFastAddonRenderer;

public class AddonRendererFillerPlanner implements IFastAddonRenderer<AddonFillerPlanner> {
    @Override
    public void renderAddonFast(AddonFillerPlanner addon, Player player, float partialTicks,
                                 PoseStack poseStack, MultiBufferSource bufferSource) {
        if (addon.buildingInfo == null) {
            return;
        }

        VertexConsumer vb = bufferSource.getBuffer(BCLibRenderTypes.debugFilled());

        Matrix4f pose = poseStack.last().pose();

        List<BlockPos> list = StreamSupport.stream(
            BlockPos.betweenClosed(addon.buildingInfo.box.min(), addon.buildingInfo.box.max()).spliterator(),
            false
        )
            .filter(blockPos ->
                addon.buildingInfo.getSnapshot().data.get(
                    addon.buildingInfo.getSnapshot().posToIndex(
                        addon.buildingInfo.fromWorld(blockPos)
                    )
                )
            )
            .filter(blockPos -> player.level().isEmptyBlock(blockPos))
            .map(BlockPos::immutable)
            .collect(Collectors.toCollection(ArrayList::new));

        list.sort(Comparator.<BlockPos>comparingDouble(p ->
            player.position().distanceToSqr(Vec3.atCenterOf(p))).reversed());

        for (BlockPos p : list) {
            AABB bb = new AABB(Vec3.atLowerCornerOf(p), Vec3.atLowerCornerOf(p.offset(1, 1, 1))).inflate(-0.1);

            vertex(vb, pose, bb.minX, bb.maxY, bb.minZ, 204, 204, 204, 127, 0, 0, 0, 0, -1);
            vertex(vb, pose, bb.maxX, bb.maxY, bb.minZ, 204, 204, 204, 127, 0, 1, 0, 0, -1);
            vertex(vb, pose, bb.maxX, bb.minY, bb.minZ, 204, 204, 204, 127, 1, 1, 0, 0, -1);
            vertex(vb, pose, bb.minX, bb.minY, bb.minZ, 204, 204, 204, 127, 1, 0, 0, 0, -1);

            vertex(vb, pose, bb.minX, bb.minY, bb.maxZ, 204, 204, 204, 127, 0, 0, 0, 0, 1);
            vertex(vb, pose, bb.maxX, bb.minY, bb.maxZ, 204, 204, 204, 127, 0, 1, 0, 0, 1);
            vertex(vb, pose, bb.maxX, bb.maxY, bb.maxZ, 204, 204, 204, 127, 1, 1, 0, 0, 1);
            vertex(vb, pose, bb.minX, bb.maxY, bb.maxZ, 204, 204, 204, 127, 1, 0, 0, 0, 1);

            vertex(vb, pose, bb.minX, bb.minY, bb.minZ, 127, 127, 127, 127, 0, 0, 0, -1, 0);
            vertex(vb, pose, bb.maxX, bb.minY, bb.minZ, 127, 127, 127, 127, 0, 1, 0, -1, 0);
            vertex(vb, pose, bb.maxX, bb.minY, bb.maxZ, 127, 127, 127, 127, 1, 1, 0, -1, 0);
            vertex(vb, pose, bb.minX, bb.minY, bb.maxZ, 127, 127, 127, 127, 1, 0, 0, -1, 0);

            vertex(vb, pose, bb.minX, bb.maxY, bb.maxZ, 255, 255, 255, 127, 0, 0, 0, 1, 0);
            vertex(vb, pose, bb.maxX, bb.maxY, bb.maxZ, 255, 255, 255, 127, 0, 1, 0, 1, 0);
            vertex(vb, pose, bb.maxX, bb.maxY, bb.minZ, 255, 255, 255, 127, 1, 1, 0, 1, 0);
            vertex(vb, pose, bb.minX, bb.maxY, bb.minZ, 255, 255, 255, 127, 1, 0, 0, 1, 0);

            vertex(vb, pose, bb.minX, bb.minY, bb.maxZ, 153, 153, 153, 127, 0, 0, -1, 0, 0);
            vertex(vb, pose, bb.minX, bb.maxY, bb.maxZ, 153, 153, 153, 127, 0, 1, -1, 0, 0);
            vertex(vb, pose, bb.minX, bb.maxY, bb.minZ, 153, 153, 153, 127, 1, 1, -1, 0, 0);
            vertex(vb, pose, bb.minX, bb.minY, bb.minZ, 153, 153, 153, 127, 1, 0, -1, 0, 0);

            vertex(vb, pose, bb.maxX, bb.minY, bb.minZ, 153, 153, 153, 127, 0, 0, 1, 0, 0);
            vertex(vb, pose, bb.maxX, bb.maxY, bb.minZ, 153, 153, 153, 127, 0, 1, 1, 0, 0);
            vertex(vb, pose, bb.maxX, bb.maxY, bb.maxZ, 153, 153, 153, 127, 1, 1, 1, 0, 0);
            vertex(vb, pose, bb.maxX, bb.minY, bb.maxZ, 153, 153, 153, 127, 1, 0, 1, 0, 0);
        }
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
