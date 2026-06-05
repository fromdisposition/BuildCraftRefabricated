/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.misc.FluidUtilBC;

import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;

@SuppressWarnings("deprecation")
public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
    INSTANCE;

    private static final ThreadLocal<double[]> SCRATCH_AMOUNTS =
        ThreadLocal.withInitial(() -> new double[7]);
    private static final ThreadLocal<double[]> SCRATCH_OFF_X =
        ThreadLocal.withInitial(() -> new double[7]);
    private static final ThreadLocal<double[]> SCRATCH_OFF_Y =
        ThreadLocal.withInitial(() -> new double[7]);
    private static final ThreadLocal<double[]> SCRATCH_OFF_Z =
        ThreadLocal.withInitial(() -> new double[7]);

    private static final ThreadLocal<Vector3f> TL_POS =
        ThreadLocal.withInitial(Vector3f::new);
    private static final ThreadLocal<Vector3f> TL_NORM =
        ThreadLocal.withInitial(Vector3f::new);

    @Override
    public void render(PipeFlowFluids flow, double x, double y, double z,
                        float partialTicks, VertexConsumer bb, PoseStack.Pose pose) {
        FluidStack forRender = flow.getFluidStackForRender();
        if (forRender == null || forRender.isEmpty()) {
            return;
        }

        ensureRenderCache(flow, forRender);
        Identifier stillTexture = flow.renderCacheSpriteId;
        if (stillTexture == null) return;
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(stillTexture);

        int tR = flow.renderCacheTintR;
        int tG = flow.renderCacheTintG;
        int tB = flow.renderCacheTintB;
        int tA = flow.renderCacheTintA;

        MultiBufferSource.BufferSource bufferSource =
            Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer fluidBB = bufferSource.getBuffer(
            flow.renderCacheTranslucent
                ? BCLibRenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)
                : BCLibRenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS));

        double[] amounts = SCRATCH_AMOUNTS.get();
        double[] offX = SCRATCH_OFF_X.get();
        double[] offY = SCRATCH_OFF_Y.get();
        double[] offZ = SCRATCH_OFF_Z.get();
        flow.writeAmountsForRender(partialTicks, amounts);
        flow.writeOffsetsForRender(partialTicks, offX, offY, offZ);

        boolean gas = buildcraft.lib.misc.FluidUtilBC.isGaseous(forRender.getFluid());
        boolean horizontal = false;
        boolean vertical = flow.pipe.isConnected(gas ? Direction.DOWN : Direction.UP);

        for (Direction face : Direction.values()) {
            double size = ((Pipe) flow.pipe).getConnectedDist(face);
            int fi = face.ordinal();
            double amount = amounts[fi];
            if (face.getAxis() != Axis.Y) {
                horizontal |= flow.pipe.isConnected(face) && amount > 0;
            }
            if (amount <= 0) continue;

            double centerShift = 0.245 + size / 2;
            double cx = 0.5 + face.getStepX() * centerShift;
            double cy = 0.5 + face.getStepY() * centerShift;
            double cz = 0.5 + face.getStepZ() * centerShift;

            double rx = 0.24, ry = 0.24, rz = 0.24;
            double faceAxisRadius = 0.005 + size / 2;
            switch (face.getAxis()) {
                case X -> rx = faceAxisRadius;
                case Y -> ry = faceAxisRadius;
                case Z -> rz = faceAxisRadius;
            }
            if (face.getAxis() == Axis.Y) {
                double perc = Math.sqrt(amount / flow.capacity);
                rx = perc * 0.24;
                rz = perc * 0.24;
            }

            double minX = cx - rx, minY = cy - ry, minZ = cz - rz;
            double maxX = cx + rx, maxY = cy + ry, maxZ = cz + rz;
            double ox = offX[fi], oy = offY[fi], oz = offZ[fi];

            double cuboidAmount;
            double cuboidCapacity;
            if (face.getAxis() == Axis.Y) {

                cuboidAmount = 1;
                cuboidCapacity = 1;
            } else {
                cuboidAmount = amount;
                cuboidCapacity = flow.capacity;
            }

            int faceSkipMask = 0;
            int centerIdx = EnumPipePart.CENTER.getIndex();
            if (amounts[centerIdx] > 0) {
                faceSkipMask = 1 << face.getOpposite().ordinal();
            }
            renderFluidCuboid(
                flow,
                minX, minY, minZ, maxX, maxY, maxZ,
                minX + ox, minY + oy, minZ + oz,
                maxX + ox, maxY + oy, maxZ + oz,
                cuboidAmount, cuboidCapacity, gas,
                sprite, tR, tG, tB, tA, faceSkipMask, fluidBB, pose);
        }

        int ci = EnumPipePart.CENTER.getIndex();
        double centerAmount = amounts[ci];
        double cox = offX[ci];
        double coy = offY[ci];
        double coz = offZ[ci];

        boolean renderedHorizCenter = false;
        double horizPos = 0.26;
        if (horizontal || !vertical) {
            double minX = 0.26, minY = 0.26, minZ = 0.26;
            double maxX = 0.74, maxY = 0.74, maxZ = 0.74;
            renderFluidCuboid(
                flow,
                minX, minY, minZ, maxX, maxY, maxZ,
                minX + cox, minY + coy, minZ + coz,
                maxX + cox, maxY + coy, maxZ + coz,
                centerAmount, flow.capacity, gas,
                sprite, tR, tG, tB, tA, 0, fluidBB, pose);
            horizPos += (maxY - minY) * centerAmount / flow.capacity;
            renderedHorizCenter = true;
        }

        if (vertical && horizPos < 0.74) {
            double perc = Math.sqrt(centerAmount / flow.capacity);
            double minXZ = 0.5 - 0.24 * perc;
            double maxXZ = 0.5 + 0.24 * perc;
            double yMin = gas ? 0.26 : horizPos;
            double yMax = gas ? 1 - horizPos : 0.74;

            int pillarSkipMask = 0;
            if (renderedHorizCenter) {
                pillarSkipMask = 1 << (gas ? Direction.UP.ordinal() : Direction.DOWN.ordinal());
            }
            renderFluidCuboid(
                flow,
                minXZ, yMin, minXZ, maxXZ, yMax, maxXZ,
                minXZ + cox, yMin + coy, minXZ + coz,
                maxXZ + cox, yMax + coy, maxXZ + coz,
                1, 1, gas,
                sprite, tR, tG, tB, tA, pillarSkipMask, fluidBB, pose);
        }
    }

    private static void ensureRenderCache(PipeFlowFluids flow, FluidStack fluidStack) {
        Fluid current = fluidStack.getFluid();
        if (current == flow.renderCacheFluid) return;
        flow.renderCacheFluid = current;
        BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(current);
        if (entry != null) {
            flow.renderCacheSpriteId = FluidUtilBC.appearanceForPipe(entry).texture();
            flow.renderCacheBcGradient = true;
            flow.renderCacheTexLight = entry.texLight();
            flow.renderCacheTexDark = entry.texDark();
            flow.renderCacheHeat = entry.heat();
            BcFluidTintUtil.ensureClientTemplatesLoaded();
        } else {
            flow.renderCacheSpriteId = FluidUtilBC.getFluidTexture(fluidStack);
            flow.renderCacheBcGradient = false;
        }
        int color = FluidUtilBC.getFluidColor(fluidStack);
        flow.renderCacheTintR = (color >> 16) & 0xFF;
        flow.renderCacheTintG = (color >> 8) & 0xFF;
        flow.renderCacheTintB = color & 0xFF;
        int a = (color >> 24) & 0xFF;
        flow.renderCacheTintA = a == 0 ? 0xFF : a;
        flow.renderCacheTranslucent = FluidUtilBC.shouldRenderTranslucent(fluidStack);
    }

    private static void renderFluidCuboid(
            PipeFlowFluids flow,
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ,
            double uvMinX, double uvMinY, double uvMinZ,
            double uvMaxX, double uvMaxY, double uvMaxZ,
            double amount, double capacity, boolean gas,
            TextureAtlasSprite sprite,
            int tR, int tG, int tB, int tA,
            int skipFaceMask,
            VertexConsumer bb, PoseStack.Pose pose) {
        if (amount <= 0 || capacity <= 0) return;

        double height = Math.min(amount / capacity, 1.0);
        double realMinX, realMinY, realMinZ, realMaxX, realMaxY, realMaxZ;
        double realUvMinX, realUvMinY, realUvMinZ, realUvMaxX, realUvMaxY, realUvMaxZ;
        if (gas) {
            realMinX = minX; realMinZ = minZ;
            realMinY = maxY - (maxY - minY) * height;
            realMaxX = maxX; realMaxY = maxY; realMaxZ = maxZ;
            realUvMinX = uvMinX; realUvMinZ = uvMinZ;
            realUvMinY = uvMaxY - (uvMaxY - uvMinY) * height;
            realUvMaxX = uvMaxX; realUvMaxY = uvMaxY; realUvMaxZ = uvMaxZ;
        } else {
            realMinX = minX; realMinY = minY; realMinZ = minZ;
            realMaxX = maxX; realMaxZ = maxZ;
            realMaxY = minY + (maxY - minY) * height;
            realUvMinX = uvMinX; realUvMinY = uvMinY; realUvMinZ = uvMinZ;
            realUvMaxX = uvMaxX; realUvMaxZ = uvMaxZ;
            realUvMaxY = uvMinY + (uvMaxY - uvMinY) * height;
        }

        double offX = realUvMinX - realMinX;
        double offY = realUvMinY - realMinY;
        double offZ = realUvMinZ - realMinZ;

        double mvMinX = realMinX + offX;
        double mvMinY = realMinY + offY;
        double mvMinZ = realMinZ + offZ;
        double mvMaxX = realMaxX + offX;
        double mvMaxY = realMaxY + offY;
        double mvMaxZ = realMaxZ + offZ;

        int fMinX = (int) Math.floor(mvMinX);
        int fMaxX = (int) Math.floor(mvMaxX);
        int fMinY = (int) Math.floor(mvMinY);
        int fMaxY = (int) Math.floor(mvMaxY);
        int fMinZ = (int) Math.floor(mvMinZ);
        int fMaxZ = (int) Math.floor(mvMaxZ);

        for (Direction face : Direction.values()) {
            if ((skipFaceMask & (1 << face.ordinal())) != 0) continue;
            for (int i = fMinX; i <= fMaxX; i++) {
                for (int j = fMinY; j <= fMaxY; j++) {
                    for (int k = fMinZ; k <= fMaxZ; k++) {

                        double pMinX = Math.max(mvMinX, i);
                        double pMaxX = Math.min(mvMaxX, i + 1);
                        double pMinY = Math.max(mvMinY, j);
                        double pMaxY = Math.min(mvMaxY, j + 1);
                        double pMinZ = Math.max(mvMinZ, k);
                        double pMaxZ = Math.min(mvMaxZ, k + 1);

                        if (pMinX >= pMaxX || pMinY >= pMaxY || pMinZ >= pMaxZ) continue;

                        if (face == Direction.WEST  && pMinX > mvMinX + 1e-4) continue;
                        if (face == Direction.EAST  && pMaxX < mvMaxX - 1e-4) continue;
                        if (face == Direction.DOWN  && pMinY > mvMinY + 1e-4) continue;
                        if (face == Direction.UP    && pMaxY < mvMaxY - 1e-4) continue;
                        if (face == Direction.NORTH && pMinZ > mvMinZ + 1e-4) continue;
                        if (face == Direction.SOUTH && pMaxZ < mvMaxZ - 1e-4) continue;

                        float uvBoxMinX = (float)(pMinX - i);
                        float uvBoxMinY = (float)(pMinY - j);
                        float uvBoxMinZ = (float)(pMinZ - k);
                        float uvBoxMaxX = (float)(pMaxX - i);
                        float uvBoxMaxY = (float)(pMaxY - j);
                        float uvBoxMaxZ = (float)(pMaxZ - k);

                        float uMin, uMax, vMin, vMax;
                        switch (face) {
                            case WEST  -> { uMin = uvBoxMinZ;        uMax = uvBoxMaxZ;        vMin = 1f - uvBoxMaxY; vMax = 1f - uvBoxMinY; }
                            case EAST  -> { uMin = 1f - uvBoxMinZ;   uMax = 1f - uvBoxMaxZ;   vMin = 1f - uvBoxMaxY; vMax = 1f - uvBoxMinY; }
                            case DOWN  -> { uMin = uvBoxMinX;        uMax = uvBoxMaxX;        vMin = 1f - uvBoxMaxZ; vMax = 1f - uvBoxMinZ; }
                            case UP    -> { uMin = uvBoxMinX;        uMax = uvBoxMaxX;        vMin = uvBoxMaxZ;      vMax = uvBoxMinZ;      }
                            case NORTH -> { uMin = 1f - uvBoxMinX;   uMax = 1f - uvBoxMaxX;   vMin = 1f - uvBoxMaxY; vMax = 1f - uvBoxMinY; }
                            case SOUTH -> { uMin = uvBoxMinX;        uMax = uvBoxMaxX;        vMin = 1f - uvBoxMaxY; vMax = 1f - uvBoxMinY; }
                            default    -> { continue; }
                        }

                        float gMinX = (float)(pMinX - offX);
                        float gMaxX = (float)(pMaxX - offX);
                        float gMinY = (float)(pMinY - offY);
                        float gMaxY = (float)(pMaxY - offY);
                        float gMinZ = (float)(pMinZ - offZ);
                        float gMaxZ = (float)(pMaxZ - offZ);

                        if (gMaxX <= gMinX || gMaxY <= gMinY || gMaxZ <= gMinZ) continue;

                        float sUMin = sprite.getU(uMin);
                        float sUMax = sprite.getU(uMax);
                        float sVMin = sprite.getV(vMin);
                        float sVMax = sprite.getV(vMax);

                        emitFace(flow, sprite, face,
                            gMinX, gMinY, gMinZ, gMaxX, gMaxY, gMaxZ,
                            sUMin, sVMin, sUMax, sVMax,
                            tR, tG, tB, tA, bb, pose);
                    }
                }
            }
        }
    }

    static void computeFaceVertices(Direction face,
                                      float gMinX, float gMinY, float gMinZ,
                                      float gMaxX, float gMaxY, float gMaxZ,
                                      float uMin, float vMin, float uMax, float vMax,
                                      float[] out) {
        switch (face) {
            case UP -> {

                set(out, 0, gMaxX, gMaxY, gMaxZ, uMax, vMin);
                set(out, 1, gMaxX, gMaxY, gMinZ, uMax, vMax);
                set(out, 2, gMinX, gMaxY, gMinZ, uMin, vMax);
                set(out, 3, gMinX, gMaxY, gMaxZ, uMin, vMin);
            }
            case DOWN -> {

                set(out, 0, gMinX, gMinY, gMaxZ, uMin, vMin);
                set(out, 1, gMinX, gMinY, gMinZ, uMin, vMax);
                set(out, 2, gMaxX, gMinY, gMinZ, uMax, vMax);
                set(out, 3, gMaxX, gMinY, gMaxZ, uMax, vMin);
            }
            case NORTH -> {

                set(out, 0, gMaxX, gMaxY, gMinZ, uMax, vMin);
                set(out, 1, gMaxX, gMinY, gMinZ, uMax, vMax);
                set(out, 2, gMinX, gMinY, gMinZ, uMin, vMax);
                set(out, 3, gMinX, gMaxY, gMinZ, uMin, vMin);
            }
            case SOUTH -> {

                set(out, 0, gMinX, gMaxY, gMaxZ, uMin, vMin);
                set(out, 1, gMinX, gMinY, gMaxZ, uMin, vMax);
                set(out, 2, gMaxX, gMinY, gMaxZ, uMax, vMax);
                set(out, 3, gMaxX, gMaxY, gMaxZ, uMax, vMin);
            }
            case WEST -> {

                set(out, 0, gMinX, gMaxY, gMinZ, uMin, vMin);
                set(out, 1, gMinX, gMinY, gMinZ, uMin, vMax);
                set(out, 2, gMinX, gMinY, gMaxZ, uMax, vMax);
                set(out, 3, gMinX, gMaxY, gMaxZ, uMax, vMin);
            }
            case EAST -> {

                set(out, 0, gMaxX, gMaxY, gMaxZ, uMax, vMin);
                set(out, 1, gMaxX, gMinY, gMaxZ, uMax, vMax);
                set(out, 2, gMaxX, gMinY, gMinZ, uMin, vMax);
                set(out, 3, gMaxX, gMaxY, gMinZ, uMin, vMin);
            }
        }
    }

    private static void set(float[] out, int v, float x, float y, float z, float u, float vUv) {
        int base = v * 5;
        out[base] = x;
        out[base + 1] = y;
        out[base + 2] = z;
        out[base + 3] = u;
        out[base + 4] = vUv;
    }

    private static final ThreadLocal<float[]> TL_FACE_VERTS =
        ThreadLocal.withInitial(() -> new float[20]);

    private static final int FACE_SUBDIVISIONS = buildcraft.lib.client.fluid.BcFluidQuadEmitter.SUBDIVISIONS;

    private static void emitFace(PipeFlowFluids flow, TextureAtlasSprite sprite, Direction face,
                                  float gMinX, float gMinY, float gMinZ,
                                  float gMaxX, float gMaxY, float gMaxZ,
                                  float uMin, float vMin, float uMax, float vMax,
                                  int tR, int tG, int tB, int tA,
                                  VertexConsumer bb, PoseStack.Pose pose) {
        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();
        if (!flow.renderCacheBcGradient) {
            float[] verts = TL_FACE_VERTS.get();
            computeFaceVertices(face, gMinX, gMinY, gMinZ, gMaxX, gMaxY, gMaxZ,
                    uMin, vMin, uMax, vMax, verts);
            for (int v = 0; v < 4; v++) {
                int base = v * 5;
                emitVertex(bb, pose,
                        verts[base], verts[base + 1], verts[base + 2],
                        verts[base + 3], verts[base + 4],
                        nx, ny, nz, tR, tG, tB, tA);
            }
            return;
        }
        float[] corners = TL_FACE_VERTS.get();
        computeFaceVertices(face, gMinX, gMinY, gMinZ, gMaxX, gMaxY, gMaxZ,
                uMin, vMin, uMax, vMax, corners);
        int n = FACE_SUBDIVISIONS;
        for (int i = 0; i < n; i++) {
            float fu = i / (float) n;
            float fu1 = (i + 1) / (float) n;
            for (int j = 0; j < n; j++) {
                float fv = j / (float) n;
                float fv1 = (j + 1) / (float) n;
                emitSubFaceVertex(flow, sprite, bb, pose, corners, fu, fv, nx, ny, nz);
                emitSubFaceVertex(flow, sprite, bb, pose, corners, fu1, fv, nx, ny, nz);
                emitSubFaceVertex(flow, sprite, bb, pose, corners, fu1, fv1, nx, ny, nz);
                emitSubFaceVertex(flow, sprite, bb, pose, corners, fu, fv1, nx, ny, nz);
            }
        }
    }

    private static void emitSubFaceVertex(
            PipeFlowFluids flow, TextureAtlasSprite sprite, VertexConsumer bb, PoseStack.Pose pose,
            float[] corners, float u, float v,
            float nx, float ny, float nz) {
        float x = bilinearCorner(corners, 0, u, v);
        float y = bilinearCorner(corners, 1, u, v);
        float z = bilinearCorner(corners, 2, u, v);
        float tu = bilinearCorner(corners, 3, u, v);
        float tv = bilinearCorner(corners, 4, u, v);
        float nu = BcFluidTintUtil.normalizedU(tu, sprite.getU0(), sprite.getU1());
        float nv = BcFluidTintUtil.normalizedV(tv, sprite.getV0(), sprite.getV1());
        int packed = BcFluidTintUtil.vertexColorFromTemplate(
                flow.renderCacheTexLight,
                flow.renderCacheTexDark,
                flow.renderCacheHeat,
                nu, nv);
        int vr = (packed >> 16) & 0xFF;
        int vg = (packed >> 8) & 0xFF;
        int vb = packed & 0xFF;
        int va = (packed >> 24) & 0xFF;
        if (va == 0) {
            va = 0xFF;
        }
        emitVertex(bb, pose, x, y, z, tu, tv, nx, ny, nz, vr, vg, vb, va);
    }

    private static float bilinearCorner(float[] corners, int channel, float u, float v) {
        int base = channel;
        float c0 = corners[base];
        float c1 = corners[5 + base];
        float c2 = corners[10 + base];
        float c3 = corners[15 + base];
        float top = c0 + (c1 - c0) * u;
        float bot = c3 + (c2 - c3) * u;
        return top + (bot - top) * v;
    }

    private static void emitVertex(VertexConsumer bb, PoseStack.Pose pose,
                                    float x, float y, float z,
                                    float u, float v,
                                    float nx, float ny, float nz,
                                    int tR, int tG, int tB, int tA) {
        Vector3f pos = TL_POS.get();
        pos.set(x, y, z);
        pose.pose().transformPosition(pos);
        Vector3f norm = TL_NORM.get();
        norm.set(nx, ny, nz);
        pose.normal().transform(norm);
        bb.addVertex(pos.x, pos.y, pos.z)
          .setColor(tR, tG, tB, tA)
          .setUv(u, v)
          .setOverlay(OverlayTexture.NO_OVERLAY)
          .setUv2(15 << 4, 15 << 4)
          .setNormal(norm.x, norm.y, norm.z);
    }
}
