/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render.pip;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.fluid.BcFluidVertexEmitter;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.key.PipeModelKey;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector3f;

/**
 * 1.21.1 (versions/1.21.1) blueprint/template preview renderer. The shared class extends the 1.21.5
 * PictureInPictureRenderer (+ GPU lighting UBOs + ItemStackRenderState), none of which exist on 1.21.1.
 * Here the rotating 3D structure is drawn directly in the GUI pose space (like an enlarged 3D item icon):
 * each block is rendered via ItemRenderer.renderStatic, pipes via ModelPipe.renderDirect, fluids/template
 * ghosts via immediate quads, all clipped to the preview rect. The plan (block->ItemStack/pipe/fluid/ghost)
 * mirrors the shared buildPlan but stores ItemStacks instead of the 1.21.4 render-state.
 */
public final class BlueprintPipRenderer {
   private static final int FULL_BRIGHT = 15728880;
   private static final Identifier SCAN_TEXTURE = Identifier.parse("buildcraftbuilders:textures/block/scan.png");
   private static final Vector3f GHOST_CENTER = new Vector3f(0.5F, 0.5F, 0.5F);
   private static final Vector3f GHOST_RADIUS = new Vector3f(0.5F, 0.5F, 0.5F);
   private static final ModelUtil.UvFaceData GHOST_UVS = new ModelUtil.UvFaceData(0.0F, 0.0F, 1.0F, 1.0F);

   private static final Map<PlanKey, PreviewPlan> PLAN_CACHE = new LinkedHashMap<PlanKey, PreviewPlan>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<PlanKey, PreviewPlan> eldest) {
         return this.size() > 16;
      }
   };

   private BlueprintPipRenderer() {
   }

   public static void render(BCGraphics g, Snapshot snapshot, int x0, int y0, int x1, int y1, float scale) {
      if (snapshot == null) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();
      BlockPos size = snapshot.size;
      int sizeX = Math.max(1, size.getX());
      int sizeY = Math.max(1, size.getY());
      int sizeZ = Math.max(1, size.getZ());
      long gameTime = mc.level != null ? mc.level.getGameTime() : 0L;
      float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float yaw = ((float) (gameTime % 72L) + partialTick) / 72.0F * 360.0F;

      PreviewPlan plan = planFor(snapshot, mc);
      MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

      g.raw.flush();
      g.raw.enableScissor(x0, y0, x1, y1);
      RenderSystem.enableDepthTest();

      PoseStack poseStack = g.raw.pose();
      poseStack.pushPose();
      poseStack.translate((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, 250.0F);
      poseStack.scale(scale, scale, scale);
      // Match the shared PIP model transform (it renders into a texture, so it flips Y/Z); the GUI ortho is
      // y-down, so the net orientation may need a sign tweak after visual testing.
      poseStack.scale(1.0F, -1.0F, -1.0F);
      poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
      poseStack.translate(-sizeX / 2.0F, -sizeY / 2.0F, -sizeZ / 2.0F);

      for (TemplateEntry entry : plan.templateEntries) {
         poseStack.pushPose();
         poseStack.translate(entry.x, entry.y, entry.z);
         Pose pose = poseStack.last();
         VertexConsumer vc = buffers.getBuffer(BCLibRenderTypes.entityTranslucent(SCAN_TEXTURE));
         for (Direction face : entry.faces) {
            ModelUtil.createFace(face, GHOST_CENTER, GHOST_RADIUS, GHOST_UVS).lighti(15, 15).colouri(255, 255, 255, 128).render(pose, vc);
         }
         poseStack.popPose();
      }

      for (PipeEntry entry : plan.pipeEntries) {
         poseStack.pushPose();
         poseStack.translate(entry.x, entry.y, entry.z);
         Pose pipePose = poseStack.last();
         ModelPipe.renderDirect(entry.pipeKey, pipePose, buffers.getBuffer(BCLibRenderTypes.cutoutBlockSheet()), FULL_BRIGHT);
         ModelPipe.renderMaskOverlay(entry.pipeKey, pipePose, buffers.getBuffer(BCLibRenderTypes.translucentBlockSheet()), FULL_BRIGHT, 76);
         poseStack.popPose();
      }

      for (FluidEntry entry : plan.fluidEntries) {
         renderFluidTop(poseStack, buffers, entry);
      }

      Lighting.setupFor3DItems();
      for (ItemEntry entry : plan.itemEntries) {
         poseStack.pushPose();
         poseStack.translate(entry.x + 0.5F, entry.y + 0.5F, entry.z + 0.5F);
         mc.getItemRenderer().renderStatic(entry.stack, ItemDisplayContext.NONE, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, buffers, mc.level, 0);
         poseStack.popPose();
      }

      buffers.endBatch();
      Lighting.setupForFlatItems();
      poseStack.popPose();
      g.raw.disableScissor();
   }

   private static void renderFluidTop(PoseStack poseStack, MultiBufferSource buffers, FluidEntry entry) {
      if (entry.cullTop) {
         return;
      }

      FluidStack stack = new FluidStack(entry.fluidState.getType(), 1);
      BcFluidAppearance appearance = BcFluidAppearanceCache.get(stack);
      if (appearance == null || appearance.sprite() == null) {
         return;
      }

      TextureAtlasSprite sprite = appearance.sprite();
      int tint = appearance.tint();
      float a0 = (tint >>> 24 & 0xFF) / 255.0F;
      float r = (tint >>> 16 & 0xFF) / 255.0F;
      float gg = (tint >>> 8 & 0xFF) / 255.0F;
      float b = (tint & 0xFF) / 255.0F;
      float a = a0 <= 0.0F ? 1.0F : a0;
      float h = entry.fluidState.isSource() ? 1.0F : Math.max(0.125F, entry.fluidState.getOwnHeight());
      poseStack.pushPose();
      poseStack.translate(entry.x, entry.y, entry.z);
      Pose pose = poseStack.last();
      VertexConsumer vc = buffers.getBuffer(BcFluidAppearanceCache.renderType(appearance));
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
         pose, vc, sprite,
         0.0F, h, 0.0F, sprite.getU(0.0F), sprite.getV(0.0F),
         0.0F, h, 1.0F, sprite.getU(0.0F), sprite.getV(1.0F),
         1.0F, h, 1.0F, sprite.getU(1.0F), sprite.getV(1.0F),
         1.0F, h, 0.0F, sprite.getU(1.0F), sprite.getV(0.0F),
         0.0F, 1.0F, 0.0F, r, gg, b, a, FULL_BRIGHT, OverlayTexture.NO_OVERLAY
      );
      poseStack.popPose();
   }

   private static PreviewPlan planFor(Snapshot snapshot, Minecraft mc) {
      return PLAN_CACHE.computeIfAbsent(PlanKey.of(snapshot), ignored -> buildPlan(snapshot, mc));
   }

   private static PreviewPlan buildPlan(Snapshot snapshot, Minecraft mc) {
      PreviewPlan plan = new PreviewPlan();
      BlockPos size = snapshot.size;
      int sizeX = Math.max(1, size.getX());
      int sizeY = Math.max(1, size.getY());
      int sizeZ = Math.max(1, size.getZ());
      Blueprint blueprint = snapshot instanceof Blueprint bp ? bp : null;
      Template template = snapshot instanceof Template tp ? tp : null;
      Map<BlockState, ItemStack> stateCache = new java.util.HashMap<>();
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
                        plan.templateEntries.add(new TemplateEntry(x, y, z, faces));
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
                                 plan.pipeEntries.add(new PipeEntry(x, y, z, pipeKey));
                                 continue;
                              }
                           }

                           FluidState fluidState = state.getFluidState();
                           if (!fluidState.isEmpty()) {
                              Fluid fluid = fluidState.getType();
                              plan.fluidEntries.add(new FluidEntry(x, y, z, fluidState, neighborIsSameFluid(blueprint, size, x, y + 1, z, fluid)));
                           } else if (!shouldCullItemBlock(blueprint, size, x, y, z, state)) {
                              ItemStack stack = stateCache.get(state);
                              if (stack == null) {
                                 stack = new ItemStack(state.getBlock());
                                 stateCache.put(state, stack);
                              }

                              if (!stack.isEmpty()) {
                                 plan.itemEntries.add(new ItemEntry(x, y, z, stack));
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
         }
      }

      return null;
   }

   private static boolean neighborIsSameFluid(Blueprint blueprint, BlockPos size, int nx, int ny, int nz, Fluid fluid) {
      if (nx >= 0 && ny >= 0 && nz >= 0 && nx < size.getX() && ny < size.getY() && nz < size.getZ()) {
         int idx = blueprint.data[Snapshot.posToIndex(size, nx, ny, nz)];
         if (idx >= 0 && idx < blueprint.palette.size()) {
            ISchematicBlock schBlock = blueprint.palette.get(idx);
            if (schBlock != null) {
               BlockState nState = schBlock.getBlockStateForRender();
               if (nState != null) {
                  FluidState nFluid = nState.getFluidState();
                  return !nFluid.isEmpty() && nFluid.is(fluid);
               }
            }
         }
      }

      return false;
   }

   private record ItemEntry(int x, int y, int z, ItemStack stack) {
   }

   private record PipeEntry(int x, int y, int z, PipeModelKey pipeKey) {
   }

   private record FluidEntry(int x, int y, int z, FluidState fluidState, boolean cullTop) {
   }

   private record TemplateEntry(int x, int y, int z, EnumSet<Direction> faces) {
   }

   private record PlanKey(Class<?> type, int sizeX, int sizeY, int sizeZ, int snapshotHash, int hashLength, int identity) {
      static PlanKey of(Snapshot snapshot) {
         BlockPos size = snapshot.size;
         byte[] hash = snapshot.key == null ? null : snapshot.key.hash;
         int hashLength = hash == null ? 0 : hash.length;
         int identity = hashLength == 0 ? System.identityHashCode(snapshot) : 0;
         return new PlanKey(
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
      private final List<ItemEntry> itemEntries = new ArrayList<>();
      private final List<PipeEntry> pipeEntries = new ArrayList<>();
      private final List<FluidEntry> fluidEntries = new ArrayList<>();
      private final List<TemplateEntry> templateEntries = new ArrayList<>();
   }
}
