/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileMiningWell;

public class RenderMiningWell implements BlockEntityRenderer<TileMiningWell, MiningWellRenderState> {

    private static final int[] COLOUR_POWER = new int[16];

    private static final double LED_INSET = 0.2 / 16.0;
    private static final double POWER_OFFSET = 2.5 / 16.0;
    private static final double STATUS_OFFSET = 4.5 / 16.0;
    private static final double Y = 5.5 / 16.0;

    private static final RenderPartCube LED_POWER = new RenderPartCube();
    private static final RenderPartCube LED_STATUS = new RenderPartCube();

    private static final LaserType TUBE_LASER;

    static {
        for (int i = 0; i < COLOUR_POWER.length; i++) {
            int c = ((i * 0x40) / COLOUR_POWER.length) & 0xFF;
            int r = (((i * 0xB0) / COLOUR_POWER.length) & 0xFF) + 0x4F;
            COLOUR_POWER[i] = (0xFF << 24) | (c << 16) | (c << 8) | r;
        }

        SpriteHolder spriteTubeMiddle = SpriteHolderRegistry.getHolder("buildcraftfactory:block/mining_well/tube");
        LaserRow cap = new LaserRow(spriteTubeMiddle, 0, 8, 8, 16);
        LaserRow middle = new LaserRow(spriteTubeMiddle, 0, 0, 16, 8);

        LaserRow[] middles = { middle };

        TUBE_LASER = new LaserType(cap, middle, middles, null, cap);
    }

    public RenderMiningWell(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public MiningWellRenderState createRenderState() {
        return new MiningWellRenderState();
    }

    @Override
    public boolean shouldRender(TileMiningWell tile, Vec3 cameraPos) {

        return true;
    }

    @Override
    public void submit(MiningWellRenderState renderState, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        Vec3 camPos = cameraState.pos;
        if (camPos == null) return;

        org.joml.Vector3f t = new org.joml.Vector3f();
        poseStack.last().pose().getTranslation(t);
        BlockPos pos = new BlockPos(
                Math.round((float) (camPos.x + t.x)),
                Math.round((float) (camPos.y + t.y)),
                Math.round((float) (camPos.z + t.z)));

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TileMiningWell tile)) return;

        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();
        renderLEDs(tile, pos, level, poseStack, bufferSource);
        bufferSource.endBatch();

        poseStack.popPose();
    }

    private void renderLEDs(TileMiningWell tile, BlockPos pos, Level level,
                            PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        BlockState state = level.getBlockState(pos);
        Direction facing = state.is(BCFactoryBlocks.MINING_WELL)
                ? state.getValue(BuildCraftProperties.BLOCK_FACING)
                : Direction.NORTH;

        float percentFilled = tile.getPercentFilledForRender();
        int powerColour = COLOUR_POWER[(int) (percentFilled * (COLOUR_POWER.length - 1))];

        boolean complete = tile.isComplete();
        int statusColour = complete ? LedRenderUtil.COLOUR_OFF : LedRenderUtil.COLOUR_GREEN_ON;

        LedRenderUtil.setFacePosition(LED_POWER, facing, LED_INSET, POWER_OFFSET, Y);
        LedRenderUtil.setFacePosition(LED_STATUS, facing, LED_INSET, STATUS_OFFSET, Y);
        LED_POWER.center.colouri(powerColour);
        LED_STATUS.center.colouri(statusColour);

        VertexConsumer consumer = bufferSource.getBuffer(BCLibRenderTypes.led());
        PoseStack.Pose pose = poseStack.last();

        Direction skipFace = facing.getOpposite();
        LED_POWER.render(pose, consumer, skipFace);
        LED_STATUS.render(pose, consumer, skipFace);
    }
}
