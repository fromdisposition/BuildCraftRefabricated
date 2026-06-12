/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.tile.BcBerRenderUtil;
import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.PipeMutableQuadCache;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.tile.TilePipeHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderPipeHolder implements BlockEntityRenderer<TilePipeHolder, PipeHolderRenderState> {
   private final ItemModelResolver itemModelResolver;
   private long itemModelCacheTick = Long.MIN_VALUE;
   private final Map<Integer, ItemStackRenderState> itemModelBySignature = new HashMap<>();

   public RenderPipeHolder(Context context) {
      this.itemModelResolver = context.itemModelResolver();
   }

   private ItemStackRenderState resolveItemModel(PipeHolderRenderState renderState, ItemStack stack, Level world, int seed, long tick) {
      if (tick != this.itemModelCacheTick) {
         this.itemModelBySignature.clear();
         this.itemModelCacheTick = tick;
      }

      int signature = ItemStack.hashItemAndComponents(stack);
      ItemStackRenderState cached = this.itemModelBySignature.get(signature);
      if (cached != null) {
         return cached;
      }

      ItemStackRenderState itemState = renderState.acquireItemState();
      this.itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.NONE, world, null, seed);
      this.itemModelBySignature.put(signature, itemState);
      return itemState;
   }

   public PipeHolderRenderState createRenderState() {
      return new PipeHolderRenderState();
   }

   public void extractRenderState(
      TilePipeHolder blockEntity, PipeHolderRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay
   ) {
      BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
      renderState.pipe = blockEntity;
      renderState.partialTick = partialTick;
      renderState.beginItemExtraction();
      Pipe pipe = blockEntity.getPipe();
      if (pipe != null && pipe.flow instanceof PipeFlowItems flowItems) {
         Level world = blockEntity.getLevel();
         if (world != null) {
            long now = world.getGameTime();
            int posHash = (int)blockEntity.getBlockPos().asLong();
            int[] itemIndex = new int[]{0};
            double[] posScratch = new double[3];
            flowItems.forEachItemForRender(
               item -> {
                  int i = (int)(itemIndex[0]++);
                  ItemStack stack = item.clientItemLink.get();
                  if (stack == null || stack.isEmpty()) {
                     stack = item.getStack();
                  }

                  if (stack != null && !stack.isEmpty()) {
                     ItemStackRenderState itemState = this.resolveItemModel(renderState, stack, world, posHash + i, now);
                     if (!itemState.isEmpty()) {
                        item.writeRenderPosition(BlockPos.ZERO, now, partialTick, flowItems, posScratch);
                        Direction dir = item.getRenderDirection(now, partialTick);
                        int count = item.stackSize > 0 ? item.stackSize : stack.getCount();
                        renderState.itemEntries
                           .add(new PipeHolderRenderState.ItemRenderEntry(itemState, posScratch[0], posScratch[1], posScratch[2], dir, item.colour, count));
                     }
                  }
               }
            );
         }
      }
   }

   public void submit(PipeHolderRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      TilePipeHolder pipe = renderState.pipe;
      if (pipe != null) {
         Level level = pipe.getLevel();
         if (level != null) {
            int light = renderState.lightCoords;
            poseStack.pushPose();
            BcBerRenderUtil.submit(
               poseStack, collector, BCLibRenderTypes.cutoutBlockSheet(), (pose, buffer) -> {
                  ModelPipe.renderDirect(pipe, pose, buffer, light);
                  ModelPipe.renderCutoutPluggables(pipe, pose, buffer, light);
                  PipeWireRenderer.renderWires(pipe, pose, light, buffer);
               }
            );
            Pipe bodyPipe = pipe.getPipe();
            if (bodyPipe != null && bodyPipe.getColour() != null) {
               int paintAlpha = bodyPipe.definition.flowType == PipeApi.flowFluids ? 255 : ModelPipe.PIPE_PAINT_ALPHA;
               BcBerRenderUtil.submit(
                  poseStack, collector, BCLibRenderTypes.translucentBlockSheet(), (pose, buffer) -> {
                     ModelPipe.renderMaskOverlay(pipe, pose, buffer, light, paintAlpha);
                  }
               );
            }
            submitItems(renderState, poseStack, collector, light);
            renderContents(pipe, 0.0, 0.0, 0.0, renderState.partialTick, poseStack, collector, light);
            poseStack.popPose();
         }
      }
   }

   private static void submitItems(PipeHolderRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, int light) {
      if (!renderState.itemEntries.isEmpty()) {
         Random modelOffsetRandom = new Random(0L);

         for (PipeHolderRenderState.ItemRenderEntry entry : renderState.itemEntries) {
            if (!entry.renderState.isEmpty()) {
               Direction dir = entry.direction != null ? entry.direction : Direction.EAST;
               int itemModelCount = getStackModelCount(entry.stackCount);
               if (itemModelCount > 1) {
                  setupModelOffsetRandom(modelOffsetRandom, entry.stackCount);
               }

               for (int i = 0; i < itemModelCount; i++) {
                  poseStack.pushPose();
                  float dx = 0.0F;
                  float dy = 0.0F;
                  float dz = 0.0F;
                  if (i > 0) {
                     dx = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                     dy = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                     dz = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                  }

                  poseStack.translate(entry.posX + dx, entry.posY + dy, entry.posZ + dz);
                  poseStack.scale(0.3F, 0.3F, 0.3F);
                  applyDirectionRotation(poseStack, dir);
                  entry.renderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                  poseStack.popPose();
               }

               if (entry.colour != null) {
                  poseStack.pushPose();
                  poseStack.translate(entry.posX, entry.posY, entry.posZ);
                  BcBerRenderUtil.submit(poseStack, collector, BCLibRenderTypes.cutoutBlockSheet(), (pose, buffer) -> {
                     renderColourOverlayQuads(pose, buffer, entry, light);
                  });
                  poseStack.popPose();
               }
            }
         }
      }
   }

   private static void renderColourOverlayQuads(Pose pose, VertexConsumer buffer, PipeHolderRenderState.ItemRenderEntry entry, int light) {
      MutableQuad[] colourQuads = PipeItemColourQuads.get(entry.colour);
      if (colourQuads != null) {
         MutableQuad scratch = PipeMutableQuadCache.renderScratch();

         for (MutableQuad template : colourQuads) {
            if (template != null) {
               scratch.copyFrom(template);
               scratch.lighti(light);
               scratch.render(pose, buffer);
            }
         }
      }
   }

   private static void applyDirectionRotation(PoseStack ps, Direction dir) {
      switch (dir) {
         case NORTH:
            ps.mulPose(Axis.YP.rotationDegrees(180.0F));
            break;
         case EAST:
            ps.mulPose(Axis.YP.rotationDegrees(90.0F));
            break;
         case WEST:
            ps.mulPose(Axis.YP.rotationDegrees(-90.0F));
            break;
         case UP:
            ps.mulPose(Axis.XP.rotationDegrees(-90.0F));
            break;
         case DOWN:
            ps.mulPose(Axis.XP.rotationDegrees(90.0F));
         case SOUTH:
      }
   }

   private static void setupModelOffsetRandom(Random random, int stackCount) {
      random.setSeed(stackCount & 2147483647L);
   }

   private static int getStackModelCount(int stackCount) {
      return stackCount > 1 ? 2 : 1;
   }

   private static void renderContents(
      TilePipeHolder pipe, double x, double y, double z, float partialTicks, PoseStack poseStack, SubmitNodeCollector collector, int light
   ) {
      Pipe p = pipe.getPipe();
      if (p != null) {
         PipeRenderContext.setPackedLight(light);
         boolean hasBehaviour = p.behaviour != null;
         boolean hasPluggables = false;

         for (Direction facing : Direction.values()) {
            if (pipe.getPluggable(facing) != null) {
               hasPluggables = true;
               break;
            }
         }

         if (hasBehaviour || hasPluggables) {
            BcBerRenderUtil.submitWithPoseStack(poseStack, collector, BCLibRenderTypes.cutoutBlockSheet(), (stack, buffer) -> {
               if (hasBehaviour) {
                  renderBehaviour(p.behaviour, x, y, z, partialTicks, buffer, stack.last());
               }

               for (Direction facing : Direction.values()) {
                  PipePluggable plug = pipe.getPluggable(facing);
                  if (plug != null) {
                     renderPluggable(plug, x, y, z, partialTicks, buffer, stack);
                  }
               }
            });
         }

         if (p.flow != null && !(p.flow instanceof PipeFlowItems)) {
            RenderType flowType = BCLibRenderTypes.cutoutBlockSheet();
            if (p.flow instanceof PipeFlowFluids fluids) {
               PipeFlowRendererFluids.prepareRenderCache(fluids);
               BcFluidAppearance appearance = fluids.renderCacheAppearance;
               if (appearance != null) {
                  flowType = BcFluidAppearanceCache.renderType(appearance);
               }
            }

            BcBerRenderUtil.submit(poseStack, collector, flowType, (pose, buffer) -> {
               renderFlow(p.flow, x, y, z, partialTicks, buffer, pose);
            });
         }
      }
   }

   private static <P extends PipePluggable> void renderPluggable(
      P plug, double x, double y, double z, float partialTicks, VertexConsumer plugBuffer, PoseStack poseStack
   ) {
      IPlugDynamicRenderer<P> renderer = PipeRegistryClient.getPlugRenderer(plug);
      if (renderer != null) {
         renderer.render(plug, x, y, z, partialTicks, plugBuffer, poseStack);
      }
   }

   private static <F extends PipeFlow> void renderFlow(F flow, double x, double y, double z, float partialTicks, VertexConsumer buffer, Pose pose) {
      IPipeFlowRenderer<F> renderer = PipeRegistryClient.getFlowRenderer(flow);
      if (renderer != null) {
         renderer.render(flow, x, y, z, partialTicks, buffer, pose);
      }
   }

   private static <B extends PipeBehaviour> void renderBehaviour(
      B behaviour, double x, double y, double z, float partialTicks, VertexConsumer buffer, Pose pose
   ) {
      IPipeBehaviourRenderer<B> renderer = PipeRegistryClient.getBehaviourRenderer(behaviour);
      if (renderer != null) {
         renderer.render(behaviour, x, y, z, partialTicks, buffer, pose);
      }
   }
}
