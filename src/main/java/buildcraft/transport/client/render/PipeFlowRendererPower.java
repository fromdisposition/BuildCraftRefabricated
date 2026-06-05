/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;

import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowPower.Section;

@SuppressWarnings("deprecation")
public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
    INSTANCE;

    @Override
    public void render(PipeFlowPower flow, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack.Pose pose) {
        double centrePower = 0;
        double[] power = new double[6];
        for (Direction side : Direction.values()) {
            Section s = flow.getSection(side);
            int i = side.ordinal();
            power[i] = s.displayPower / (double) MjAPI.MJ;
            centrePower = Math.max(centrePower, power[i]);
        }

        if (centrePower > 0) {

            MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer powerBB = bufferSource.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));

            for (Direction side : Direction.values()) {
                if (!flow.pipe.isConnected(side)) {
                    continue;
                }
                int i = side.ordinal();
                Section s = flow.getSection(side);
                double offset = computeOffset(s.clientDisplayFlowLast, s.clientDisplayFlow, partialTicks);
                renderSidePower(side, power[i], centrePower, offset, powerBB, pose);
            }

            Vec3 offsetLast = flow.clientDisplayFlowCentreLast;
            Vec3 offsetThis = flow.clientDisplayFlowCentre;
            double offsetX = computeOffset(offsetLast.x, offsetThis.x, partialTicks);
            double offsetY = computeOffset(offsetLast.y, offsetThis.y, partialTicks);
            double offsetZ = computeOffset(offsetLast.z, offsetThis.z, partialTicks);

            renderCentrePower(centrePower, offsetX, offsetY, offsetZ, powerBB, pose);

            bufferSource.endBatch(net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
        }
    }

    private static double computeOffset(double tick0, double tick1, float partialTicks) {
        if (tick0 + 8 < tick1) {
            tick0 += 16;
        } else if (tick1 + 8 < tick0) {
            tick1 += 16;
        }
        double offset = MathUtil.interp(partialTicks, tick0, tick1);
        if (offset >= 16) {
            offset -= 16;
        }
        return offset;
    }

    private static void renderSidePower(Direction side, double power, double centrePower, double offset,
        VertexConsumer bb, PoseStack.Pose pose) {
        if (power < 0) {
            return;
        }

        TextureAtlasSprite sprite = BCTransportSprites.POWER_FLOW.getSprite();
        if (sprite == null) return;

        AABB box = sideFlowBox(side, power, centrePower);
        Vec3 offsetVec = VecUtil.offset(Vec3.ZERO, side,
            offset * side.getAxisDirection().getStep() / 32.0);

        UvFaceData uvs = new UvFaceData();
        for (Direction face : Direction.values()) {
            if (face == side.getOpposite()) {
                continue;
            }
            renderScrollingBox(box, offsetVec, face, uvs, sprite, bb, pose);
        }
    }

    static AABB sideFlowBox(Direction side, double power, double centrePower) {
        double p = Math.min(Math.max(power, 0), 1);
        double c = Math.min(Math.max(centrePower, 0), 1);
        double radius = 0.248 * p;
        double centreRadius = 0.252 - 0.248 * c;

        Vec3 centre = VecUtil.offset(VecUtil.VEC_HALF, side, 0.25 + 0.125 - centreRadius / 2);
        Vec3 radiusV = new Vec3(radius, radius, radius);
        radiusV = VecUtil.replaceValue(radiusV, side.getAxis(), 0.125 + centreRadius / 2);

        return new AABB(
            centre.x - radiusV.x, centre.y - radiusV.y, centre.z - radiusV.z,
            centre.x + radiusV.x, centre.y + radiusV.y, centre.z + radiusV.z
        );
    }

    private static void renderCentrePower(double power, double offsetX, double offsetY, double offsetZ,
        VertexConsumer bb, PoseStack.Pose pose) {
        float radius = 0.248f * (float) power;
        if (radius > 0.248f) {
            radius = 0.248f;
        }
        TextureAtlasSprite sprite = BCTransportSprites.POWER_FLOW.getSprite();
        if (sprite == null) return;

        Vector3f centre = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radiusP = new Vector3f(radius, radius, radius);

        UvFaceData uvs = new UvFaceData();

        for (Direction face : Direction.values()) {
            AABB box = new AABB(
                0.5 - radius, 0.5 - radius, 0.5 - radius,
                0.5 + radius, 0.5 + radius, 0.5 + radius
            );
            Vec3 offsetVec = new Vec3(offsetX / 32.0, offsetY / 32.0, offsetZ / 32.0);

            renderScrollingBox(box, offsetVec, face, uvs, sprite, bb, pose);
        }
    }

    private static void renderScrollingBox(AABB baseBox, Vec3 offsetVec, Direction face, UvFaceData uvs, TextureAtlasSprite sprite, VertexConsumer bb, PoseStack.Pose pose) {
        AABB movedBox = baseBox.move(offsetVec.x, offsetVec.y, offsetVec.z);

        int fMinX = (int) Math.floor(movedBox.minX);
        int fMaxX = (int) Math.floor(movedBox.maxX);
        int fMinY = (int) Math.floor(movedBox.minY);
        int fMaxY = (int) Math.floor(movedBox.maxY);
        int fMinZ = (int) Math.floor(movedBox.minZ);
        int fMaxZ = (int) Math.floor(movedBox.maxZ);

        for (int i = fMinX; i <= fMaxX; i++) {
            for (int j = fMinY; j <= fMaxY; j++) {
                for (int k = fMinZ; k <= fMaxZ; k++) {
                    double pMinX = Math.max(movedBox.minX, i);
                    double pMaxX = Math.min(movedBox.maxX, i + 1);
                    double pMinY = Math.max(movedBox.minY, j);
                    double pMaxY = Math.min(movedBox.maxY, j + 1);
                    double pMinZ = Math.max(movedBox.minZ, k);
                    double pMaxZ = Math.min(movedBox.maxZ, k + 1);

                    if (pMinX >= pMaxX || pMinY >= pMaxY || pMinZ >= pMaxZ) continue;

                    if (face == Direction.WEST && pMinX > movedBox.minX + 1e-4) continue;
                    if (face == Direction.EAST && pMaxX < movedBox.maxX - 1e-4) continue;
                    if (face == Direction.DOWN && pMinY > movedBox.minY + 1e-4) continue;
                    if (face == Direction.UP && pMaxY < movedBox.maxY - 1e-4) continue;
                    if (face == Direction.NORTH && pMinZ > movedBox.minZ + 1e-4) continue;
                    if (face == Direction.SOUTH && pMaxZ < movedBox.maxZ - 1e-4) continue;

                    AABB uvBox = new AABB(pMinX - i, pMinY - j, pMinZ - k, pMaxX - i, pMaxY - j, pMaxZ - k);
                    ModelUtil.mapBoxToUvs(uvBox, face, uvs);

                    double gMinX = pMinX - offsetVec.x;
                    double gMaxX = pMaxX - offsetVec.x;
                    double gMinY = pMinY - offsetVec.y;
                    double gMaxY = pMaxY - offsetVec.y;
                    double gMinZ = pMinZ - offsetVec.z;
                    double gMaxZ = pMaxZ - offsetVec.z;

                    Vector3f pCent = new Vector3f((float)(gMinX + gMaxX) / 2f, (float)(gMinY + gMaxY) / 2f, (float)(gMinZ + gMaxZ) / 2f);
                    Vector3f pRad = new Vector3f((float)(gMaxX - gMinX) / 2f, (float)(gMaxY - gMinY) / 2f, (float)(gMaxZ - gMinZ) / 2f);

                    MutableQuad quad = ModelUtil.createFace(face, pCent, pRad, uvs);
                    quad.texFromSprite(sprite);
                    quad.lighti(15, 15);
                    quad.render(pose, bb);
                }
            }
        }
    }
}
