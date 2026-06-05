/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.client.render.pip;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.key.PipeModelKey;

@SuppressWarnings("deprecation")
public class BlueprintPipRenderer extends PictureInPictureRenderer<BlueprintPipRenderState> {

    private static final Logger LOGGER = LogManager.getLogger("BCBlueprintPipRenderer");

    private static final Set<Integer> LOGGED_SNAPSHOTS =
            Collections.synchronizedSet(new HashSet<>());

    private static final float PITCH_DEG = 20.0f;

    private static final long YAW_PERIOD_MS = 3600L;

    private static final int FULL_BRIGHT = 15728880;

    private static final Identifier SCAN_TEXTURE =
            Identifier.parse("buildcraftbuilders:textures/block/scan.png");

    private static final int TEMPLATE_GHOST_ALPHA = 128;

    private static final int PIPE_PAINT_ALPHA = 76;

    private static final Vector3f GHOST_CENTER = new Vector3f(0.5f, 0.5f, 0.5f);
    private static final Vector3f GHOST_RADIUS = new Vector3f(0.5f, 0.5f, 0.5f);
    private static final ModelUtil.UvFaceData GHOST_UVS =
            new ModelUtil.UvFaceData(0f, 0f, 1f, 1f);

    private static final Vector3f LIGHT0_MODEL_SPACE = new Vector3f(1.0f, 1.0f, 1.0f).normalize();
    private static final Vector3f LIGHT1_MODEL_SPACE = new Vector3f(-1.0f, 1.0f, -1.0f).normalize();

    private GpuBuffer lightingBuffer;

    private int lightingBufferPaddedSize;

    public BlueprintPipRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    private void ensureLightingBufferAllocated() {
        if (lightingBuffer != null) return;
        GpuDevice device = RenderSystem.getDevice();
        lightingBufferPaddedSize = Mth.roundToward(Lighting.UBO_SIZE, device.getUniformOffsetAlignment());
        lightingBuffer = device.createBuffer(
                () -> "BCBlueprintPipLighting", 136, lightingBufferPaddedSize);
    }

    private void writeLightDirections(Vector3f light0, Vector3f light1) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer bb = Std140Builder.onStack(stack, Lighting.UBO_SIZE)
                    .putVec3(light0)
                    .putVec3(light1)
                    .get();
            RenderSystem.getDevice().createCommandEncoder()
                    .writeToBuffer(lightingBuffer.slice(0, lightingBufferPaddedSize), bb);
        }
    }

    @Override
    public Class<BlueprintPipRenderState> getRenderStateClass() {
        return BlueprintPipRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "buildcraft_blueprint_preview";
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0f;
    }

    @Override
    protected void renderToTexture(BlueprintPipRenderState renderState, PoseStack poseStack) {
        Snapshot snapshot = renderState.snapshot();
        BlockPos size = snapshot.size;
        int sizeX = Math.max(1, size.getX());
        int sizeY = Math.max(1, size.getY());
        int sizeZ = Math.max(1, size.getZ());

        poseStack.scale(1.0f, -1.0f, -1.0f);

        float yaw = (System.currentTimeMillis() % YAW_PERIOD_MS) / (float) YAW_PERIOD_MS * 360.0f;
        poseStack.mulPose(Axis.XP.rotationDegrees(PITCH_DEG));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(-sizeX / 2.0f, -sizeY / 2.0f, -sizeZ / 2.0f);

        Minecraft mc = Minecraft.getInstance();
        Vector3f light0Camera = poseStack.last().transformNormal(
                LIGHT0_MODEL_SPACE.x(), LIGHT0_MODEL_SPACE.y(), LIGHT0_MODEL_SPACE.z(),
                new Vector3f());
        Vector3f light1Camera = poseStack.last().transformNormal(
                LIGHT1_MODEL_SPACE.x(), LIGHT1_MODEL_SPACE.y(), LIGHT1_MODEL_SPACE.z(),
                new Vector3f());
        ensureLightingBufferAllocated();
        writeLightDirections(light0Camera, light1Camera);
        GpuBufferSlice savedShaderLights = RenderSystem.getShaderLights();
        RenderSystem.setShaderLights(lightingBuffer.slice(0, Lighting.UBO_SIZE));

        FeatureRenderDispatcher featureRenderDispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage submitNodeStorage = featureRenderDispatcher.getSubmitNodeStorage();

        Map<BlockState, TrackingItemStackRenderState> stateCache = new HashMap<>();

        int submitted = 0;
        int submittedFluid = 0;
        int submittedTemplate = 0;
        int submittedPipe = 0;
        int skippedNoItem = 0;
        int skippedAirOrEmpty = 0;
        String sampleClassName = "n/a";

        Blueprint blueprint = snapshot instanceof Blueprint bp ? bp : null;
        Template template = snapshot instanceof Template tp ? tp : null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    pos.set(x, y, z);
                    int dataIndex = Snapshot.posToIndex(size, pos);

                    if (template != null) {

                        if (template.data != null && template.data.get(dataIndex)) {
                            submitTemplateGhostCube(poseStack, template, size, x, y, z);
                            submittedTemplate++;
                        } else {
                            skippedAirOrEmpty++;
                        }
                        continue;
                    }

                    if (blueprint == null) {

                        skippedAirOrEmpty++;
                        continue;
                    }

                    int index = blueprint.data[dataIndex];
                    if (index < 0 || index >= blueprint.palette.size()) {
                        skippedAirOrEmpty++;
                        continue;
                    }
                    ISchematicBlock schBlock = blueprint.palette.get(index);
                    if (schBlock == null || schBlock.isAir()) {
                        skippedAirOrEmpty++;
                        continue;
                    }
                    BlockState state = schBlock.getBlockStateForRender();
                    if (state == null || state.isAir()) {
                        skippedAirOrEmpty++;
                        continue;
                    }
                    if (sampleClassName.equals("n/a")) {
                        sampleClassName = schBlock.getClass().getSimpleName();
                    }

                    if (PipePreviewModel.isPipe(state)) {
                        PipeModelKey pipeKey = PipePreviewModel.modelKey(schBlock.getTileNbtForRender());
                        if (pipeKey != null) {
                            poseStack.pushPose();
                            poseStack.translate(x, y, z);
                            PoseStack.Pose pipePose = poseStack.last();

                            ModelPipe.renderDirect(pipeKey, pipePose,
                                    this.bufferSource.getBuffer(
                                            BCLibRenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS)),
                                    FULL_BRIGHT);
                            ModelPipe.renderMaskOverlay(pipeKey, pipePose,
                                    this.bufferSource.getBuffer(
                                            BCLibRenderTypes.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS)),
                                    FULL_BRIGHT, PIPE_PAINT_ALPHA);
                            poseStack.popPose();
                            submittedPipe++;
                            continue;
                        }
                    }

                    FluidState fluidState = state.getFluidState();
                    if (!fluidState.isEmpty()) {
                        submitFluidCube(poseStack, blueprint, size, x, y, z, fluidState, FULL_BRIGHT);
                        submittedFluid++;
                        continue;
                    }

                    TrackingItemStackRenderState itemRenderState = stateCache.get(state);
                    if (itemRenderState == null) {
                        ItemStack stack = new ItemStack(state.getBlock());
                        if (stack.isEmpty()) {

                            stateCache.put(state, MARKER_EMPTY);
                            skippedNoItem++;
                            continue;
                        }
                        itemRenderState = new TrackingItemStackRenderState();
                        mc.getItemModelResolver().updateForTopItem(
                                itemRenderState, stack,

                                ItemDisplayContext.NONE,
                                mc.level, null, 0);
                        stateCache.put(state, itemRenderState);
                    } else if (itemRenderState == MARKER_EMPTY) {
                        skippedNoItem++;
                        continue;
                    }

                    poseStack.pushPose();

                    poseStack.translate(x + 0.5f, y + 0.5f, z + 0.5f);
                    itemRenderState.submit(poseStack, submitNodeStorage, FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                    submitted++;
                }
            }
        }

        featureRenderDispatcher.renderAllFeatures();

        this.bufferSource.endBatch();

        RenderSystem.setShaderLights(savedShaderLights);

        if (LOGGED_SNAPSHOTS.add(System.identityHashCode(snapshot))) {
            LOGGER.info("renderToTexture: type={} size={}x{}x{} submitted={} submittedFluid={} submittedTemplate={} submittedPipe={} skippedNoItem={} skippedAirOrEmpty={} sampleSchBlock={} distinctStates={}",
                    snapshot.getClass().getSimpleName(), sizeX, sizeY, sizeZ, submitted, submittedFluid,
                    submittedTemplate, submittedPipe, skippedNoItem, skippedAirOrEmpty, sampleClassName, stateCache.size());
        }
    }

    private void submitFluidCube(
            PoseStack poseStack, Blueprint blueprint, BlockPos size,
            int xCell, int yCell, int zCell,
            FluidState fluidState, int lightmap) {
        Fluid fluid = fluidState.getType();

        FluidStack stack = new FluidStack(fluid, 1);

        Identifier stillTexture = FluidUtilBC.getFluidTexture(stack);
        if (stillTexture == null) return;

        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(stillTexture);
        if (sprite == null) return;

        float[] rgba = FluidUtilBC.vertexRgba(stack);
        float r = rgba[0];
        float g = rgba[1];
        float b = rgba[2];
        float a = rgba[3];

        float h = fluidState.isSource() ? 1.0f : Math.max(0.125f, fluidState.getOwnHeight());

        boolean cullTop    = neighborIsSameFluid(blueprint, size, xCell,   yCell + 1, zCell,   fluid);
        boolean cullBottom = neighborIsSameFluid(blueprint, size, xCell,   yCell - 1, zCell,   fluid);
        boolean cullNorth  = neighborIsSameFluid(blueprint, size, xCell,   yCell,     zCell - 1, fluid);
        boolean cullSouth  = neighborIsSameFluid(blueprint, size, xCell,   yCell,     zCell + 1, fluid);
        boolean cullWest   = neighborIsSameFluid(blueprint, size, xCell - 1, yCell,   zCell,   fluid);
        boolean cullEast   = neighborIsSameFluid(blueprint, size, xCell + 1, yCell,   zCell,   fluid);

        VertexConsumer vc = this.bufferSource.getBuffer(
                FluidUtilBC.shouldRenderTranslucent(fluid)
                        ? BCLibRenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)
                        : BCLibRenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS));

        poseStack.pushPose();
        poseStack.translate(xCell, yCell, zCell);
        PoseStack.Pose pose = poseStack.last();
        int overlay = OverlayTexture.NO_OVERLAY;

        if (!cullTop) {
            buildcraft.lib.client.fluid.BcFluidQuadEmitter.emitQuadWithAtlasUv(
                    pose, vc, sprite, null,
                    0, h, 0, sprite.getU(0), sprite.getV(0),
                    0, h, 1, sprite.getU(0), sprite.getV(1),
                    1, h, 1, sprite.getU(1), sprite.getV(1),
                    1, h, 0, sprite.getU(1), sprite.getV(0),
                    0, 1, 0, r, g, b, a, lightmap, overlay);
        }

        if (!cullBottom) {
            buildcraft.lib.client.fluid.BcFluidQuadEmitter.emitQuadWithAtlasUv(
                    pose, vc, sprite, null,
                    0, 0, 0, sprite.getU(0), sprite.getV(0),
                    1, 0, 0, sprite.getU(1), sprite.getV(0),
                    1, 0, 1, sprite.getU(1), sprite.getV(1),
                    0, 0, 1, sprite.getU(0), sprite.getV(1),
                    0, -1, 0, r, g, b, a, lightmap, overlay);
        }

        if (!cullNorth) {
            buildcraft.lib.client.fluid.BcFluidQuadEmitter.emitQuadWithAtlasUv(
                    pose, vc, sprite, null,
                    0, 0, 0, sprite.getU(0), sprite.getV(1),
                    0, h, 0, sprite.getU(0), sprite.getV(1 - h),
                    1, h, 0, sprite.getU(1), sprite.getV(1 - h),
                    1, 0, 0, sprite.getU(1), sprite.getV(1),
                    0, 0, -1, r, g, b, a, lightmap, overlay);
        }

        if (!cullSouth) {
            buildcraft.lib.client.fluid.BcFluidQuadEmitter.emitQuadWithAtlasUv(
                    pose, vc, sprite, null,
                    1, 0, 1, sprite.getU(0), sprite.getV(1),
                    1, h, 1, sprite.getU(0), sprite.getV(1 - h),
                    0, h, 1, sprite.getU(1), sprite.getV(1 - h),
                    0, 0, 1, sprite.getU(1), sprite.getV(1),
                    0, 0, 1, r, g, b, a, lightmap, overlay);
        }

        if (!cullWest) {
            buildcraft.lib.client.fluid.BcFluidQuadEmitter.emitQuadWithAtlasUv(
                    pose, vc, sprite, null,
                    0, 0, 1, sprite.getU(0), sprite.getV(1),
                    0, h, 1, sprite.getU(0), sprite.getV(1 - h),
                    0, h, 0, sprite.getU(1), sprite.getV(1 - h),
                    0, 0, 0, sprite.getU(1), sprite.getV(1),
                    -1, 0, 0, r, g, b, a, lightmap, overlay);
        }

        if (!cullEast) {
            buildcraft.lib.client.fluid.BcFluidQuadEmitter.emitQuadWithAtlasUv(
                    pose, vc, sprite, null,
                    1, 0, 0, sprite.getU(0), sprite.getV(1),
                    1, h, 0, sprite.getU(0), sprite.getV(1 - h),
                    1, h, 1, sprite.getU(1), sprite.getV(1 - h),
                    1, 0, 1, sprite.getU(1), sprite.getV(1),
                    1, 0, 0, r, g, b, a, lightmap, overlay);
        }

        poseStack.popPose();
    }

    private static boolean neighborIsSameFluid(
            Blueprint blueprint, BlockPos size,
            int nx, int ny, int nz, Fluid fluid) {
        if (nx < 0 || ny < 0 || nz < 0
                || nx >= size.getX() || ny >= size.getY() || nz >= size.getZ()) {
            return false;
        }
        int idx = blueprint.data[Snapshot.posToIndex(size, new BlockPos(nx, ny, nz))];
        if (idx < 0 || idx >= blueprint.palette.size()) return false;
        ISchematicBlock schBlock = blueprint.palette.get(idx);
        if (schBlock == null) return false;
        BlockState nState = schBlock.getBlockStateForRender();
        if (nState == null) return false;
        FluidState nFluid = nState.getFluidState();

        return !nFluid.isEmpty() && nFluid.is(fluid);
    }

    private static void putFluidVertex(
            VertexConsumer vc, PoseStack.Pose pose,
            float x, float y, float z, float u, float v,
            float r, float g, float b, float a,
            float nx, float ny, float nz,
            int light, int overlay) {
        vc.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private void submitTemplateGhostCube(PoseStack poseStack, Template template, BlockPos size,
                                         int xCell, int yCell, int zCell) {

        EnumSet<Direction> faces = TemplateGhostGeometry.visibleFaces(template, size, xCell, yCell, zCell);
        if (faces.isEmpty()) {

            return;
        }
        VertexConsumer vc = this.bufferSource.getBuffer(BCLibRenderTypes.entityTranslucent(SCAN_TEXTURE));

        poseStack.pushPose();
        poseStack.translate(xCell, yCell, zCell);
        PoseStack.Pose pose = poseStack.last();
        for (Direction face : faces) {
            ModelUtil.createFace(face, GHOST_CENTER, GHOST_RADIUS, GHOST_UVS)
                    .lighti(15, 15)
                    .colouri(255, 255, 255, TEMPLATE_GHOST_ALPHA)
                    .render(pose, vc);
        }
        poseStack.popPose();
    }

    private static final TrackingItemStackRenderState MARKER_EMPTY = new TrackingItemStackRenderState();
}
