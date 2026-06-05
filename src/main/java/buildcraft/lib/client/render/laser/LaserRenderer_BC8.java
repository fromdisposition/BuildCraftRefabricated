/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.render.laser;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;

@SuppressWarnings("deprecation")
public class LaserRenderer_BC8 {
    private static final Map<LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new HashMap<>();

    public static void clearModels() {
        COMPILED_LASER_TYPES.clear();
    }

    private static CompiledLaserType compileType(LaserType laserType) {
        return COMPILED_LASER_TYPES.computeIfAbsent(laserType, CompiledLaserType::new);
    }

    public static void renderLaser(PoseStack poseStack, VertexConsumer consumer,
            LaserData_BC8 data, Vec3 cameraPos) {

        ILaserRenderer vertexWriter = (x, y, z, u, v, lmap, nx, ny, nz, colour) -> {

            float rx = (float) (x - cameraPos.x);
            float ry = (float) (y - cameraPos.y);
            float rz = (float) (z - cameraPos.z);

            int r = (int) (colour * 255);
            int g = (int) (colour * 255);
            int b = (int) (colour * 255);
            int a = 255;

            consumer.addVertex(poseStack.last().pose(), rx, ry, rz)
                    .setColor(r, g, b, a)
                    .setUv((float) u, (float) v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lmap)
                    .setNormal(poseStack.last(), nx, ny, nz);
        };

        LaserContext ctx = new LaserContext(vertexWriter, data, data.enableDiffuse, data.doubleFace);
        CompiledLaserType type = compileType(data.laserType);
        type.bakeFor(ctx);
    }

    public static void renderLaserStatic(PoseStack poseStack, LaserData_BC8 data, Vec3 cameraPos) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        VertexConsumer consumer = bufferSource.getBuffer(
                BCLibRenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        renderLaser(poseStack, consumer, data, cameraPos);
        bufferSource.endBatch();
    }

    public static int computeLightmap(double x, double y, double z, int minBlockLight) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return buildcraft.lib.client.render.LightUtil.FULL_BRIGHT;

        BlockPos pos = BlockPos.containing(x, y, z);
        int blockLight = minBlockLight >= 15 ? 15 :
                Math.max(minBlockLight, level.getBrightness(LightLayer.BLOCK, pos));
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        return buildcraft.lib.client.render.LightUtil.pack(blockLight, skyLight);
    }
}
