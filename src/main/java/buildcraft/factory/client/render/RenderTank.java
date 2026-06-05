/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

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
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import net.minecraft.client.renderer.rendertype.RenderType;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.fluid.BcFluidQuadEmitter;
import buildcraft.lib.misc.FluidUtilBC;

@SuppressWarnings("deprecation")
public class RenderTank implements BlockEntityRenderer<TileTank, TankRenderState> {

    private static final float MIN_XZ = 2.0f / 16.0f + 0.01f;
    private static final float MAX_XZ = 14.0f / 16.0f - 0.01f;
    private static final float MIN_Y = 0.01f;
    private static final float MAX_Y = 1.0f - 0.01f;
    private static final float MIN_Y_CONNECTED = 0.0f;
    private static final float MAX_Y_CONNECTED = 1.0f - 1e-5f;

    public RenderTank(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public TankRenderState createRenderState() {
        return new TankRenderState();
    }

    @Override
    public void submit(TankRenderState renderState, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        ProfilerFiller _profiler = Profiler.get();
        _profiler.push("buildcraft:tank_submit");
        try {
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
        if (!(be instanceof TileTank tile)) return;

        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        buildcraft.lib.fluid.FluidSmoother.FluidStackInterp interp = tile.smoothedTank.getFluidForRender(partialTicks);
        if (interp == null) return;

        FluidStack fluid = interp.fluid();
        double amount = interp.amount();
        int capacity = tile.smoothedTank.getCapacity();
        if (amount <= 0 || capacity <= 0) return;

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

        boolean connectedDown = isConnectedFluid(tile, Direction.DOWN);
        boolean connectedUp = isConnectedFluid(tile, Direction.UP);

        float minY = connectedDown ? MIN_Y_CONNECTED : MIN_Y;
        float maxYFull = connectedUp ? MAX_Y_CONNECTED : MAX_Y;
        float fillRatio = (float) (amount / capacity);

        boolean gaseous = FluidUtilBC.isGaseous(fluid);
        float fluidTop, fluidBottom;
        if (gaseous) {

            fluidTop = maxYFull;
            fluidBottom = maxYFull - (maxYFull - minY) * fillRatio;
        } else {

            fluidBottom = minY;
            fluidTop = minY + (maxYFull - minY) * fillRatio;
        }

        int light = buildcraft.lib.client.render.LightUtil.getLightCoords(level, pos);
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        VertexConsumer buffer = bufferSource.getBuffer(
                FluidUtilBC.shouldRenderTranslucent(fluid)
                    ? net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS) : net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();

        boolean renderBottom = !connectedDown;
        boolean renderTop = !connectedUp || fillRatio < 1.0f;

        BcFluidQuadEmitter.emitTankQuad(pose, buffer, sprite, MIN_XZ, fluidTop, MIN_XZ, MAX_XZ, fluidTop, MIN_XZ,
                MAX_XZ, fluidBottom, MIN_XZ, MIN_XZ, fluidBottom, MIN_XZ,
                0, 0, -1, r, g, b, a, light, overlay);

        BcFluidQuadEmitter.emitTankQuad(pose, buffer, sprite, MIN_XZ, fluidBottom, MAX_XZ, MAX_XZ, fluidBottom, MAX_XZ,
                MAX_XZ, fluidTop, MAX_XZ, MIN_XZ, fluidTop, MAX_XZ,
                0, 0, 1, r, g, b, a, light, overlay);

        BcFluidQuadEmitter.emitTankQuad(pose, buffer, sprite, MIN_XZ, fluidBottom, MIN_XZ, MIN_XZ, fluidBottom, MAX_XZ,
                MIN_XZ, fluidTop, MAX_XZ, MIN_XZ, fluidTop, MIN_XZ,
                -1, 0, 0, r, g, b, a, light, overlay);

        BcFluidQuadEmitter.emitTankQuad(pose, buffer, sprite, MAX_XZ, fluidTop, MIN_XZ, MAX_XZ, fluidTop, MAX_XZ,
                MAX_XZ, fluidBottom, MAX_XZ, MAX_XZ, fluidBottom, MIN_XZ,
                1, 0, 0, r, g, b, a, light, overlay);

        if (renderTop) {
            BcFluidQuadEmitter.emitTankHorizontal(pose, buffer, sprite, MIN_XZ, MAX_XZ, MAX_XZ, MIN_XZ, fluidTop,
                    0, 1, 0, r, g, b, a, light, overlay);
        }
        if (renderBottom) {
            BcFluidQuadEmitter.emitTankHorizontal(pose, buffer, sprite, MIN_XZ, MAX_XZ, MAX_XZ, MIN_XZ, fluidBottom,
                    0, -1, 0, r, g, b, a, light, overlay);
        }

        if (gaseous && fillRatio < 1.0f && !connectedDown) {
            BcFluidQuadEmitter.emitTankHorizontal(pose, buffer, sprite, MIN_XZ, MAX_XZ, MAX_XZ, MIN_XZ, fluidBottom,
                    0, -1, 0, r, g, b, a, light, overlay);
        }

        poseStack.popPose();
        } finally {
            _profiler.pop();
        }
    }

    private static boolean isConnectedFluid(TileTank tile, Direction direction) {
        if (tile.getLevel() == null) return false;
        BlockPos neighborPos = tile.getBlockPos().relative(direction);
        BlockEntity neighbor = tile.getLevel().getBlockEntity(neighborPos);
        if (neighbor instanceof TileTank otherTank) {
            if (!TileTank.canTanksConnect(tile, otherTank, direction)) return false;
            buildcraft.lib.transfer.fluid.FluidResource otherFluid = otherTank.tank.getResource(0);
            buildcraft.lib.transfer.fluid.FluidResource thisFluid = tile.tank.getResource(0);
            if (otherFluid.isEmpty() || thisFluid.isEmpty()) return false;
            if (!FluidUtilBC.areEquivalentFluidResources(thisFluid, otherFluid)) return false;

            Direction checkDir = FluidUtilBC.isGaseous(thisFluid.toStack(1)) ? direction.getOpposite() : direction;
            return otherTank.tank.getAmountAsLong(0) >= otherTank.tank.getCapacityAsLong(0, buildcraft.lib.transfer.fluid.FluidResource.EMPTY)
                    || checkDir == Direction.UP;
        }
        return false;
    }

}
