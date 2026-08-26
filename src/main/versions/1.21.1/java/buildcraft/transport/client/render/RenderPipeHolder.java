/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.PipeMutableQuadCache;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.WireManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 1.21.1 (versions/1.21.1) pipe block-entity renderer. The shared renderer uses the 1.21.5 render-state +
 * submit pipeline (extractRenderState/submit, SubmitNodeCollector, ItemStackRenderState); here everything is
 * drawn immediately into a MultiBufferSource. The pipe geometry helpers (ModelPipe, PipeWireRenderer, the
 * flow/behaviour/pluggable renderers) are version-neutral (Pose + VertexConsumer); travelling items render
 * through the classic ItemRenderer.renderStatic.
 */
public class RenderPipeHolder implements BlockEntityRenderer<TilePipeHolder> {

   /** Reused per item for the stacked-item jitter — render() runs single-threaded on the render thread. */
   private static final ThreadLocal<Random> MODEL_OFFSET_RANDOM = ThreadLocal.withInitial(() -> new Random(0L));
   private static final Direction[] FACES = Direction.values();
   private final int[] itemIndexScratch = new int[1];
   private final double[] itemPosScratch = new double[3];

   public RenderPipeHolder(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public boolean shouldRender(TilePipeHolder pipe, Vec3 cameraPosition) {
      return hasDynamicContent(pipe) && BlockEntityRenderer.super.shouldRender(pipe, cameraPosition);
   }

   private static boolean hasDynamicContent(TilePipeHolder tile) {
      if (tile.hasPluggables()) {
         return true;
      }

      WireManager wires = tile.getWireManager();
      if (wires != null && (!wires.parts.isEmpty() || !wires.betweens.isEmpty())) {
         return true;
      }

      Pipe pipe = tile.getPipe();
      if (pipe == null) {
         return false;
      }

      if (pipe.behaviour != null && PipeRegistryClient.getBehaviourRenderer(pipe.behaviour) != null) {
         return true;
      }

      PipeFlow flow = pipe.flow;
      if (flow instanceof PipeFlowItems items) {
         return items.doesContainItems();
      } else if (flow instanceof PipeFlowFluids fluids) {
         FluidStack forRender = fluids.getFluidStackForRender();
         return forRender != null && !forRender.isEmpty();
      } else {
         return flow != null && PipeRegistryClient.getFlowRenderer(flow) != null;
      }
   }

   @Override
   public void render(TilePipeHolder pipe, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      Level level = pipe.getLevel();
      if (level == null) {
         return;
      }
      poseStack.pushPose();

      Pose cutoutPose = poseStack.last();
      VertexConsumer cutout = buffers.getBuffer(BCLibRenderTypes.cutoutBlockSheet());
      ModelPipe.renderCutoutPluggables(pipe, cutoutPose, cutout, light);
      PipeWireRenderer.renderWires(pipe, cutoutPose, light, cutout);

      // Translucent pluggable layer (e.g. coloured lens glass) — drawn on the normal depth-sorted translucent
      // block sheet, so the glass occludes correctly. Without this the lens glass was baked but never
      // submitted, so it showed nothing in-world.
      VertexConsumer translucentPluggables = buffers.getBuffer(BCLibRenderTypes.translucentBlockSheet());
      ModelPipe.renderTranslucentPluggables(pipe, cutoutPose, translucentPluggables, light);

      renderItems(pipe, partialTick, poseStack, buffers, light, level);

      renderContents(pipe, partialTick, poseStack, buffers, light);

      poseStack.popPose();
   }

   private void renderItems(TilePipeHolder pipe, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, Level level) {
      Pipe p = pipe.getPipe();
      if (p == null || !(p.flow instanceof PipeFlowItems flowItems)) {
         return;
      }
      long now = level.getGameTime();
      int posHash = (int) pipe.getBlockPos().asLong();
      int[] idx = this.itemIndexScratch;
      idx[0] = 0;
      double[] posScratch = this.itemPosScratch;
      flowItems.forEachItemForRender(item -> {
         int i = idx[0]++;
         ItemStack stack = item.clientItemLink.get();
         if (stack == null || stack.isEmpty()) {
            stack = item.getStack();
         }
         if (stack == null || stack.isEmpty()) {
            return;
         }
         item.writeRenderPosition(BlockPos.ZERO, now, partialTick, flowItems, posScratch);
         Direction dir = item.getRenderDirection(now, partialTick);
         int count = item.stackSize > 0 ? item.stackSize : stack.getCount();
         int models = count > 1 ? 2 : 1;
         Random offset = MODEL_OFFSET_RANDOM.get();
         offset.setSeed(count & 2147483647L);
         for (int m = 0; m < models; m++) {
            poseStack.pushPose();
            float dx = 0.0F;
            float dy = 0.0F;
            float dz = 0.0F;
            if (m > 0) {
               dx = (offset.nextFloat() * 2.0F - 1.0F) * 0.08F;
               dy = (offset.nextFloat() * 2.0F - 1.0F) * 0.08F;
               dz = (offset.nextFloat() * 2.0F - 1.0F) * 0.08F;
            }
            poseStack.translate(posScratch[0] + dx, posScratch[1] + dy, posScratch[2] + dz);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            applyDirectionRotation(poseStack, dir != null ? dir : Direction.EAST);
            Minecraft.getInstance()
               .getItemRenderer()
               .renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, poseStack, buffers, level, posHash + i);
            poseStack.popPose();
         }
         if (item.colour != null) {
            poseStack.pushPose();
            poseStack.translate(posScratch[0], posScratch[1], posScratch[2]);
            VertexConsumer cb = buffers.getBuffer(BCLibRenderTypes.cutoutBlockSheet());
            renderColourOverlayQuads(poseStack.last(), cb, item.colour, light);
            poseStack.popPose();
         }
      });
   }

   private static void renderColourOverlayQuads(Pose pose, VertexConsumer buffer, DyeColor colour, int light) {
      MutableQuad[] colourQuads = PipeItemColourQuads.get(colour);
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
            break;
         case SOUTH:
         default:
            break;
      }
   }

   private static void renderContents(TilePipeHolder pipe, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
      Pipe p = pipe.getPipe();
      if (p == null) {
         return;
      }
      PipeRenderContext.setPackedLight(light);
      IPipeBehaviourRenderer<PipeBehaviour> behaviourRenderer = p.behaviour != null ? PipeRegistryClient.getBehaviourRenderer(p.behaviour) : null;
      boolean hasPluggables = false;
      for (Direction facing : FACES) {
         PipePluggable plug = pipe.getPluggable(facing);
         if (plug != null && PipeRegistryClient.getPlugRenderer(plug) != null) {
            hasPluggables = true;
            break;
         }
      }

      if (behaviourRenderer != null || hasPluggables) {
         VertexConsumer buffer = buffers.getBuffer(BCLibRenderTypes.cutoutBlockSheet());
         if (behaviourRenderer != null) {
            behaviourRenderer.render(p.behaviour, 0.0, 0.0, 0.0, partialTicks, buffer, poseStack.last());
         }
         for (Direction facing : FACES) {
            PipePluggable plug = pipe.getPluggable(facing);
            if (plug != null) {
               renderPluggable(plug, 0.0, 0.0, 0.0, partialTicks, buffer, poseStack);
            }
         }
      }

      if (p.flow != null && !(p.flow instanceof PipeFlowItems) && PipeRegistryClient.getFlowRenderer(p.flow) != null) {
         RenderType flowType = BCLibRenderTypes.cutoutBlockSheet();
         if (p.flow instanceof PipeFlowFluids fluids) {
            PipeFlowRendererFluids.prepareRenderCache(fluids);
            BcFluidAppearance appearance = fluids.renderCacheAppearance;
            if (appearance != null) {
               flowType = BcFluidAppearanceCache.renderType(appearance);
            }
         }
         VertexConsumer buffer = buffers.getBuffer(flowType);
         renderFlow(p.flow, 0.0, 0.0, 0.0, partialTicks, buffer, poseStack.last());
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
}
