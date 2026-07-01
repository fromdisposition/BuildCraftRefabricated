/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render.pip;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import buildcraft.lib.client.fluid.BcFluidVertexEmitter;
import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.key.PipeModelKey;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
//?} else {
/*import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
*///?}
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

public class BlueprintPipRenderer extends PictureInPictureRenderer<BlueprintPipRenderState> {
   private static final Logger LOGGER = LogManager.getLogger("BCBlueprintPipRenderer");
   private static final Set<Integer> LOGGED_SNAPSHOTS = Collections.synchronizedSet(new HashSet<>());
   private static final float PITCH_DEG = 20.0F;
   private static final int YAW_PERIOD_TICKS = 72;
   private static final int FULL_BRIGHT = 15728880;
   private static final Identifier SCAN_TEXTURE = Identifier.parse("buildcraftbuilders:textures/block/scan.png");
   private static final int TEMPLATE_GHOST_ALPHA = 128;
   private static final Vector3f GHOST_CENTER = new Vector3f(0.5F, 0.5F, 0.5F);
   private static final Vector3f GHOST_RADIUS = new Vector3f(0.5F, 0.5F, 0.5F);
   private static final ModelUtil.UvFaceData GHOST_UVS = new ModelUtil.UvFaceData(0.0F, 0.0F, 1.0F, 1.0F);
   private static final Vector3f LIGHT0_MODEL_SPACE = new Vector3f(1.0F, 1.0F, 1.0F).normalize();
   private static final Vector3f LIGHT1_MODEL_SPACE = new Vector3f(-1.0F, 1.0F, -1.0F).normalize();
   private GpuBuffer lightingBuffer;
   private int lightingBufferPaddedSize;
   private final Map<BlueprintPipRenderer.PlanKey, BlueprintPipRenderer.PreviewPlan> planCache = new LinkedHashMap<BlueprintPipRenderer.PlanKey, BlueprintPipRenderer.PreviewPlan>(
      16, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<BlueprintPipRenderer.PlanKey, BlueprintPipRenderer.PreviewPlan> eldest) {
         return this.size() > 16;
      }
   };
   private static final ThreadLocal<MutableBlockPos> NEIGHBOR_SCRATCH = ThreadLocal.withInitial(MutableBlockPos::new);
   private static final TrackingItemStackRenderState MARKER_EMPTY = new TrackingItemStackRenderState();

   //? if >= 26.2 {
   public BlueprintPipRenderer() {
      super();
   }
   //?} else {
   /*public BlueprintPipRenderer(BufferSource bufferSource) {
      super(bufferSource);
   }
   *///?}

   //? if >= 26.2 {
   @Override
   protected void renderToTexture(BlueprintPipRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      SubmitNodeStorage storage = (SubmitNodeStorage) submitNodeCollector;
      Snapshot snapshot = renderState.snapshot();
      BlockPos size = snapshot.size;
      int sizeX = Math.max(1, size.getX());
      int sizeY = Math.max(1, size.getY());
      int sizeZ = Math.max(1, size.getZ());
      poseStack.scale(1.0F, -1.0F, -1.0F);
      Minecraft mc = Minecraft.getInstance();
      long gameTime = mc.level != null ? mc.level.getGameTime() : 0L;
      float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float yaw = ((float)(gameTime % 72L) + partialTick) / 72.0F * 360.0F;
      poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
      poseStack.translate(-sizeX / 2.0F, -sizeY / 2.0F, -sizeZ / 2.0F);
      Vector3f light0Camera = poseStack.last().transformNormal(LIGHT0_MODEL_SPACE.x(), LIGHT0_MODEL_SPACE.y(), LIGHT0_MODEL_SPACE.z(), new Vector3f());
      Vector3f light1Camera = poseStack.last().transformNormal(LIGHT1_MODEL_SPACE.x(), LIGHT1_MODEL_SPACE.y(), LIGHT1_MODEL_SPACE.z(), new Vector3f());
      this.ensureLightingBufferAllocated();
      this.writeLightDirections(light0Camera, light1Camera);
      GpuBufferSlice savedShaderLights = RenderSystem.getShaderLights();
      RenderSystem.setShaderLights(this.lightingBuffer.slice(0, (int) Lighting.UBO_SIZE));
      net.minecraft.client.renderer.feature.FeatureRenderDispatcher featureRenderDispatcher = mc.gameRenderer.featureRenderDispatcher();
      BlueprintPipRenderer.PreviewPlan plan = this.planFor(snapshot, mc);

      for (BlueprintPipRenderer.TemplateEntry entry : plan.templateEntries) {
         this.submitTemplateGhostCube26(poseStack, entry, storage);
      }

      for (BlueprintPipRenderer.PipeEntry entry : plan.pipeEntries) {
         this.submitPipeEntry26(poseStack, entry, storage);
      }

      for (BlueprintPipRenderer.FluidEntry entry : plan.fluidEntries) {
         this.submitFluidCube26(poseStack, entry, FULL_BRIGHT, storage);
      }

      for (BlueprintPipRenderer.ItemEntry entry : plan.itemEntries) {
         poseStack.pushPose();
         poseStack.translate(entry.x + 0.5F, entry.y + 0.5F, entry.z + 0.5F);
         entry.renderState.submit(poseStack, submitNodeCollector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
         poseStack.popPose();
      }

      featureRenderDispatcher.renderAllFeatures(storage);
      RenderSystem.setShaderLights(savedShaderLights);
      if (LOGGED_SNAPSHOTS.add(System.identityHashCode(snapshot))) {
         LOGGER.info(
            "renderToTexture: type={} size={}x{}x{} submitted={} submittedFluid={} submittedTemplate={} submittedPipe={} skippedNoItem={} skippedAirOrEmpty={} skippedHidden={} sampleSchBlock={} distinctStates={}",
            new Object[]{
               snapshot.getClass().getSimpleName(),
               sizeX, sizeY, sizeZ,
               plan.itemEntries.size(), plan.fluidEntries.size(),
               plan.templateEntries.size(), plan.pipeEntries.size(),
               plan.skippedNoItem, plan.skippedAirOrEmpty, plan.skippedHidden,
               plan.sampleClassName, plan.distinctStates
            }
         );
      }
   }

   private void submitTemplateGhostCube26(PoseStack poseStack, BlueprintPipRenderer.TemplateEntry entry, SubmitNodeStorage storage) {
      poseStack.pushPose();
      poseStack.translate(entry.x, entry.y, entry.z);
      com.mojang.blaze3d.vertex.PoseStack.Pose pose = poseStack.last();
      storage.submitCustomGeometry(poseStack, BCLibRenderTypes.entityTranslucent(SCAN_TEXTURE), (p, vc) -> {
         for (Direction face : entry.faces) {
            ModelUtil.createFace(face, GHOST_CENTER, GHOST_RADIUS, GHOST_UVS).lighti(15, 15).colouri(255, 255, 255, 128).render(pose, vc);
         }
      });
      poseStack.popPose();
   }

   private void submitPipeEntry26(PoseStack poseStack, BlueprintPipRenderer.PipeEntry entry, SubmitNodeStorage storage) {
      poseStack.pushPose();
      poseStack.translate(entry.x, entry.y, entry.z);
      com.mojang.blaze3d.vertex.PoseStack.Pose pipePose = poseStack.last();
      storage.submitCustomGeometry(poseStack, BCLibRenderTypes.cutoutBlockSheet(), (p, vc) -> {
         ModelPipe.renderDirect(entry.pipeKey, pipePose, vc, FULL_BRIGHT);
      });
      storage.submitCustomGeometry(poseStack, BCLibRenderTypes.translucentBlockSheet(), (p, vc) -> {
         ModelPipe.renderMaskOverlay(entry.pipeKey, pipePose, vc, FULL_BRIGHT, 76);
      });
      poseStack.popPose();
   }

   private void submitFluidCube26(PoseStack poseStack, BlueprintPipRenderer.FluidEntry entry, int lightmap, SubmitNodeStorage storage) {
      FluidStack stack = new FluidStack(entry.fluidState.getType(), 1);
      BcFluidAppearance appearance = BcFluidAppearanceCache.get(stack);
      if (appearance != null && appearance.sprite() != null) {
         TextureAtlasSprite sprite = appearance.sprite();
         int tint = appearance.tint();
         float a0 = (tint >>> 24 & 0xFF) / 255.0F;
         final float r = (tint >>> 16 & 0xFF) / 255.0F;
         final float g = (tint >>> 8 & 0xFF) / 255.0F;
         final float b = (tint & 0xFF) / 255.0F;
         final float a = a0 <= 0.0F ? 1.0F : a0;
         float h = entry.fluidState.isSource() ? 1.0F : Math.max(0.125F, entry.fluidState.getOwnHeight());
         poseStack.pushPose();
         poseStack.translate(entry.x, entry.y, entry.z);
         com.mojang.blaze3d.vertex.PoseStack.Pose pose = poseStack.last();
         storage.submitCustomGeometry(poseStack, BcFluidAppearanceCache.renderType(appearance), (p, vc) -> {
            int overlay = OverlayTexture.NO_OVERLAY;
            if (!entry.cullTop) {
               BcFluidVertexEmitter.emitQuadWithAtlasUv(pose, vc, sprite, 0.0F, h, 0.0F, sprite.getU(0.0F), sprite.getV(0.0F), 0.0F, h, 1.0F, sprite.getU(0.0F), sprite.getV(1.0F), 1.0F, h, 1.0F, sprite.getU(1.0F), sprite.getV(1.0F), 1.0F, h, 0.0F, sprite.getU(1.0F), sprite.getV(0.0F), 0.0F, 1.0F, 0.0F, r, g, b, a, lightmap, overlay);
            }
         });
         poseStack.popPose();
      }
   }
   //?}

   private void ensureLightingBufferAllocated() {
      if (this.lightingBuffer == null) {
         GpuDevice device = RenderSystem.getDevice();
         //? if >= 26.2 {
         this.lightingBufferPaddedSize = Lighting.UBO_SIZE;
         //?} else {
         /*this.lightingBufferPaddedSize = Mth.roundToward(Lighting.UBO_SIZE, device.getUniformOffsetAlignment());
         *///?}
         this.lightingBuffer = device.createBuffer(() -> "BCBlueprintPipLighting", 136, this.lightingBufferPaddedSize);
      }
   }

   private void writeLightDirections(Vector3f light0, Vector3f light1) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer bb = Std140Builder.onStack(stack, Lighting.UBO_SIZE).putVec3(light0).putVec3(light1).get();
         // GpuBuffer.slice length arg is int on 1.21.10 and long on 1.21.11; the (int) cast satisfies both.
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.lightingBuffer.slice(0, (int) this.lightingBufferPaddedSize), bb);
      } catch (Throwable var7) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public Class<BlueprintPipRenderState> getRenderStateClass() {
      return BlueprintPipRenderState.class;
   }

   protected String getTextureLabel() {
      return "buildcraft_blueprint_preview";
   }

   protected float getTranslateY(int height, int guiScale) {
      return height / 2.0F;
   }

   //? if >= 26.2 {
   //?} else {
   /*protected void renderToTexture(BlueprintPipRenderState renderState, PoseStack poseStack) {
      Snapshot snapshot = renderState.snapshot();
      BlockPos size = snapshot.size;
      int sizeX = Math.max(1, size.getX());
      int sizeY = Math.max(1, size.getY());
      int sizeZ = Math.max(1, size.getZ());
      poseStack.scale(1.0F, -1.0F, -1.0F);
      Minecraft mc = Minecraft.getInstance();
      long gameTime = mc.level != null ? mc.level.getGameTime() : 0L;
      float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float yaw = ((float)(gameTime % 72L) + partialTick) / 72.0F * 360.0F;
      poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
      poseStack.translate(-sizeX / 2.0F, -sizeY / 2.0F, -sizeZ / 2.0F);
      Vector3f light0Camera = poseStack.last().transformNormal(LIGHT0_MODEL_SPACE.x(), LIGHT0_MODEL_SPACE.y(), LIGHT0_MODEL_SPACE.z(), new Vector3f());
      Vector3f light1Camera = poseStack.last().transformNormal(LIGHT1_MODEL_SPACE.x(), LIGHT1_MODEL_SPACE.y(), LIGHT1_MODEL_SPACE.z(), new Vector3f());
      this.ensureLightingBufferAllocated();
      this.writeLightDirections(light0Camera, light1Camera);
      GpuBufferSlice savedShaderLights = RenderSystem.getShaderLights();
      RenderSystem.setShaderLights(this.lightingBuffer.slice(0, (int) Lighting.UBO_SIZE));
      FeatureRenderDispatcher featureRenderDispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
      SubmitNodeStorage submitNodeStorage = featureRenderDispatcher.getSubmitNodeStorage();
      BlueprintPipRenderer.PreviewPlan plan = this.planFor(snapshot, mc);

      for (BlueprintPipRenderer.TemplateEntry entry : plan.templateEntries) {
         this.submitTemplateGhostCube(poseStack, entry);
      }

      for (BlueprintPipRenderer.PipeEntry entry : plan.pipeEntries) {
         this.submitPipeEntry(poseStack, entry);
      }

      for (BlueprintPipRenderer.FluidEntry entry : plan.fluidEntries) {
         this.submitFluidCube(poseStack, entry, 15728880);
      }

      for (BlueprintPipRenderer.ItemEntry entry : plan.itemEntries) {
         poseStack.pushPose();
         poseStack.translate(entry.x + 0.5F, entry.y + 0.5F, entry.z + 0.5F);
         entry.renderState.submit(poseStack, submitNodeStorage, 15728880, OverlayTexture.NO_OVERLAY, 0);
         poseStack.popPose();
      }

      featureRenderDispatcher.renderAllFeatures();
      this.bufferSource.endBatch();
      RenderSystem.setShaderLights(savedShaderLights);
      if (LOGGED_SNAPSHOTS.add(System.identityHashCode(snapshot))) {
         LOGGER.info(
            "renderToTexture: type={} size={}x{}x{} submitted={} submittedFluid={} submittedTemplate={} submittedPipe={} skippedNoItem={} skippedAirOrEmpty={} skippedHidden={} sampleSchBlock={} distinctStates={}",
            new Object[]{
               snapshot.getClass().getSimpleName(),
               sizeX,
               sizeY,
               sizeZ,
               plan.itemEntries.size(),
               plan.fluidEntries.size(),
               plan.templateEntries.size(),
               plan.pipeEntries.size(),
               plan.skippedNoItem,
               plan.skippedAirOrEmpty,
               plan.skippedHidden,
               plan.sampleClassName,
               plan.distinctStates
            }
         );
      }
   }
   *///?}

   private BlueprintPipRenderer.PreviewPlan planFor(Snapshot snapshot, Minecraft mc) {
      BlueprintPipRenderer.PlanKey key = BlueprintPipRenderer.PlanKey.of(snapshot);
      return this.planCache.computeIfAbsent(key, ignored -> this.buildPlan(snapshot, mc));
   }

   private BlueprintPipRenderer.PreviewPlan buildPlan(Snapshot snapshot, Minecraft mc) {
      BlueprintPipRenderer.PreviewPlan plan = new BlueprintPipRenderer.PreviewPlan();
      BlockPos size = snapshot.size;
      int sizeX = Math.max(1, size.getX());
      int sizeY = Math.max(1, size.getY());
      int sizeZ = Math.max(1, size.getZ());
      Blueprint blueprint = snapshot instanceof Blueprint bp ? bp : null;
      Template template = snapshot instanceof Template tp ? tp : null;
      Map<BlockState, TrackingItemStackRenderState> stateCache = new HashMap<>();
      MutableBlockPos pos = new MutableBlockPos();

      for (int z = 0; z < sizeZ; z++) {
         for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
               pos.set(x, y, z);
               int dataIndex = Snapshot.posToIndex(size, pos);
               if (template != null) {
                  if (template.data != null && template.data.get(dataIndex)) {
                     EnumSet<Direction> faces = TemplateGhostGeometry.visibleFaces(template, size, x, y, z);
                     if (!faces.isEmpty()) {
                        plan.templateEntries.add(new BlueprintPipRenderer.TemplateEntry(x, y, z, faces));
                     }
                  } else {
                     plan.skippedAirOrEmpty++;
                  }
               } else if (blueprint != null && blueprint.data != null) {
                  int index = blueprint.data[dataIndex];
                  if (index >= 0 && index < blueprint.palette.size()) {
                     ISchematicBlock schBlock = blueprint.palette.get(index);
                     if (schBlock != null && !schBlock.isAir()) {
                        BlockState state = schBlock.getBlockStateForRender();
                        if (state != null && !state.isAir()) {
                           if (plan.sampleClassName.equals("n/a")) {
                              plan.sampleClassName = schBlock.getClass().getSimpleName();
                           }

                           if (PipePreviewModel.isPipe(state)) {
                              PipeModelKey pipeKey = PipePreviewModel.modelKey(schBlock.getTileNbtForRender());
                              if (pipeKey != null) {
                                 plan.pipeEntries.add(new BlueprintPipRenderer.PipeEntry(x, y, z, pipeKey));
                                 continue;
                              }
                           }

                           FluidState fluidState = state.getFluidState();
                           if (!fluidState.isEmpty()) {
                              Fluid fluid = fluidState.getType();
                              plan.fluidEntries
                                 .add(
                                    new BlueprintPipRenderer.FluidEntry(
                                       x,
                                       y,
                                       z,
                                       fluidState,
                                       neighborIsSameFluid(blueprint, size, x, y + 1, z, fluid),
                                       neighborIsSameFluid(blueprint, size, x, y - 1, z, fluid),
                                       neighborIsSameFluid(blueprint, size, x, y, z - 1, fluid),
                                       neighborIsSameFluid(blueprint, size, x, y, z + 1, fluid),
                                       neighborIsSameFluid(blueprint, size, x - 1, y, z, fluid),
                                       neighborIsSameFluid(blueprint, size, x + 1, y, z, fluid)
                                    )
                                 );
                           } else if (shouldCullItemBlock(blueprint, size, x, y, z, state)) {
                              plan.skippedHidden++;
                           } else {
                              TrackingItemStackRenderState itemRenderState = stateCache.get(state);
                              if (itemRenderState == null) {
                                 ItemStack stack = new ItemStack(state.getBlock());
                                 if (stack.isEmpty()) {
                                    stateCache.put(state, MARKER_EMPTY);
                                    plan.skippedNoItem++;
                                    continue;
                                 }

                                 itemRenderState = new TrackingItemStackRenderState();
                                 mc.getItemModelResolver().updateForTopItem(itemRenderState, stack, ItemDisplayContext.NONE, mc.level, null, 0);
                                 stateCache.put(state, itemRenderState);
                              } else if (itemRenderState == MARKER_EMPTY) {
                                 plan.skippedNoItem++;
                                 continue;
                              }

                              plan.itemEntries.add(new BlueprintPipRenderer.ItemEntry(x, y, z, itemRenderState));
                           }
                        } else {
                           plan.skippedAirOrEmpty++;
                        }
                     } else {
                        plan.skippedAirOrEmpty++;
                     }
                  } else {
                     plan.skippedAirOrEmpty++;
                  }
               } else {
                  plan.skippedAirOrEmpty++;
               }
            }
         }
      }

      plan.distinctStates = stateCache.size();
      return plan;
   }

   private static boolean shouldCullItemBlock(Blueprint blueprint, BlockPos size, int x, int y, int z, BlockState state) {
      if (!isPreviewOpaqueCube(state)) {
         return false;
      }

      for (Direction direction : Direction.values()) {
         BlockState neighbor = blueprintState(blueprint, size, x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
         if (!isPreviewOpaqueCube(neighbor)) {
            return false;
         }
      }

      return true;
   }

   private static boolean isPreviewOpaqueCube(BlockState state) {
      return state != null && state.canOcclude() && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
   }

   private static BlockState blueprintState(Blueprint blueprint, BlockPos size, int x, int y, int z) {
      if (x >= 0 && y >= 0 && z >= 0 && x < size.getX() && y < size.getY() && z < size.getZ()) {
         int idx = blueprint.data[Snapshot.posToIndex(size, x, y, z)];
         if (idx >= 0 && idx < blueprint.palette.size()) {
            ISchematicBlock schBlock = blueprint.palette.get(idx);
            return schBlock != null && !schBlock.isAir() ? schBlock.getBlockStateForRender() : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   //? if >= 26.2 {
   //?} else {
   /*private void submitPipeEntry(PoseStack poseStack, BlueprintPipRenderer.PipeEntry entry) {
      poseStack.pushPose();
      poseStack.translate(entry.x, entry.y, entry.z);
      Pose pipePose = poseStack.last();
      ModelPipe.renderDirect(entry.pipeKey, pipePose, this.bufferSource.getBuffer(BCLibRenderTypes.cutoutBlockSheet()), 15728880);
      ModelPipe.renderMaskOverlay(
         entry.pipeKey, pipePose, this.bufferSource.getBuffer(BCLibRenderTypes.translucentBlockSheet()), 15728880, 76
      );
      poseStack.popPose();
   }
   *///?}

   //? if >= 26.2 {
   //?} else {
   /*private void submitFluidCube(PoseStack poseStack, BlueprintPipRenderer.FluidEntry entry, int lightmap) {
      int xCell = entry.x;
      int yCell = entry.y;
      int zCell = entry.z;
      FluidState fluidState = entry.fluidState;
      Fluid fluid = fluidState.getType();
      FluidStack stack = new FluidStack(fluid, 1);
      BcFluidAppearance appearance = BcFluidAppearanceCache.get(stack);
      if (appearance != null && appearance.sprite() != null) {
         TextureAtlasSprite sprite = appearance.sprite();
         int tint = appearance.tint();
         float a = (tint >>> 24 & 0xFF) / 255.0F;
         float r = (tint >>> 16 & 0xFF) / 255.0F;
         float g = (tint >>> 8 & 0xFF) / 255.0F;
         float b = (tint & 0xFF) / 255.0F;
         if (a <= 0.0F) {
            a = 1.0F;
         }

         float h = fluidState.isSource() ? 1.0F : Math.max(0.125F, fluidState.getOwnHeight());
         VertexConsumer vc = this.bufferSource.getBuffer(BcFluidAppearanceCache.renderType(appearance));
         poseStack.pushPose();
         poseStack.translate(xCell, yCell, zCell);
         Pose pose = poseStack.last();
         int overlay = OverlayTexture.NO_OVERLAY;
         if (!entry.cullTop) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(
               pose,
               vc,
               sprite,
               0.0F,
               h,
               0.0F,
               sprite.getU(0.0F),
               sprite.getV(0.0F),
               0.0F,
               h,
               1.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F),
               1.0F,
               h,
               1.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F),
               1.0F,
               h,
               0.0F,
               sprite.getU(1.0F),
               sprite.getV(0.0F),
               0.0F,
               1.0F,
               0.0F,
               r,
               g,
               b,
               a,
               lightmap,
               overlay
            );
         }

         if (!entry.cullBottom) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(
               pose,
               vc,
               sprite,
               0.0F,
               0.0F,
               0.0F,
               sprite.getU(0.0F),
               sprite.getV(0.0F),
               1.0F,
               0.0F,
               0.0F,
               sprite.getU(1.0F),
               sprite.getV(0.0F),
               1.0F,
               0.0F,
               1.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F),
               0.0F,
               0.0F,
               1.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F),
               0.0F,
               -1.0F,
               0.0F,
               r,
               g,
               b,
               a,
               lightmap,
               overlay
            );
         }

         if (!entry.cullNorth) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(
               pose,
               vc,
               sprite,
               0.0F,
               0.0F,
               0.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F),
               0.0F,
               h,
               0.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F - h),
               1.0F,
               h,
               0.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F - h),
               1.0F,
               0.0F,
               0.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F),
               0.0F,
               0.0F,
               -1.0F,
               r,
               g,
               b,
               a,
               lightmap,
               overlay
            );
         }

         if (!entry.cullSouth) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(
               pose,
               vc,
               sprite,
               1.0F,
               0.0F,
               1.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F),
               1.0F,
               h,
               1.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F - h),
               0.0F,
               h,
               1.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F - h),
               0.0F,
               0.0F,
               1.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F),
               0.0F,
               0.0F,
               1.0F,
               r,
               g,
               b,
               a,
               lightmap,
               overlay
            );
         }

         if (!entry.cullWest) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(
               pose,
               vc,
               sprite,
               0.0F,
               0.0F,
               1.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F),
               0.0F,
               h,
               1.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F - h),
               0.0F,
               h,
               0.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F - h),
               0.0F,
               0.0F,
               0.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F),
               -1.0F,
               0.0F,
               0.0F,
               r,
               g,
               b,
               a,
               lightmap,
               overlay
            );
         }

         if (!entry.cullEast) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(
               pose,
               vc,
               sprite,
               1.0F,
               0.0F,
               0.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F),
               1.0F,
               h,
               0.0F,
               sprite.getU(0.0F),
               sprite.getV(1.0F - h),
               1.0F,
               h,
               1.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F - h),
               1.0F,
               0.0F,
               1.0F,
               sprite.getU(1.0F),
               sprite.getV(1.0F),
               1.0F,
               0.0F,
               0.0F,
               r,
               g,
               b,
               a,
               lightmap,
               overlay
            );
         }

         poseStack.popPose();
      }
   }
   *///?}

   private static boolean neighborIsSameFluid(Blueprint blueprint, BlockPos size, int nx, int ny, int nz, Fluid fluid) {
      if (nx >= 0 && ny >= 0 && nz >= 0 && nx < size.getX() && ny < size.getY() && nz < size.getZ()) {
         BlockPos neighbor = NEIGHBOR_SCRATCH.get().set(nx, ny, nz);
         int idx = blueprint.data[Snapshot.posToIndex(size, neighbor)];
         if (idx >= 0 && idx < blueprint.palette.size()) {
            ISchematicBlock schBlock = blueprint.palette.get(idx);
            if (schBlock == null) {
               return false;
            }

            BlockState nState = schBlock.getBlockStateForRender();
            if (nState == null) {
               return false;
            }

            FluidState nFluid = nState.getFluidState();
            return !nFluid.isEmpty() && nFluid.is(fluid);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   //? if >= 26.2 {
   //?} else {
   /*private void submitTemplateGhostCube(PoseStack poseStack, BlueprintPipRenderer.TemplateEntry entry) {
      VertexConsumer vc = this.bufferSource.getBuffer(BCLibRenderTypes.entityTranslucent(SCAN_TEXTURE));
      poseStack.pushPose();
      poseStack.translate(entry.x, entry.y, entry.z);
      Pose pose = poseStack.last();

      for (Direction face : entry.faces) {
         ModelUtil.createFace(face, GHOST_CENTER, GHOST_RADIUS, GHOST_UVS).lighti(15, 15).colouri(255, 255, 255, 128).render(pose, vc);
      }

      poseStack.popPose();
   }
   *///?}

   private record FluidEntry(
      int x, int y, int z, FluidState fluidState, boolean cullTop, boolean cullBottom, boolean cullNorth, boolean cullSouth, boolean cullWest, boolean cullEast
   ) {
   }

   private record ItemEntry(int x, int y, int z, TrackingItemStackRenderState renderState) {
   }

   private record PipeEntry(int x, int y, int z, PipeModelKey pipeKey) {
   }

   private record PlanKey(Class<?> type, int sizeX, int sizeY, int sizeZ, int snapshotHash, int hashLength, int identity) {
      static BlueprintPipRenderer.PlanKey of(Snapshot snapshot) {
         BlockPos size = snapshot.size;
         byte[] hash = snapshot.key == null ? null : snapshot.key.hash;
         int hashLength = hash == null ? 0 : hash.length;
         int identity = hashLength == 0 ? System.identityHashCode(snapshot) : 0;
         return new BlueprintPipRenderer.PlanKey(
            snapshot.getClass(),
            size == null ? 0 : size.getX(),
            size == null ? 0 : size.getY(),
            size == null ? 0 : size.getZ(),
            Arrays.hashCode(hash),
            hashLength,
            identity
         );
      }
   }

   private static final class PreviewPlan {
      private final List<BlueprintPipRenderer.ItemEntry> itemEntries = new ArrayList<>();
      private final List<BlueprintPipRenderer.PipeEntry> pipeEntries = new ArrayList<>();
      private final List<BlueprintPipRenderer.FluidEntry> fluidEntries = new ArrayList<>();
      private final List<BlueprintPipRenderer.TemplateEntry> templateEntries = new ArrayList<>();
      private int skippedNoItem;
      private int skippedAirOrEmpty;
      private int skippedHidden;
      private int distinctStates;
      private String sampleClassName = "n/a";
   }

   private record TemplateEntry(int x, int y, int z, EnumSet<Direction> faces) {
   }
}
