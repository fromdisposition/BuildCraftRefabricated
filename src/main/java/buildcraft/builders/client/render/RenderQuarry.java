/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;

import buildcraft.builders.tile.TileQuarry;

public class RenderQuarry implements BlockEntityRenderer<TileQuarry, QuarryRenderState> {
    private static final double LED_INSET = 0.4 / 16.0;
    private static final double GREEN_OFFSET = 1.5 / 16.0;
    private static final double RED_OFFSET = 3.5 / 16.0;
    private static final double Y = 13.5 / 16.0;

    private static final RenderPartCube[] LED_GREEN = new RenderPartCube[4];
    private static final RenderPartCube[] LED_RED = new RenderPartCube[4];

    static {
        for (int i = 0; i < 4; i++) {
            Direction face = Direction.from2DDataValue(i);
            LED_GREEN[i] = new RenderPartCube();
            LED_RED[i] = new RenderPartCube();
            LedRenderUtil.setFacePosition(LED_GREEN[i], face, LED_INSET, GREEN_OFFSET, Y);
            LedRenderUtil.setFacePosition(LED_RED[i], face, LED_INSET, RED_OFFSET, Y);
        }
    }

    public RenderQuarry(BlockEntityRendererProvider.Context context) {
    }

    public static void init() {
    }

    @Override
    public QuarryRenderState createRenderState() {
        return new QuarryRenderState();
    }

    @Override
    public void submit(QuarryRenderState renderState, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        net.minecraft.world.phys.Vec3 camPos = cameraState.pos;
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
        if (!(be instanceof TileQuarry tile)) return;

        BlockState state = level.getBlockState(pos);
        Direction front = state.hasProperty(HorizontalDirectionalBlock.FACING)
                ? state.getValue(HorizontalDirectionalBlock.FACING)
                : Direction.NORTH;
        Direction rear = front.getOpposite();

        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();
        renderLEDs(tile, rear, poseStack, bufferSource);
        bufferSource.endBatch();

        poseStack.popPose();
    }

    private void renderLEDs(TileQuarry tile, Direction rear,
                            PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        boolean hasPower = tile.hasPower();
        boolean hasTask = tile.isMining();

        boolean greenOn = hasPower || !hasTask;
        boolean redOn = !hasPower || !hasTask;

        int greenColour = greenOn ? LedRenderUtil.COLOUR_GREEN_ON : LedRenderUtil.COLOUR_OFF;
        int redColour = redOn ? LedRenderUtil.COLOUR_RED_ON : LedRenderUtil.COLOUR_OFF;

        VertexConsumer consumer = bufferSource.getBuffer(BCLibRenderTypes.led());
        PoseStack.Pose pose = poseStack.last();

        for (int i = 0; i < 4; i++) {
            Direction dir = Direction.from2DDataValue(i);
            if (dir == rear) continue;

            LED_GREEN[i].center.colouri(greenColour);
            LED_RED[i].center.colouri(redColour);

            Direction skipFace = dir.getOpposite();
            LED_GREEN[i].render(pose, consumer, skipFace);
            LED_RED[i].render(pose, consumer, skipFace);
        }
    }
}
