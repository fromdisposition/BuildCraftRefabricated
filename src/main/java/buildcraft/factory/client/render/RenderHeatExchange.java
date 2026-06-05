/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.minecraft.client.renderer.rendertype.RenderType;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileHeatExchange.EnumProgressState;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionEnd;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionStart;
import buildcraft.lib.client.fluid.BcFluidQuadEmitter;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.misc.FluidUtilBC;

@SuppressWarnings("deprecation")
public class RenderHeatExchange implements BlockEntityRenderer<TileHeatExchange, HeatExchangeRenderState> {

    private static final Map<Direction, TankSideData> TANK_SIDES = new EnumMap<>(Direction.class);
    private static final TankBounds TANK_BOTTOM, TANK_TOP;

    static {
        TANK_BOTTOM = new TankBounds(2, 0, 2, 14, 2, 14);
        TANK_TOP = new TankBounds(2, 14, 2, 14, 16, 14);
        TankBounds start = new TankBounds(0, 4, 4, 2, 12, 12);
        TankBounds end = new TankBounds(14, 4, 4, 16, 12, 12);
        TankSideData sides = new TankSideData(start, end);
        Direction face = Direction.EAST;
        for (int i = 0; i < 4; i++) {
            TANK_SIDES.put(face, sides);
            face = face.getClockWise();
            sides = sides.rotateY();
        }
    }

    public RenderHeatExchange(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public HeatExchangeRenderState createRenderState() {
        return new HeatExchangeRenderState();
    }

    @Override
    public void submit(HeatExchangeRenderState renderState, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        Vec3 camPos = cameraState.pos;
        if (camPos == null) return;
        org.joml.Vector3f t = new org.joml.Vector3f();
        poseStack.last().pose().getTranslation(t);
        BlockPos pos = new BlockPos(
                Math.round((float)(camPos.x + t.x)),
                Math.round((float)(camPos.y + t.y)),
                Math.round((float)(camPos.z + t.z)));

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TileHeatExchange tile)) return;

        if (!tile.isStart()) return;

        ExchangeSectionStart section = (ExchangeSectionStart) tile.getSection();
        if (section == null) return;
        ExchangeSectionEnd sectionEnd = section.getEndSection();

        if (sectionEnd == null) {
            BlockState st = tile.getBlockState();
            if (st.getBlock() instanceof BlockHeatExchange) {
                Direction dir = st.getValue(BlockHeatExchange.FACING).getCounterClockWise();
                for (int i = 1; i < 6; i++) {
                    BlockEntity neighbor = level.getBlockEntity(pos.relative(dir, i));
                    if (neighbor instanceof TileHeatExchange other && other.isEnd()) {
                        sectionEnd = (ExchangeSectionEnd) other.getSection();
                        tile.markCheckNeighbours();
                        break;
                    }
                    if (!(neighbor instanceof TileHeatExchange)) break;
                }
            }
        }

        BlockState state = tile.getBlockState();
        if (!(state.getBlock() instanceof BlockHeatExchange)) return;

        Direction facing = state.getValue(BlockHeatExchange.FACING);
        Direction face = facing.getCounterClockWise();
        TankSideData sideTank = TANK_SIDES.get(face);
        if (sideTank == null) return;

        int light = buildcraft.lib.client.render.LightUtil.getLightCoords(level, pos);

        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        renderSmoothedFluid(section.smoothedTankInput, TANK_BOTTOM, poseStack, bufferSource, light, partialTicks);
        renderSmoothedFluid(section.smoothedTankOutput, sideTank.start, poseStack, bufferSource, light, partialTicks);

        if (sectionEnd != null) {
            BlockPos diff = sectionEnd.getTile().getBlockPos().subtract(tile.getBlockPos());
            poseStack.translate(diff.getX(), diff.getY(), diff.getZ());
            renderSmoothedFluid(sectionEnd.smoothedTankOutput, TANK_TOP, poseStack, bufferSource, light, partialTicks);
            renderSmoothedFluid(sectionEnd.smoothedTankInput, sideTank.end, poseStack, bufferSource, light, partialTicks);
            poseStack.translate(-diff.getX(), -diff.getY(), -diff.getZ());
        }

        int middles = section.middleCount;
        if (middles > 0 && sectionEnd != null) {
            EnumProgressState progressState = section.getProgressState();
            double progress = section.getProgress(partialTicks);
            if (progress > 0) {
                double length = middles + 2 - 4 / 16.0 - 0.02;
                double p0 = 2 / 16.0 + 0.01;
                double p1 = p0 + length - 0.01;
                double progressStart = p0;
                double progressEnd = p0 + length * progress;

                boolean flip = progressState == EnumProgressState.PREPARING;
                flip ^= face.getAxisDirection() == AxisDirection.NEGATIVE;

                if (flip) {
                    progressStart = p1 - length * progress;
                    progressEnd = p1;
                }
                BlockPos diff = BlockPos.ZERO;
                if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
                    diff = diff.relative(face, middles + 1);
                }
                double otherStart = flip ? p0 : p1 - length * progress;
                double otherEnd = flip ? p0 + length * progress : p1;
                Vec3 vDiff = Vec3.atLowerCornerOf(diff);

                FluidStack coolantFluid = sectionEnd.smoothedTankInput.getFluid();
                FluidStack heatantFluid = section.smoothedTankInput.getFluid();

                if (!coolantFluid.isEmpty()) {
                    renderFlow(vDiff, face, poseStack, bufferSource, progressStart + 0.01, progressEnd - 0.01,
                            coolantFluid, 4, partialTicks, light);
                }

                if (!heatantFluid.isEmpty()) {
                    renderFlow(vDiff, face.getOpposite(), poseStack, bufferSource, otherStart, otherEnd,
                            heatantFluid, 2, partialTicks, light);
                }
            }
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(TileHeatExchange blockEntity, Vec3 cameraPos) {
        return blockEntity.isStart();
    }

    private static void renderSmoothedFluid(FluidSmoother smoother, TankBounds bounds, PoseStack poseStack,
                                             MultiBufferSource.BufferSource bufferSource, int light, float partialTicks) {
        FluidSmoother.FluidStackInterp interp = smoother.getFluidForRender(partialTicks);
        if (interp == null || interp.amount() <= 0) return;

        FluidStack fluid = interp.fluid();
        int capacity = smoother.getCapacity();
        if (capacity <= 0) return;

        Identifier stillTexture = FluidUtilBC.getFluidTexture(fluid);
        if (stillTexture == null) return;

        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(stillTexture);

        float[] rgba = FluidUtilBC.vertexRgba(fluid);
        float r = rgba[0];
        float g = rgba[1];
        float b = rgba[2];
        float a = rgba[3];

        float fillRatio = (float) (interp.amount() / capacity);

        float shrink = 1.0f / 64.0f;
        float minX = bounds.minX / 16.0f + shrink;
        float minY = bounds.minY / 16.0f + shrink;
        float minZ = bounds.minZ / 16.0f + shrink;
        float maxX = bounds.maxX / 16.0f - shrink;
        float maxY = bounds.maxY / 16.0f - shrink;
        float maxZ = bounds.maxZ / 16.0f - shrink;

        boolean gaseous = FluidUtilBC.isGaseous(fluid);
        float fluidTop, fluidBottom;
        if (gaseous) {
            fluidTop = maxY;
            fluidBottom = maxY - (maxY - minY) * fillRatio;
        } else {
            fluidBottom = minY;
            fluidTop = minY + (maxY - minY) * fillRatio;
        }

        VertexConsumer buffer = bufferSource.getBuffer(
                FluidUtilBC.shouldRenderTranslucent(fluid)
                    ? net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS) : net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        int overlay = OverlayTexture.NO_OVERLAY;

        BcFluidQuadEmitter.emitTankQuad(pose, buffer, sprite, minX, fluidTop, minZ, maxX, fluidTop, minZ,
                maxX, fluidBottom, minZ, minX, fluidBottom, minZ,
                0, 0, -1, r, g, b, a, light, overlay);

        BcFluidQuadEmitter.emitTankQuad(pose, buffer, sprite, minX, fluidBottom, maxZ, maxX, fluidBottom, maxZ,
                maxX, fluidTop, maxZ, minX, fluidTop, maxZ,
                0, 0, 1, r, g, b, a, light, overlay);

        BcFluidQuadEmitter.emitTankQuad(pose, buffer, sprite, minX, fluidBottom, minZ, minX, fluidBottom, maxZ,
                minX, fluidTop, maxZ, minX, fluidTop, minZ,
                -1, 0, 0, r, g, b, a, light, overlay);

        BcFluidQuadEmitter.emitTankQuad(pose, buffer, sprite, maxX, fluidTop, minZ, maxX, fluidTop, maxZ,
                maxX, fluidBottom, maxZ, maxX, fluidBottom, minZ,
                1, 0, 0, r, g, b, a, light, overlay);

        BcFluidQuadEmitter.emitTankHorizontal(pose, buffer, sprite, minX, maxX, maxZ, minZ, fluidTop,
                0, 1, 0, r, g, b, a, light, overlay);

        BcFluidQuadEmitter.emitTankHorizontal(pose, buffer, sprite, minX, maxX, maxZ, minZ, fluidBottom,
                0, -1, 0, r, g, b, a, light, overlay);
    }

    private static void renderFlow(Vec3 diff, Direction face, PoseStack poseStack,
                                    MultiBufferSource.BufferSource bufferSource,
                                    double s, double e, FluidStack fluid,
                                    int point, float partialTicks, int light) {
        if (fluid.isEmpty()) return;

        Identifier stillTexture = FluidUtilBC.getFluidTexture(fluid);
        if (stillTexture == null) return;

        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(stillTexture);

        float[] rgba = FluidUtilBC.vertexRgba(fluid);
        float r = rgba[0];
        float g = rgba[1];
        float b = rgba[2];
        float a = rgba[3];

        VertexConsumer buffer = bufferSource.getBuffer(
                FluidUtilBC.shouldRenderTranslucent(fluid)
                    ? net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS) : net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
        int overlay = OverlayTexture.NO_OVERLAY;

        Level level = Minecraft.getInstance().level;
        double tickTime = level != null ? level.getGameTime() : 0;
        double offset = (tickTime + partialTicks) % 31 / 31.0;

        Direction renderFace = face;
        if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
            offset = -offset;
            renderFace = face.getOpposite();
        }
        Vec3 dirVec = Vec3.atLowerCornerOf(renderFace.getUnitVec3i());
        double ds = (point + 0.1) / 16.0;
        float minCross = (float) ds;
        float maxCross = (float) (1 - ds);

        diff = diff.subtract(dirVec.scale(offset));
        s += offset;
        e += offset;
        if (s < 0) {
            s++;
            e++;
            diff = diff.subtract(dirVec);
        }

        for (int i = 0; i <= e; i++) {
            if (i < s - 1) {
                diff = diff.add(dirVec);
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(diff.x, diff.y, diff.z);
            PoseStack.Pose pose = poseStack.last();
            diff = diff.add(dirVec);

            double s1 = s < i ? 0 : (s % 1);
            double e1 = e > i + 1 ? 1 : (e % 1);

            float flowMinX = minCross, flowMaxX = maxCross;
            float flowMinY = minCross, flowMaxY = maxCross;
            float flowMinZ = minCross, flowMaxZ = maxCross;

            switch (renderFace.getAxis()) {
                case X -> { flowMinX = (float) s1; flowMaxX = (float) e1; }
                case Y -> { flowMinY = (float) s1; flowMaxY = (float) e1; }
                case Z -> { flowMinZ = (float) s1; flowMaxZ = (float) e1; }
            }

            boolean[] sides = new boolean[6];
            Arrays.fill(sides, true);
            if (s < i) sides[renderFace.getOpposite().ordinal()] = false;
            if (e > i + 1) sides[renderFace.ordinal()] = false;

            if (sides[Direction.NORTH.ordinal()]) {
                quad(pose, buffer, sprite, flowMinX, flowMaxY, flowMinZ, flowMaxX, flowMaxY, flowMinZ,
                        flowMaxX, flowMinY, flowMinZ, flowMinX, flowMinY, flowMinZ,
                        0, 0, -1, r, g, b, a, light, overlay);
            }
            if (sides[Direction.SOUTH.ordinal()]) {
                quad(pose, buffer, sprite, flowMinX, flowMinY, flowMaxZ, flowMaxX, flowMinY, flowMaxZ,
                        flowMaxX, flowMaxY, flowMaxZ, flowMinX, flowMaxY, flowMaxZ,
                        0, 0, 1, r, g, b, a, light, overlay);
            }
            if (sides[Direction.WEST.ordinal()]) {
                quad(pose, buffer, sprite, flowMinX, flowMinY, flowMinZ, flowMinX, flowMinY, flowMaxZ,
                        flowMinX, flowMaxY, flowMaxZ, flowMinX, flowMaxY, flowMinZ,
                        -1, 0, 0, r, g, b, a, light, overlay);
            }
            if (sides[Direction.EAST.ordinal()]) {
                quad(pose, buffer, sprite, flowMaxX, flowMaxY, flowMinZ, flowMaxX, flowMaxY, flowMaxZ,
                        flowMaxX, flowMinY, flowMaxZ, flowMaxX, flowMinY, flowMinZ,
                        1, 0, 0, r, g, b, a, light, overlay);
            }
            if (sides[Direction.UP.ordinal()]) {
                quadHorizontal(pose, buffer, sprite, flowMinX, flowMaxX, flowMaxZ, flowMinZ, flowMaxY,
                        0, 1, 0, r, g, b, a, light, overlay);
            }
            if (sides[Direction.DOWN.ordinal()]) {
                quadHorizontal(pose, buffer, sprite, flowMinX, flowMaxX, flowMaxZ, flowMinZ, flowMinY,
                        0, -1, 0, r, g, b, a, light, overlay);
            }

            poseStack.popPose();
        }
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer builder, TextureAtlasSprite sprite,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            float nx, float ny, float nz,
            float r, float g, float b, float a, int light, int overlay) {
        BcFluidQuadEmitter.emitTankQuad(pose, builder, sprite,
                x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
                nx, ny, nz, r, g, b, a, light, overlay);
    }

    private static void quadHorizontal(PoseStack.Pose pose, VertexConsumer builder, TextureAtlasSprite sprite,
            float x1, float x2, float z1, float z2, float y,
            float nx, float ny, float nz,
            float r, float g, float b, float a, int light, int overlay) {
        BcFluidQuadEmitter.emitTankHorizontal(pose, builder, sprite,
                x1, x2, z1, z2, y, nx, ny, nz, r, g, b, a, light, overlay);
    }

    static class TankBounds {
        final float minX, minY, minZ, maxX, maxY, maxZ;

        TankBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        TankBounds rotateY() {
            float newMinX = 16 - maxZ;
            float newMinZ = minX;
            float newMaxX = 16 - minZ;
            float newMaxZ = maxX;
            return new TankBounds(
                Math.min(newMinX, newMaxX), minY, Math.min(newMinZ, newMaxZ),
                Math.max(newMinX, newMaxX), maxY, Math.max(newMinZ, newMaxZ)
            );
        }
    }

    static class TankSideData {
        final TankBounds start, end;

        TankSideData(TankBounds start, TankBounds end) {
            this.start = start;
            this.end = end;
        }

        TankSideData rotateY() {
            return new TankSideData(start.rotateY(), end.rotateY());
        }
    }
}
