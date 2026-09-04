/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render.pip;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.client.model.PipePluggableQuadCache;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import buildcraft.lib.client.fluid.BcFluidVertexEmitter;
import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.GeometrySink;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.key.PipeModelKey;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
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
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

public class BlueprintPipRenderer extends PictureInPictureRenderer<BlueprintPipRenderState> {
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
   private static final Map<BlueprintPipRenderer.PlanKey, BlueprintPipRenderer.PreviewPlan> PLAN_CACHE = new LinkedHashMap<BlueprintPipRenderer.PlanKey, BlueprintPipRenderer.PreviewPlan>(
      16, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<BlueprintPipRenderer.PlanKey, BlueprintPipRenderer.PreviewPlan> eldest) {
         return this.size() > 16;
      }
   };
   private static final ThreadLocal<MutableBlockPos> NEIGHBOR_SCRATCH = ThreadLocal.withInitial(MutableBlockPos::new);

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
      //? if < 26.3-pre-1 {
      /*this.render(renderState, poseStack, GeometrySink.of(storage), () -> Minecraft.getInstance().gameRenderer.featureRenderDispatcher().renderAllFeatures(storage));
      *///?} else {
      this.render(renderState, poseStack, GeometrySink.of(storage), () -> {});
      //?}
   }
   //?} else {
   /*@Override
   protected void renderToTexture(BlueprintPipRenderState renderState, PoseStack poseStack) {
      this.render(renderState, poseStack, GeometrySink.of(this.bufferSource), () -> {
         Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
         this.bufferSource.endBatch();
      });
   }
   *///?}

   private void render(BlueprintPipRenderState renderState, PoseStack poseStack, GeometrySink sink, Runnable flush) {
      Snapshot snapshot = renderState.snapshot();
      BlockPos size = snapshot.size;
      int sizeX = Math.max(1, size.getX());
      int sizeY = Math.max(1, size.getY());
      int sizeZ = Math.max(1, size.getZ());
      poseStack.scale(1.0F, -1.0F, -1.0F);
      Minecraft mc = Minecraft.getInstance();
      long gameTime = mc.level != null ? mc.level.getGameTime() : 0L;
      float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float yaw = ((float)(gameTime % YAW_PERIOD_TICKS) + partialTick) / (float) YAW_PERIOD_TICKS * 360.0F;
      poseStack.rotate(Axis.XP.rotationDegrees(PITCH_DEG));
      poseStack.rotate(Axis.YP.rotationDegrees(yaw));
      poseStack.translate(-sizeX / 2.0F, -sizeY / 2.0F, -sizeZ / 2.0F);
      Vector3f light0Camera = poseStack.last().transformNormal(LIGHT0_MODEL_SPACE.x(), LIGHT0_MODEL_SPACE.y(), LIGHT0_MODEL_SPACE.z(), new Vector3f());
      Vector3f light1Camera = poseStack.last().transformNormal(LIGHT1_MODEL_SPACE.x(), LIGHT1_MODEL_SPACE.y(), LIGHT1_MODEL_SPACE.z(), new Vector3f());
      this.ensureLightingBufferAllocated();
      this.writeLightDirections(light0Camera, light1Camera);
      GpuBufferSlice savedShaderLights = RenderSystem.getShaderLights();
      RenderSystem.setShaderLights(this.lightingBuffer.slice(0,  Lighting.UBO_SIZE));
      BlueprintPipRenderer.PreviewPlan plan = this.planFor(snapshot, mc);

      for (BlueprintPipRenderer.TemplateEntry entry : plan.templateEntries) {
         submitTemplateGhostCube(poseStack, entry, sink);
      }

      for (BlueprintPipRenderer.PipeEntry entry : plan.pipeEntries) {
         submitPipeEntry(poseStack, entry, sink);
      }

      for (BlueprintPipRenderer.FluidEntry entry : plan.fluidEntries) {
         submitFluidCube(poseStack, entry, FULL_BRIGHT, sink);
      }

      if (!plan.blockEntries.isEmpty()) {
         sink.submit(poseStack, BCLibRenderTypes.cutoutBlockSheet(), (pose, vc) -> renderBlockEntries(plan.blockEntries, pose, vc));
      }

      flush.run();
      RenderSystem.setShaderLights(savedShaderLights);
   }

   private static void submitTemplateGhostCube(PoseStack poseStack, BlueprintPipRenderer.TemplateEntry entry, GeometrySink sink) {
      poseStack.pushPose();
      poseStack.translate(entry.x, entry.y, entry.z);
      sink.submit(poseStack, BCLibRenderTypes.entityTranslucent(SCAN_TEXTURE), (pose, vc) -> {
         for (Direction face : entry.faces) {
            ModelUtil.createFace(face, GHOST_CENTER, GHOST_RADIUS, GHOST_UVS).lighti(15, 15).colouri(255, 255, 255, TEMPLATE_GHOST_ALPHA).render(pose, vc);
         }
      });
      poseStack.popPose();
   }

   private static void submitPipeEntry(PoseStack poseStack, BlueprintPipRenderer.PipeEntry entry, GeometrySink sink) {
      poseStack.pushPose();
      poseStack.translate(entry.x, entry.y, entry.z);
      sink.submit(poseStack, BCLibRenderTypes.cutoutBlockSheet(), (pose, vc) -> {
         ModelPipe.renderDirect(entry.pipeKey, pose, vc, FULL_BRIGHT);
         renderPluggables(entry.plugs, pose, vc);
      });
      sink.submit(poseStack, BCLibRenderTypes.translucentBlockSheet(), (pose, vc) -> ModelPipe.renderMaskOverlay(entry.pipeKey, pose, vc, FULL_BRIGHT, 76));
      poseStack.popPose();
   }

   private static void submitFluidCube(PoseStack poseStack, BlueprintPipRenderer.FluidEntry entry, int lightmap, GeometrySink sink) {
      FluidState fluidState = entry.fluidState;
      FluidStack stack = new FluidStack(fluidState.getType(), 1);
      BcFluidAppearance appearance = BcFluidAppearanceCache.get(stack);
      if (appearance == null || appearance.sprite() == null) {
         return;
      }

      TextureAtlasSprite sprite = appearance.sprite();
      int tint = appearance.tint();
      float a0 = (tint >>> 24 & 0xFF) / 255.0F;
      float r = (tint >>> 16 & 0xFF) / 255.0F;
      float g = (tint >>> 8 & 0xFF) / 255.0F;
      float b = (tint & 0xFF) / 255.0F;
      float a = a0 <= 0.0F ? 1.0F : a0;
      float h = fluidState.isSource() ? 1.0F : Math.max(0.125F, fluidState.getOwnHeight());
      poseStack.pushPose();
      poseStack.translate(entry.x, entry.y, entry.z);
      sink.submit(poseStack, BcFluidAppearanceCache.renderType(appearance), (pose, vc) -> {
         int overlay = OverlayTexture.NO_OVERLAY;
         float u0 = sprite.getU(0.0F);
         float u1 = sprite.getU(1.0F);
         float v0 = sprite.getV(0.0F);
         float v1 = sprite.getV(1.0F);
         float vh = sprite.getV(1.0F - h);
         if (!entry.cullTop) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(pose, vc, sprite, 0.0F, h, 0.0F, u0, v0, 0.0F, h, 1.0F, u0, v1, 1.0F, h, 1.0F, u1, v1, 1.0F, h, 0.0F, u1, v0, 0.0F, 1.0F, 0.0F, r, g, b, a, lightmap, overlay);
         }

         if (!entry.cullBottom) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(pose, vc, sprite, 0.0F, 0.0F, 0.0F, u0, v0, 1.0F, 0.0F, 0.0F, u1, v0, 1.0F, 0.0F, 1.0F, u1, v1, 0.0F, 0.0F, 1.0F, u0, v1, 0.0F, -1.0F, 0.0F, r, g, b, a, lightmap, overlay);
         }

         if (!entry.cullNorth) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(pose, vc, sprite, 0.0F, 0.0F, 0.0F, u0, v1, 0.0F, h, 0.0F, u0, vh, 1.0F, h, 0.0F, u1, vh, 1.0F, 0.0F, 0.0F, u1, v1, 0.0F, 0.0F, -1.0F, r, g, b, a, lightmap, overlay);
         }

         if (!entry.cullSouth) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(pose, vc, sprite, 1.0F, 0.0F, 1.0F, u0, v1, 1.0F, h, 1.0F, u0, vh, 0.0F, h, 1.0F, u1, vh, 0.0F, 0.0F, 1.0F, u1, v1, 0.0F, 0.0F, 1.0F, r, g, b, a, lightmap, overlay);
         }

         if (!entry.cullWest) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(pose, vc, sprite, 0.0F, 0.0F, 1.0F, u0, v1, 0.0F, h, 1.0F, u0, vh, 0.0F, h, 0.0F, u1, vh, 0.0F, 0.0F, 0.0F, u1, v1, -1.0F, 0.0F, 0.0F, r, g, b, a, lightmap, overlay);
         }

         if (!entry.cullEast) {
            BcFluidVertexEmitter.emitQuadWithAtlasUv(pose, vc, sprite, 1.0F, 0.0F, 0.0F, u0, v1, 1.0F, h, 0.0F, u0, vh, 1.0F, h, 1.0F, u1, vh, 1.0F, 0.0F, 1.0F, u1, v1, 1.0F, 0.0F, 0.0F, r, g, b, a, lightmap, overlay);
         }
      });
      poseStack.popPose();
   }

   public static void onModelBake() {
      PLAN_CACHE.clear();
   }

   @Override
   public void close() {
      super.close();
      if (this.lightingBuffer != null) {
         this.lightingBuffer.close();
         this.lightingBuffer = null;
      }
   }

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
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.lightingBuffer.slice(0,  this.lightingBufferPaddedSize), bb);
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

   private BlueprintPipRenderer.PreviewPlan planFor(Snapshot snapshot, Minecraft mc) {
      BlueprintPipRenderer.PlanKey key = BlueprintPipRenderer.PlanKey.of(snapshot);
      return PLAN_CACHE.computeIfAbsent(key, ignored -> this.buildPlan(snapshot, mc));
   }

   private BlueprintPipRenderer.PreviewPlan buildPlan(Snapshot snapshot, Minecraft mc) {
      BlueprintPipRenderer.PreviewPlan plan = new BlueprintPipRenderer.PreviewPlan();
      BlockPos size = snapshot.size;
      int sizeX = Math.max(1, size.getX());
      int sizeY = Math.max(1, size.getY());
      int sizeZ = Math.max(1, size.getZ());
      Blueprint blueprint = snapshot instanceof Blueprint bp ? bp : null;
      Template template = snapshot instanceof Template tp ? tp : null;
      Map<BlockState, List<MutableQuad>> stateCache = new HashMap<>();
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
                  }
               } else if (blueprint != null && blueprint.data != null) {
                  int index = blueprint.data[dataIndex];
                  if (index >= 0 && index < blueprint.palette.size()) {
                     ISchematicBlock schBlock = blueprint.palette.get(index);
                     if (schBlock != null && !schBlock.isAir()) {
                        BlockState state = schBlock.getBlockStateForRender();
                        if (state != null && !state.isAir()) {
                           if (PipePreviewModel.isPipe(state)) {
                              PipeModelKey pipeKey = PipePreviewModel.modelKey(schBlock.getTileNbtForRender());
                              if (pipeKey != null) {
                                 plan.pipeEntries
                                    .add(
                                       new BlueprintPipRenderer.PipeEntry(
                                          x,
                                          y,
                                          z,
                                          withBlueprintConnections(pipeKey, blueprint, size, x, y, z),
                                          PipePreviewModel.pluggableKeys(schBlock.getTileNbtForRender())
                                       )
                                    );
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
                           } else if (!shouldCullItemBlock(blueprint, size, x, y, z, state)) {
                              List<MutableQuad> quads = stateCache.get(state);
                              if (quads == null) {
                                 quads = buildBlockQuads(state);
                                 stateCache.put(state, quads);
                              }

                              if (!quads.isEmpty()) {
                                 plan.blockEntries.add(new BlueprintPipRenderer.BlockEntry(x, y, z, quads));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

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

   private static boolean neighborIsPipe(Blueprint blueprint, BlockPos size, int nx, int ny, int nz) {
      if (nx < 0 || ny < 0 || nz < 0 || nx >= size.getX() || ny >= size.getY() || nz >= size.getZ()) {
         return false;
      }

      BlockPos neighbor = NEIGHBOR_SCRATCH.get().set(nx, ny, nz);
      int idx = blueprint.data[Snapshot.posToIndex(size, neighbor)];
      if (idx < 0 || idx >= blueprint.palette.size()) {
         return false;
      }

      ISchematicBlock schBlock = blueprint.palette.get(idx);
      if (schBlock == null) {
         return false;
      }

      BlockState nState = schBlock.getBlockStateForRender();
      return nState != null && PipePreviewModel.isPipe(nState);
   }

   private static PipeModelKey withBlueprintConnections(PipeModelKey base, Blueprint blueprint, BlockPos size, int x, int y, int z) {
      float[] connected = new float[6];

      for (Direction face : Direction.values()) {
         if (neighborIsPipe(blueprint, size, x + face.getStepX(), y + face.getStepY(), z + face.getStepZ())) {
            connected[face.ordinal()] = 0.25F;
         }
      }

      return new PipeModelKey(base.definition, base.center, base.sides, connected, base.colour, 0);
   }

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

   private record FluidEntry(
      int x, int y, int z, FluidState fluidState, boolean cullTop, boolean cullBottom, boolean cullNorth, boolean cullSouth, boolean cullWest, boolean cullEast
   ) {
   }

   private static final List<MutableQuad> NO_QUADS = List.of();
   private static final net.minecraft.util.RandomSource QUAD_RANDOM = net.minecraft.util.RandomSource.create(42L);

   private static List<MutableQuad> buildBlockQuads(BlockState state) {
      Minecraft mc = Minecraft.getInstance();
      //? if >= 26.1 {
      BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
      //?} else {
      /*BlockStateModel model = mc.getBlockRenderer().getBlockModelShaper().getBlockModel(state);
      *///?}
      List<BlockStateModelPart> parts = new ArrayList<>();
      if (model == null) {
         return NO_QUADS;
      }

      model.collectParts(QUAD_RANDOM, parts);
      List<BakedQuad> baked = new ArrayList<>();

      for (BlockStateModelPart part : parts) {
         baked.addAll(part.getQuads(null));

         for (Direction face : Direction.values()) {
            baked.addAll(part.getQuads(face));
         }
      }

      if (baked.isEmpty()) {
         return NO_QUADS;
      }

      List<MutableQuad> quads = new ArrayList<>(baked.size());

      for (BakedQuad quad : baked) {
         MutableQuad mutable = new MutableQuad().fromBakedBlock(quad);
         mutable.setCalculatedDiffuse();
         int tintIndex = mutable.getTint();
         if (tintIndex >= 0) {
            //? if >= 26.1 {
            net.minecraft.client.color.block.BlockTintSource source = mc.getBlockColors().getTintSource(state, tintIndex);
            int rgb = source == null ? -1 : source.color(state);
            //?} else {
            /*int rgb = mc.getBlockColors().getColor(state, null, null, tintIndex);
            *///?}
            if (rgb != -1) {
               mutable.multColouri(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, 255);
            }
         }

         mutable.lighti(15, 15);
         quads.add(mutable);
      }

      return quads;
   }

   private static void renderPluggables(List<PluggableModelKey> plugs, Pose pose, VertexConsumer vc) {
      for (PluggableModelKey key : plugs) {
         PipePluggableQuadCache.renderCutoutTintResolved(key, pose, vc, FULL_BRIGHT, tint -> key.resolveWorldTint(tint, null, null));
      }
   }

   private static void renderBlockEntries(List<BlueprintPipRenderer.BlockEntry> entries, Pose pose, VertexConsumer vc) {
      MutableQuad scratch = new MutableQuad();

      for (BlueprintPipRenderer.BlockEntry entry : entries) {
         for (MutableQuad quad : entry.quads) {
            scratch.copyFrom(quad);
            scratch.translatef(entry.x, entry.y, entry.z);
            scratch.render(pose, vc);
         }
      }
   }

   private record BlockEntry(int x, int y, int z, List<MutableQuad> quads) {
   }

   private record PipeEntry(int x, int y, int z, PipeModelKey pipeKey, List<PluggableModelKey> plugs) {
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
      private final List<BlueprintPipRenderer.BlockEntry> blockEntries = new ArrayList<>();
      private final List<BlueprintPipRenderer.PipeEntry> pipeEntries = new ArrayList<>();
      private final List<BlueprintPipRenderer.FluidEntry> fluidEntries = new ArrayList<>();
      private final List<BlueprintPipRenderer.TemplateEntry> templateEntries = new ArrayList<>();
   }

   private record TemplateEntry(int x, int y, int z, EnumSet<Direction> faces) {
   }
}
