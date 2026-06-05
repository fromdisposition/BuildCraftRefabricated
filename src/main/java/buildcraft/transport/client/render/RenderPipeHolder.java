/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import java.util.List;
import java.util.Random;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.TravellingItem;
import buildcraft.transport.tile.TilePipeHolder;

import org.jspecify.annotations.Nullable;

public class RenderPipeHolder implements BlockEntityRenderer<TilePipeHolder, PipeHolderRenderState> {

    private static final int PIPE_PAINT_ALPHA = 76;

    private final ItemModelResolver itemModelResolver;

    public RenderPipeHolder(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public PipeHolderRenderState createRenderState() {
        return new PipeHolderRenderState();
    }

    public void extractRenderState(TilePipeHolder blockEntity, PipeHolderRenderState renderState,
                                    float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
        renderState.pipe = blockEntity;
        renderState.partialTick = partialTick;

        renderState.itemEntries.clear();
        Pipe pipe = blockEntity.getPipe();
        if (pipe != null && pipe.flow instanceof PipeFlowItems flowItems) {
            Level world = blockEntity.getLevel();
            if (world != null) {
                long now = world.getGameTime();
                int posHash = (int) blockEntity.getBlockPos().asLong();
                List<TravellingItem> items = flowItems.getAllItemsForRender();

                for (int i = 0; i < items.size(); i++) {
                    TravellingItem item = items.get(i);
                    ItemStack stack = item.clientItemLink.get();
                    if (stack == null || stack.isEmpty()) {
                        stack = item.getStack();
                    }
                    if (stack == null || stack.isEmpty()) continue;

                    ItemStackRenderState itemState = new ItemStackRenderState();

                    this.itemModelResolver.updateForTopItem(
                        itemState, stack, ItemDisplayContext.NONE,
                        world, null, posHash + i);

                    if (itemState.isEmpty()) continue;

                    Vec3 pos = item.getRenderPosition(BlockPos.ZERO, now, partialTick, flowItems);
                    Direction dir = item.getRenderDirection(now, partialTick);
                    int count = item.stackSize > 0 ? item.stackSize : stack.getCount();

                    renderState.itemEntries.add(new PipeHolderRenderState.ItemRenderEntry(
                        itemState, pos.x, pos.y, pos.z, dir, item.colour, count));
                }
            }
        }
    }

    @Override
    public void submit(PipeHolderRenderState renderState, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        TilePipeHolder pipe = renderState.pipe;
        if (pipe == null) return;
        Level level = pipe.getLevel();
        if (level == null) return;

        int light = buildcraft.lib.client.render.LightUtil.getLightCoords(level, pipe.getBlockPos());

        poseStack.pushPose();

        MultiBufferSource.BufferSource bodyBuffers = Minecraft.getInstance().renderBuffers().bufferSource();
        ModelPipe.renderDirect(pipe, poseStack.last(),
                bodyBuffers.getBuffer(BCLibRenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS)), light);
        ModelPipe.renderCutoutPluggables(pipe, poseStack.last(),
                bodyBuffers.getBuffer(BCLibRenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS)), light);
        Pipe bodyPipe = pipe.getPipe();
        if (bodyPipe != null && bodyPipe.getColour() != null) {
            ModelPipe.renderMaskOverlay(pipe, poseStack.last(),
                    bodyBuffers.getBuffer(BCLibRenderTypes.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS)),
                    light, PIPE_PAINT_ALPHA);
        }

        PipeWireRenderer.renderWires(pipe, poseStack.last(), light);

        submitItems(renderState, poseStack, collector, light);

        ItemRenderUtil.beginItemBatch(poseStack, collector, light);
        renderContents(pipe, 0, 0, 0, renderState.partialTick, poseStack);
        ItemRenderUtil.endItemBatch();

        poseStack.popPose();
    }

    private static void submitItems(PipeHolderRenderState renderState, PoseStack poseStack,
                                     SubmitNodeCollector collector, int light) {
        if (renderState.itemEntries.isEmpty()) return;

        Random modelOffsetRandom = new Random(0);

        VertexConsumer colourBuffer = null;
        MultiBufferSource.BufferSource colourBufferSource = null;
        boolean needsColourFlush = false;

        for (PipeHolderRenderState.ItemRenderEntry entry : renderState.itemEntries) {
            if (entry.renderState.isEmpty()) continue;

            Direction dir = entry.direction != null ? entry.direction : Direction.EAST;
            int itemModelCount = getStackModelCount(entry.stackCount);

            if (itemModelCount > 1) {
                setupModelOffsetRandom(modelOffsetRandom, entry.stackCount);
            }

            for (int i = 0; i < itemModelCount; i++) {
                poseStack.pushPose();

                float dx = 0, dy = 0, dz = 0;
                if (i > 0) {
                    dx = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                    dy = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                    dz = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                }

                poseStack.translate(entry.posX + dx, entry.posY + dy, entry.posZ + dz);

                poseStack.scale(0.30f, 0.30f, 0.30f);

                applyDirectionRotation(poseStack, dir);

                entry.renderState.submit(poseStack, collector,
                    light, OverlayTexture.NO_OVERLAY, 0);

                poseStack.popPose();
            }

            if (entry.colour != null) {
                if (colourBuffer == null) {
                    colourBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                    colourBuffer = colourBufferSource.getBuffer(Sheets.cutoutBlockSheet());
                    needsColourFlush = true;
                }
                renderColourOverlay(poseStack, colourBuffer, entry);
            }
        }

        if (needsColourFlush && colourBufferSource != null) {
            colourBufferSource.endBatch(Sheets.cutoutBlockSheet());
        }
    }

    private static void renderColourOverlay(PoseStack poseStack, VertexConsumer buffer,
                                             PipeHolderRenderState.ItemRenderEntry entry) {
        MutableQuad[] colourQuads = PipeItemColourQuads.get();
        if (colourQuads == null) return;

        int col = buildcraft.lib.misc.ColourUtil.getLightHex(entry.colour);
        int r = (col >> 16) & 0xFF;
        int g = (col >> 8) & 0xFF;
        int b = col & 0xFF;

        poseStack.pushPose();
        poseStack.translate(entry.posX, entry.posY, entry.posZ);
        for (MutableQuad q : colourQuads) {
            if (q == null) continue;
            MutableQuad q2 = new MutableQuad(q);
            q2.lighti(15, 15);
            q2.multColouri(r, g, b, 255);
            q2.render(poseStack.last(), buffer);
        }
        poseStack.popPose();
    }

    private static void applyDirectionRotation(PoseStack ps, Direction dir) {
        switch (dir) {
            case NORTH -> ps.mulPose(Axis.YP.rotationDegrees(180));
            case EAST -> ps.mulPose(Axis.YP.rotationDegrees(90));
            case WEST -> ps.mulPose(Axis.YP.rotationDegrees(-90));
            case UP -> ps.mulPose(Axis.XP.rotationDegrees(-90));
            case DOWN -> ps.mulPose(Axis.XP.rotationDegrees(90));
            case SOUTH -> {}
        }
    }

    private static void setupModelOffsetRandom(Random random, int stackCount) {
        random.setSeed(stackCount & 0x7F_FF_FF_FFL);
    }

    private static int getStackModelCount(int stackCount) {
        if (stackCount > 48) return 5;
        if (stackCount > 32) return 4;
        if (stackCount > 16) return 3;
        if (stackCount > 1) return 2;
        return 1;
    }

    private static void renderContents(TilePipeHolder pipe, double x, double y, double z,
        float partialTicks, PoseStack poseStack) {
        Pipe p = pipe.getPipe();
        if (p == null) {
            return;
        }

        if (p.flow != null && !(p.flow instanceof PipeFlowItems)) {
            renderFlow(p.flow, x, y, z, partialTicks, poseStack.last());
        }
        if (p.behaviour != null) {
            renderBehaviour(p.behaviour, x, y, z, partialTicks, poseStack.last());
        }
        VertexConsumer plugBuffer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(net.minecraft.client.renderer.Sheets.cutoutBlockSheet());
        for (Direction facing : Direction.values()) {
            PipePluggable plug = pipe.getPluggable(facing);
            if (plug != null) {
                renderPluggable(plug, x, y, z, partialTicks, plugBuffer, poseStack);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <P extends PipePluggable> void renderPluggable(P plug, double x, double y, double z,
        float partialTicks, VertexConsumer plugBuffer, PoseStack poseStack) {
        buildcraft.api.transport.pluggable.IPlugDynamicRenderer<P> renderer = PipeRegistryClient.getPlugRenderer(plug);
        if (renderer != null) {
            renderer.render(plug, x, y, z, partialTicks, plugBuffer, poseStack);
        }
    }

    @SuppressWarnings("unchecked")
    private static <F extends PipeFlow> void renderFlow(F flow, double x, double y, double z,
        float partialTicks, PoseStack.Pose pose) {
        IPipeFlowRenderer<F> renderer = PipeRegistryClient.getFlowRenderer(flow);
        if (renderer != null) {
            renderer.render(flow, x, y, z, partialTicks, null, pose);
        }
    }

    @SuppressWarnings("unchecked")
    private static <B extends PipeBehaviour> void renderBehaviour(B behaviour, double x, double y, double z,
        float partialTicks, PoseStack.Pose pose) {
        IPipeBehaviourRenderer<B> renderer = PipeRegistryClient.getBehaviourRenderer(behaviour);
        if (renderer != null) {
            renderer.render(behaviour, x, y, z, partialTicks, null, pose);
        }
    }
}
