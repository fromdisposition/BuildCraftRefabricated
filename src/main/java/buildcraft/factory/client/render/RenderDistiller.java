/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import net.minecraft.client.renderer.rendertype.RenderType;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.client.fluid.BcFluidQuadEmitter;
import buildcraft.lib.misc.FluidUtilBC;

@SuppressWarnings("deprecation")
public class RenderDistiller implements BlockEntityRenderer<TileDistiller_BC8, DistillerRenderState> {

    private static final Map<Direction, TankSizes> TANK_SIZES = new EnumMap<>(Direction.class);

    private static final Identifier[] POWER_TEXTURES = {
        Identifier.parse("buildcraftfactory:block/distiller/power_sprite_a"),
        Identifier.parse("buildcraftfactory:block/distiller/power_sprite_a"),
        Identifier.parse("buildcraftfactory:block/distiller/power_sprite_b"),
        Identifier.parse("buildcraftfactory:block/distiller/power_sprite_b"),
        Identifier.parse("buildcraftfactory:block/distiller/power_sprite_c"),
        Identifier.parse("buildcraftfactory:block/distiller/power_sprite_c"),
        Identifier.parse("buildcraftfactory:block/distiller/power_sprite_d"),
    };

    private static final boolean[] POWER_TOP_HALF = {
        true,
        false,
        true,
        false,
        true,
        false,
        true,
    };

    static {

        TankSizes sizes = new TankSizes(
            new TankBounds(0, 0, 4, 8, 16, 12),
            new TankBounds(8, 8, 0, 16, 16, 16),
            new TankBounds(8, 0, 0, 16, 8, 16),

            new PowerCubeBounds(0, 12, 8, 4, 4),
            new PowerCubeBounds(0, 0, 8, 4, 4)
        );
        Direction face = Direction.WEST;
        for (int i = 0; i < 4; i++) {
            TANK_SIZES.put(face, sizes);
            face = face.getClockWise();
            sizes = sizes.rotateY();
        }
    }

    public RenderDistiller(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public DistillerRenderState createRenderState() {
        return new DistillerRenderState();
    }

    @Override
    public void submit(DistillerRenderState renderState, PoseStack poseStack,
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
        if (!(be instanceof TileDistiller_BC8 tile)) return;

        BlockState state = level.getBlockState(pos);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        TankSizes sizes = TANK_SIZES.get(facing);
        if (sizes == null) return;

        int light = buildcraft.lib.client.render.LightUtil.getLightCoords(level, pos);

        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        renderSmoothedFluid(tile.getSmoothIn(), sizes.tankIn, poseStack, bufferSource, light, partialTicks);
        renderSmoothedFluid(tile.getSmoothGasOut(), sizes.tankGasOut, poseStack, bufferSource, light, partialTicks);
        renderSmoothedFluid(tile.getSmoothLiquidOut(), sizes.tankLiquidOut, poseStack, bufferSource, light, partialTicks);

        renderPowerCubes(tile, sizes, poseStack, bufferSource, light, partialTicks);

        poseStack.popPose();
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

    private static void renderPowerCubes(TileDistiller_BC8 tile, TankSizes sizes,
                                          PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                          int light, float partialTicks) {

        double prevAnim = tile.getPrevAnimState();
        double curAnim = tile.getAnimState();
        double animState = prevAnim + (curAnim - prevAnim) * partialTicks;
        long powerAvg = tile.getPowerAvgClient();

        double stMod1 = animState - Math.floor(animState);
        float y1 = (float) (1.0 - Math.abs(stMod1 - 0.5) * 2.0);

        double st2 = animState <= 0.5 ? 0 : animState - 0.5;
        double st2Mod1 = st2 - Math.floor(st2);
        float y2 = (float) (1.0 - Math.abs(st2Mod1 - 0.5) * 2.0);

        int texIndex;
        if (powerAvg <= 0) {
            texIndex = 0;
        } else {
            texIndex = (int) (powerAvg * 6 / TileDistiller_BC8.MAX_MJ_PER_TICK);
            texIndex = Math.max(1, Math.min(texIndex, 6));
        }

        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(POWER_TEXTURES[texIndex]);
        boolean topHalf = POWER_TOP_HALF[texIndex];

        float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f;

        VertexConsumer buffer = bufferSource.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        int overlay = OverlayTexture.NO_OVERLAY;

        renderPowerCube(pose, buffer, sprite, topHalf, sizes.powerRight, y1, r, g, b, a, light, overlay);

        renderPowerCube(pose, buffer, sprite, topHalf, sizes.powerLeft, y2, r, g, b, a, light, overlay);
    }

    private static void renderPowerCube(PoseStack.Pose pose, VertexConsumer buffer,
                                         TextureAtlasSprite sprite, boolean topHalf,
                                         PowerCubeBounds pcb, float yFraction,
                                         float r, float g, float b, float a, int light, int overlay) {

        float cubeMinY = (yFraction * 12.0f) / 16.0f;
        float cubeMaxY = cubeMinY + 4.0f / 16.0f;

        float minX = pcb.minX / 16.0f;
        float maxX = (pcb.minX + pcb.sizeX) / 16.0f;
        float minZ = pcb.minZ / 16.0f;
        float maxZ = (pcb.minZ + pcb.sizeZ) / 16.0f;

        float sprU0 = sprite.getU0();
        float sprU1 = sprite.getU1();
        float sprV0 = sprite.getV0();
        float sprV1 = sprite.getV1();
        float sprURange = sprU1 - sprU0;
        float sprVRange = sprV1 - sprV0;

        float vBase = topHalf ? 0 : 0.5f;

        float sideV0 = sprV0 + sprVRange * (vBase + 8.0f / 32.0f);
        float sideV1 = sprV0 + sprVRange * (vBase + 16.0f / 32.0f);

        float nsU0, nsU1;
        if (pcb.sizeX >= 8) {

            nsU0 = sprU0 + sprURange * (4.0f / 16.0f);
            nsU1 = sprU0 + sprURange * (12.0f / 16.0f);
        } else {

            nsU0 = sprU0 + sprURange * (0.0f / 16.0f);
            nsU1 = sprU0 + sprURange * (4.0f / 16.0f);
        }

        float ewU0, ewU1;
        if (pcb.sizeZ >= 8) {

            ewU0 = sprU0 + sprURange * (4.0f / 16.0f);
            ewU1 = sprU0 + sprURange * (12.0f / 16.0f);
        } else {

            ewU0 = sprU0 + sprURange * (0.0f / 16.0f);
            ewU1 = sprU0 + sprURange * (4.0f / 16.0f);
        }

        float udU0 = sprU0 + sprURange * (4.0f / 16.0f);
        float udU1 = sprU0 + sprURange * (12.0f / 16.0f);
        float udV0 = sprV0 + sprVRange * (vBase + 0.0f / 32.0f);
        float udV1 = sprV0 + sprVRange * (vBase + 8.0f / 32.0f);

        quadUV(pose, buffer, minX, cubeMaxY, minZ, maxX, cubeMaxY, minZ,
                maxX, cubeMinY, minZ, minX, cubeMinY, minZ,
                0, 0, -1, r, g, b, a, light, overlay,
                nsU0, sideV0, nsU1, sideV0, nsU1, sideV1, nsU0, sideV1);

        quadUV(pose, buffer, minX, cubeMinY, maxZ, maxX, cubeMinY, maxZ,
                maxX, cubeMaxY, maxZ, minX, cubeMaxY, maxZ,
                0, 0, 1, r, g, b, a, light, overlay,
                nsU0, sideV1, nsU1, sideV1, nsU1, sideV0, nsU0, sideV0);

        quadUV(pose, buffer, minX, cubeMinY, minZ, minX, cubeMinY, maxZ,
                minX, cubeMaxY, maxZ, minX, cubeMaxY, minZ,
                -1, 0, 0, r, g, b, a, light, overlay,
                ewU0, sideV1, ewU1, sideV1, ewU1, sideV0, ewU0, sideV0);

        quadUV(pose, buffer, maxX, cubeMaxY, minZ, maxX, cubeMaxY, maxZ,
                maxX, cubeMinY, maxZ, maxX, cubeMinY, minZ,
                1, 0, 0, r, g, b, a, light, overlay,
                ewU0, sideV0, ewU1, sideV0, ewU1, sideV1, ewU0, sideV1);

        boolean rotated = pcb.sizeX < pcb.sizeZ;

        if (!rotated) {
            quadUV(pose, buffer, minX, cubeMaxY, maxZ, maxX, cubeMaxY, maxZ,
                    maxX, cubeMaxY, minZ, minX, cubeMaxY, minZ,
                    0, 1, 0, r, g, b, a, light, overlay,
                    udU0, udV1, udU1, udV1, udU1, udV0, udU0, udV0);
        } else {

            quadUV(pose, buffer, minX, cubeMaxY, maxZ, maxX, cubeMaxY, maxZ,
                    maxX, cubeMaxY, minZ, minX, cubeMaxY, minZ,
                    0, 1, 0, r, g, b, a, light, overlay,
                    udU0, udV0, udU0, udV1, udU1, udV1, udU1, udV0);
        }

        if (!rotated) {
            quadUV(pose, buffer, minX, cubeMinY, minZ, maxX, cubeMinY, minZ,
                    maxX, cubeMinY, maxZ, minX, cubeMinY, maxZ,
                    0, -1, 0, r, g, b, a, light, overlay,
                    udU0, udV0, udU1, udV0, udU1, udV1, udU0, udV1);
        } else {
            quadUV(pose, buffer, minX, cubeMinY, minZ, maxX, cubeMinY, minZ,
                    maxX, cubeMinY, maxZ, minX, cubeMinY, maxZ,
                    0, -1, 0, r, g, b, a, light, overlay,
                    udU1, udV0, udU1, udV1, udU0, udV1, udU0, udV0);
        }
    }

    private static void quadUV(PoseStack.Pose pose, VertexConsumer builder,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            float nx, float ny, float nz,
            float r, float g, float b, float a, int light, int overlay,
            float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4) {
        builder.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        builder.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u2, v2).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        builder.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u3, v3).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        builder.addVertex(pose, x4, y4, z4).setColor(r, g, b, a).setUv(u4, v4).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
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

    static class PowerCubeBounds {
        final float minX, minZ, sizeX, sizeY, sizeZ;

        PowerCubeBounds(float minX, float minZ, float sizeX, float sizeY, float sizeZ) {
            this.minX = minX;
            this.minZ = minZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        PowerCubeBounds rotateY() {

            float newMinX = 16 - minZ - sizeZ;
            float newMinZ = minX;
            return new PowerCubeBounds(newMinX, newMinZ, sizeZ, sizeY, sizeX);
        }
    }

    static class TankSizes {
        final TankBounds tankIn, tankGasOut, tankLiquidOut;
        final PowerCubeBounds powerRight, powerLeft;

        TankSizes(TankBounds tankIn, TankBounds tankGasOut, TankBounds tankLiquidOut,
                  PowerCubeBounds powerRight, PowerCubeBounds powerLeft) {
            this.tankIn = tankIn;
            this.tankGasOut = tankGasOut;
            this.tankLiquidOut = tankLiquidOut;
            this.powerRight = powerRight;
            this.powerLeft = powerLeft;
        }

        TankSizes rotateY() {
            return new TankSizes(tankIn.rotateY(), tankGasOut.rotateY(), tankLiquidOut.rotateY(),
                    powerRight.rotateY(), powerLeft.rotateY());
        }
    }
}
