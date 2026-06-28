/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.fluid.BcFluidBerHelper;
import buildcraft.lib.client.fluid.BcFluidBoxQuads;
import buildcraft.lib.client.fluid.BcFluidRenderLookup;
import buildcraft.lib.fluid.stack.FluidStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 1.21.1 (versions/1.21.1) heat-exchange renderer: immediate-mode port of the shared render-state/submit BER.
 * Draws the input/output tanks (via the version-neutral BcFluidBerHelper BufferSource overload) and the moving
 * coolant/heatant flow through the exchanger middle. Geometry duplicated from the shared RenderHeatExchange,
 * which this override replaces on 1.21.1.
 */
public class RenderHeatExchange implements BlockEntityRenderer<TileHeatExchange> {
   private static final Map<Direction, TankSideData> TANK_SIDES = new EnumMap<>(Direction.class);
   private static final BcFluidBerHelper.TankBounds TANK_BOTTOM = new BcFluidBerHelper.TankBounds(2.0F, 0.0F, 2.0F, 14.0F, 2.0F, 14.0F);
   private static final BcFluidBerHelper.TankBounds TANK_TOP = new BcFluidBerHelper.TankBounds(2.0F, 14.0F, 2.0F, 14.0F, 16.0F, 14.0F);

   public RenderHeatExchange(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public boolean shouldRenderOffScreen(TileHeatExchange tile) {
      return tile.isStart();
   }

   @Override
   public void render(TileHeatExchange tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      if (!tile.isStart()) {
         return;
      }

      Level level = tile.getLevel();
      if (level == null) {
         return;
      }

      if (!(tile.getSection() instanceof TileHeatExchange.ExchangeSectionStart section)) {
         return;
      }

      BlockState blockState = tile.getBlockState();
      if (!(blockState.getBlock() instanceof BlockHeatExchange)) {
         return;
      }

      Direction facing = blockState.getValue(BlockHeatExchange.FACING);
      Direction face = facing.getCounterClockWise();
      TankSideData sideTank = TANK_SIDES.get(face);
      if (sideTank == null) {
         return;
      }

      TileHeatExchange.ExchangeSectionEnd sectionEnd = section.getEndSection();
      if (sectionEnd == null) {
         for (int i = 1; i < 6; i++) {
            BlockEntity neighbor = level.getBlockEntity(tile.getBlockPos().relative(face, i));
            if (neighbor instanceof TileHeatExchange other && other.isEnd()) {
               sectionEnd = (TileHeatExchange.ExchangeSectionEnd)other.getSection();
               break;
            }

            if (!(neighbor instanceof TileHeatExchange)) {
               break;
            }
         }
      }

      BufferSource bufferSource = (BufferSource)buffers;
      poseStack.pushPose();
      BcFluidBerHelper.renderSmoothedFluid(section.smoothedTankInput, TANK_BOTTOM, poseStack, bufferSource, light, partialTick);
      BcFluidBerHelper.renderSmoothedFluid(section.smoothedTankOutput, sideTank.start, poseStack, bufferSource, light, partialTick);
      if (sectionEnd != null) {
         BlockPos diff = sectionEnd.getTile().getBlockPos().subtract(tile.getBlockPos());
         poseStack.translate(diff.getX(), diff.getY(), diff.getZ());
         BcFluidBerHelper.renderSmoothedFluid(sectionEnd.smoothedTankOutput, TANK_TOP, poseStack, bufferSource, light, partialTick);
         BcFluidBerHelper.renderSmoothedFluid(sectionEnd.smoothedTankInput, sideTank.end, poseStack, bufferSource, light, partialTick);
         poseStack.translate(-diff.getX(), -diff.getY(), -diff.getZ());
      }

      int middles = section.middleCount;
      double progress = section.getProgress(partialTick);
      if (middles > 0 && sectionEnd != null && progress > 0.0) {
         double length = middles + 2 - 0.25 - 0.02;
         double p0 = 0.135;
         double p1 = p0 + length - 0.01;
         double progressStart = p0;
         double progressEnd = p0 + length * progress;
         boolean flip = section.getProgressState() == TileHeatExchange.EnumProgressState.PREPARING;
         flip ^= face.getAxisDirection() == AxisDirection.NEGATIVE;
         if (flip) {
            progressStart = p1 - length * progress;
            progressEnd = p1;
         }

         BlockPos diff = BlockPos.ZERO;
         if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
            diff = diff.relative(face, middles + 1);
         }

         double otherStart = flip ? p0 : p1 - length * progress;
         double otherEnd = flip ? p0 + length * progress : p1;
         Vec3 vDiff = Vec3.atLowerCornerOf(diff);
         FluidStack coolantFluid = sectionEnd.smoothedTankInput.getFluid();
         FluidStack heatantFluid = section.smoothedTankInput.getFluid();
         if (!coolantFluid.isEmpty()) {
            renderFlow(vDiff, face, poseStack, buffers, progressStart + 0.01, progressEnd - 0.01, coolantFluid, 4, partialTick, light);
         }

         if (!heatantFluid.isEmpty()) {
            renderFlow(vDiff, face.getOpposite(), poseStack, buffers, otherStart, otherEnd, heatantFluid, 2, partialTick, light);
         }
      }

      poseStack.popPose();
   }

   private static void renderFlow(
      Vec3 diff, Direction face, PoseStack poseStack, MultiBufferSource buffers, double s, double e, FluidStack fluid, int point, float partialTicks, int light
   ) {
      if (fluid.isEmpty()) {
         return;
      }

      BcFluidAppearance appearance = BcFluidAppearanceCache.get(fluid);
      if (appearance == null) {
         return;
      }

      TextureAtlasSprite sprite = appearance.sprite();
      float[] rgba = BcFluidRenderLookup.vertexRgba(fluid);
      float r = rgba[0];
      float g = rgba[1];
      float b = rgba[2];
      float a = rgba[3];
      int overlay = OverlayTexture.NO_OVERLAY;
      Level level = Minecraft.getInstance().level;
      double tickTime = level != null ? level.getGameTime() : 0.0;
      double offset = (tickTime + partialTicks) % 31.0 / 31.0;
      Direction renderFace = face;
      if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
         offset = -offset;
         renderFace = face.getOpposite();
      }

      Vec3 dirVec = Vec3.atLowerCornerOf(renderFace.getUnitVec3i());
      double ds = (point + 0.1) / 16.0;
      float minCross = (float)ds;
      float maxCross = (float)(1.0 - ds);
      diff = diff.subtract(dirVec.scale(offset));
      s += offset;
      e += offset;
      if (s < 0.0) {
         s++;
         e++;
         diff = diff.subtract(dirVec);
      }

      Direction finalRenderFace = renderFace;
      VertexConsumer buffer = buffers.getBuffer(BcFluidAppearanceCache.renderType(appearance));
      poseStack.pushPose();
      Vec3 segmentDiff = diff;

      for (int i = 0; i <= e; i++) {
         if (i < s - 1.0) {
            segmentDiff = segmentDiff.add(dirVec);
         } else {
            poseStack.pushPose();
            poseStack.translate(segmentDiff.x, segmentDiff.y, segmentDiff.z);
            Pose pose = poseStack.last();
            segmentDiff = segmentDiff.add(dirVec);
            double s1 = s < i ? 0.0 : s % 1.0;
            double e1 = e > i + 1 ? 1.0 : e % 1.0;
            float flowMinX = minCross;
            float flowMaxX = maxCross;
            float flowMinY = minCross;
            float flowMaxY = maxCross;
            float flowMinZ = minCross;
            float flowMaxZ = maxCross;
            switch (finalRenderFace.getAxis()) {
               case X:
                  flowMinX = (float)s1;
                  flowMaxX = (float)e1;
                  break;
               case Y:
                  flowMinY = (float)s1;
                  flowMaxY = (float)e1;
                  break;
               case Z:
                  flowMinZ = (float)s1;
                  flowMaxZ = (float)e1;
            }

            boolean[] sides = new boolean[6];
            Arrays.fill(sides, true);
            if (s < i) {
               sides[finalRenderFace.getOpposite().ordinal()] = false;
            }

            if (e > i + 1) {
               sides[finalRenderFace.ordinal()] = false;
            }

            if (sides[Direction.NORTH.ordinal()]) {
               quad(pose, buffer, sprite, flowMinX, flowMaxY, flowMinZ, flowMaxX, flowMaxY, flowMinZ, flowMaxX, flowMinY, flowMinZ, flowMinX, flowMinY, flowMinZ, 0.0F, 0.0F, -1.0F, r, g, b, a, light, overlay);
            }

            if (sides[Direction.SOUTH.ordinal()]) {
               quad(pose, buffer, sprite, flowMinX, flowMinY, flowMaxZ, flowMaxX, flowMinY, flowMaxZ, flowMaxX, flowMaxY, flowMaxZ, flowMinX, flowMaxY, flowMaxZ, 0.0F, 0.0F, 1.0F, r, g, b, a, light, overlay);
            }

            if (sides[Direction.WEST.ordinal()]) {
               quad(pose, buffer, sprite, flowMinX, flowMinY, flowMinZ, flowMinX, flowMinY, flowMaxZ, flowMinX, flowMaxY, flowMaxZ, flowMinX, flowMaxY, flowMinZ, -1.0F, 0.0F, 0.0F, r, g, b, a, light, overlay);
            }

            if (sides[Direction.EAST.ordinal()]) {
               quad(pose, buffer, sprite, flowMaxX, flowMaxY, flowMinZ, flowMaxX, flowMaxY, flowMaxZ, flowMaxX, flowMinY, flowMaxZ, flowMaxX, flowMinY, flowMinZ, 1.0F, 0.0F, 0.0F, r, g, b, a, light, overlay);
            }

            if (sides[Direction.UP.ordinal()]) {
               quadHorizontal(pose, buffer, sprite, flowMinX, flowMaxX, flowMaxZ, flowMinZ, flowMaxY, 0.0F, 1.0F, 0.0F, r, g, b, a, light, overlay);
            }

            if (sides[Direction.DOWN.ordinal()]) {
               quadHorizontal(pose, buffer, sprite, flowMinX, flowMaxX, flowMaxZ, flowMinZ, flowMinY, 0.0F, -1.0F, 0.0F, r, g, b, a, light, overlay);
            }

            poseStack.popPose();
         }
      }

      poseStack.popPose();
   }

   private static void quad(
      Pose pose, VertexConsumer builder, TextureAtlasSprite sprite,
      float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
      float nx, float ny, float nz, float r, float g, float b, float a, int light, int overlay
   ) {
      BcFluidBoxQuads.emitNormalQuad(pose, builder, sprite, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, nx, ny, nz, r, g, b, a, light, overlay);
   }

   private static void quadHorizontal(
      Pose pose, VertexConsumer builder, TextureAtlasSprite sprite, float x1, float x2, float z1, float z2, float y,
      float nx, float ny, float nz, float r, float g, float b, float a, int light, int overlay
   ) {
      BcFluidBoxQuads.emitHorizontal(pose, builder, sprite, x1, x2, z1, z2, y, nx, ny, nz, r, g, b, a, light, overlay);
   }

   static {
      BcFluidBerHelper.TankBounds start = new BcFluidBerHelper.TankBounds(0.0F, 4.0F, 4.0F, 2.0F, 12.0F, 12.0F);
      BcFluidBerHelper.TankBounds end = new BcFluidBerHelper.TankBounds(14.0F, 4.0F, 4.0F, 16.0F, 12.0F, 12.0F);
      TankSideData sides = new TankSideData(start, end);
      Direction face = Direction.EAST;

      for (int i = 0; i < 4; i++) {
         TANK_SIDES.put(face, sides);
         face = face.getClockWise();
         sides = sides.rotateY();
      }
   }

   static class TankSideData {
      final BcFluidBerHelper.TankBounds start;
      final BcFluidBerHelper.TankBounds end;

      TankSideData(BcFluidBerHelper.TankBounds start, BcFluidBerHelper.TankBounds end) {
         this.start = start;
         this.end = end;
      }

      TankSideData rotateY() {
         return new TankSideData(this.start.rotateY(), this.end.rotateY());
      }
   }
}
