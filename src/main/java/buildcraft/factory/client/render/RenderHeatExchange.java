/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.client.fluid.BcFluidBerHelper;
import buildcraft.lib.client.fluid.BcFluidQuadEmitter;
import buildcraft.lib.client.fluid.FluidClientCache;
import buildcraft.lib.client.render.tile.BcBerRenderUtil;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RenderHeatExchange extends BcBlockEntityRenderer<TileHeatExchange, HeatExchangeRenderState> {
   private static final Map<Direction, RenderHeatExchange.TankSideData> TANK_SIDES = new EnumMap<>(Direction.class);
   private static final BcFluidBerHelper.TankBounds TANK_BOTTOM = new BcFluidBerHelper.TankBounds(2.0F, 0.0F, 2.0F, 14.0F, 2.0F, 14.0F);
   private static final BcFluidBerHelper.TankBounds TANK_TOP = new BcFluidBerHelper.TankBounds(2.0F, 14.0F, 2.0F, 14.0F, 16.0F, 14.0F);

   public RenderHeatExchange(Context context) {
   }

   public HeatExchangeRenderState createRenderState() {
      return new HeatExchangeRenderState();
   }

   @Override
   protected void extract(TileHeatExchange tile, HeatExchangeRenderState state, float partialTick) {
      state.render = false;
      if (!tile.isStart()) {
         return;
      }

      Level level = tile.getLevel();
      if (level == null) {
         return;
      }

      TileHeatExchange.ExchangeSectionStart section = (TileHeatExchange.ExchangeSectionStart)tile.getSection();
      if (section == null) {
         return;
      }

      BlockState blockState = tile.getBlockState();
      if (!(blockState.getBlock() instanceof BlockHeatExchange)) {
         return;
      }

      Direction facing = (Direction)blockState.getValue(BlockHeatExchange.FACING);
      Direction face = facing.getCounterClockWise();
      if (TANK_SIDES.get(face) == null) {
         return;
      }

      TileHeatExchange.ExchangeSectionEnd sectionEnd = section.getEndSection();
      if (sectionEnd == null) {
         Direction dir = face;

         for (int i = 1; i < 6; i++) {
            BlockEntity neighbor = level.getBlockEntity(state.blockPos.relative(dir, i));
            if (neighbor instanceof TileHeatExchange other && other.isEnd()) {
               sectionEnd = (TileHeatExchange.ExchangeSectionEnd)other.getSection();
               break;
            }

            if (!(neighbor instanceof TileHeatExchange)) {
               break;
            }
         }
      }

      state.render = true;
      state.section = section;
      state.sectionEnd = sectionEnd;
      state.face = face;
      state.middleCount = section.middleCount;
      state.progressState = section.getProgressState();
      state.progress = section.getProgress(partialTick);
      state.endDiff = sectionEnd != null ? sectionEnd.getTile().getBlockPos().subtract(tile.getBlockPos()) : BlockPos.ZERO;
      state.coolantFluid = sectionEnd != null ? sectionEnd.smoothedTankInput.getFluid() : FluidStack.EMPTY;
      state.heatantFluid = section.smoothedTankInput.getFluid();
   }

   public void submit(HeatExchangeRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      if (!renderState.render || renderState.section == null) {
         return;
      }

      RenderHeatExchange.TankSideData sideTank = TANK_SIDES.get(renderState.face);
      if (sideTank == null) {
         return;
      }

      TileHeatExchange.ExchangeSectionStart section = renderState.section;
      TileHeatExchange.ExchangeSectionEnd sectionEnd = renderState.sectionEnd;
      int light = renderState.light;
      poseStack.pushPose();
      float partialTicks = renderState.partialTick;
      BcFluidBerHelper.renderSmoothedFluid(section.smoothedTankInput, TANK_BOTTOM, poseStack, collector, light, partialTicks);
      BcFluidBerHelper.renderSmoothedFluid(section.smoothedTankOutput, sideTank.start, poseStack, collector, light, partialTicks);
      if (sectionEnd != null) {
         BlockPos diff = renderState.endDiff;
         poseStack.translate(diff.getX(), diff.getY(), diff.getZ());
         BcFluidBerHelper.renderSmoothedFluid(sectionEnd.smoothedTankOutput, TANK_TOP, poseStack, collector, light, partialTicks);
         BcFluidBerHelper.renderSmoothedFluid(sectionEnd.smoothedTankInput, sideTank.end, poseStack, collector, light, partialTicks);
         poseStack.translate(-diff.getX(), -diff.getY(), -diff.getZ());
      }

      int middles = renderState.middleCount;
      if (middles > 0 && sectionEnd != null && renderState.progress > 0.0) {
         double length = middles + 2 - 0.25 - 0.02;
         double p0 = 0.135;
         double p1 = p0 + length - 0.01;
         double progressStart = p0;
         double progressEnd = p0 + length * renderState.progress;
         boolean flip = renderState.progressState == TileHeatExchange.EnumProgressState.PREPARING;
         flip ^= renderState.face.getAxisDirection() == AxisDirection.NEGATIVE;
         if (flip) {
            progressStart = p1 - length * renderState.progress;
            progressEnd = p1;
         }

         BlockPos diff = BlockPos.ZERO;
         if (renderState.face.getAxisDirection() == AxisDirection.NEGATIVE) {
            diff = diff.relative(renderState.face, middles + 1);
         }

         double otherStart = flip ? p0 : p1 - length * renderState.progress;
         double otherEnd = flip ? p0 + length * renderState.progress : p1;
         Vec3 vDiff = Vec3.atLowerCornerOf(diff);
         FluidStack coolantFluid = renderState.coolantFluid;
         FluidStack heatantFluid = renderState.heatantFluid;
         if (!coolantFluid.isEmpty()) {
            renderFlow(vDiff, renderState.face, poseStack, collector, progressStart + 0.01, progressEnd - 0.01, coolantFluid, 4, partialTicks, light);
         }

         if (!heatantFluid.isEmpty()) {
            renderFlow(vDiff, renderState.face.getOpposite(), poseStack, collector, otherStart, otherEnd, heatantFluid, 2, partialTicks, light);
         }
      }

      poseStack.popPose();
   }

   public boolean shouldRender(TileHeatExchange blockEntity, Vec3 cameraPos) {
      return blockEntity.isStart();
   }

   private static void renderFlow(
      Vec3 diff, Direction face, PoseStack poseStack, SubmitNodeCollector collector, double s, double e, FluidStack fluid, int point, float partialTicks, int light
   ) {
      if (!fluid.isEmpty()) {
         FluidClientCache.Appearance appearance = FluidClientCache.get(fluid);
         if (appearance != null) {
            TextureAtlasSprite sprite = appearance.sprite();
            float[] rgba = FluidUtilBC.vertexRgba(fluid);
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

            Vec3 flowDiff = diff;
            double flowS = s;
            double flowE = e;
            Direction finalRenderFace = renderFace;
            BcBerRenderUtil.submitWithPoseStack(poseStack, collector, FluidClientCache.renderType(appearance), (stack, buffer) -> {
               Vec3 segmentDiff = flowDiff;

               for (int i = 0; i <= flowE; i++) {
                  if (i < flowS - 1.0) {
                     segmentDiff = segmentDiff.add(dirVec);
                  } else {
                     stack.pushPose();
                     stack.translate(segmentDiff.x, segmentDiff.y, segmentDiff.z);
                     Pose pose = stack.last();
                     segmentDiff = segmentDiff.add(dirVec);
                     double s1 = flowS < i ? 0.0 : flowS % 1.0;
                     double e1 = flowE > i + 1 ? 1.0 : flowE % 1.0;
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
                  if (flowS < i) {
                     sides[finalRenderFace.getOpposite().ordinal()] = false;
                  }

                  if (flowE > i + 1) {
                     sides[finalRenderFace.ordinal()] = false;
                  }

                  if (sides[Direction.NORTH.ordinal()]) {
                     quad(
                        pose,
                        buffer,
                        sprite,
                        flowMinX,
                        flowMaxY,
                        flowMinZ,
                        flowMaxX,
                        flowMaxY,
                        flowMinZ,
                        flowMaxX,
                        flowMinY,
                        flowMinZ,
                        flowMinX,
                        flowMinY,
                        flowMinZ,
                        0.0F,
                        0.0F,
                        -1.0F,
                        r,
                        g,
                        b,
                        a,
                        light,
                        overlay
                     );
                  }

                  if (sides[Direction.SOUTH.ordinal()]) {
                     quad(
                        pose,
                        buffer,
                        sprite,
                        flowMinX,
                        flowMinY,
                        flowMaxZ,
                        flowMaxX,
                        flowMinY,
                        flowMaxZ,
                        flowMaxX,
                        flowMaxY,
                        flowMaxZ,
                        flowMinX,
                        flowMaxY,
                        flowMaxZ,
                        0.0F,
                        0.0F,
                        1.0F,
                        r,
                        g,
                        b,
                        a,
                        light,
                        overlay
                     );
                  }

                  if (sides[Direction.WEST.ordinal()]) {
                     quad(
                        pose,
                        buffer,
                        sprite,
                        flowMinX,
                        flowMinY,
                        flowMinZ,
                        flowMinX,
                        flowMinY,
                        flowMaxZ,
                        flowMinX,
                        flowMaxY,
                        flowMaxZ,
                        flowMinX,
                        flowMaxY,
                        flowMinZ,
                        -1.0F,
                        0.0F,
                        0.0F,
                        r,
                        g,
                        b,
                        a,
                        light,
                        overlay
                     );
                  }

                  if (sides[Direction.EAST.ordinal()]) {
                     quad(
                        pose,
                        buffer,
                        sprite,
                        flowMaxX,
                        flowMaxY,
                        flowMinZ,
                        flowMaxX,
                        flowMaxY,
                        flowMaxZ,
                        flowMaxX,
                        flowMinY,
                        flowMaxZ,
                        flowMaxX,
                        flowMinY,
                        flowMinZ,
                        1.0F,
                        0.0F,
                        0.0F,
                        r,
                        g,
                        b,
                        a,
                        light,
                        overlay
                     );
                  }

                  if (sides[Direction.UP.ordinal()]) {
                     quadHorizontal(pose, buffer, sprite, flowMinX, flowMaxX, flowMaxZ, flowMinZ, flowMaxY, 0.0F, 1.0F, 0.0F, r, g, b, a, light, overlay);
                  }

                  if (sides[Direction.DOWN.ordinal()]) {
                     quadHorizontal(pose, buffer, sprite, flowMinX, flowMaxX, flowMaxZ, flowMinZ, flowMinY, 0.0F, -1.0F, 0.0F, r, g, b, a, light, overlay);
                  }

                     stack.popPose();
                  }
               }
            });
         }
      }
   }

   private static void quad(
      Pose pose,
      VertexConsumer builder,
      TextureAtlasSprite sprite,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float nx,
      float ny,
      float nz,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      BcFluidQuadEmitter.emitTankQuad(pose, builder, sprite, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, nx, ny, nz, r, g, b, a, light, overlay);
   }

   private static void quadHorizontal(
      Pose pose,
      VertexConsumer builder,
      TextureAtlasSprite sprite,
      float x1,
      float x2,
      float z1,
      float z2,
      float y,
      float nx,
      float ny,
      float nz,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      BcFluidQuadEmitter.emitTankHorizontal(pose, builder, sprite, x1, x2, z1, z2, y, nx, ny, nz, r, g, b, a, light, overlay);
   }

   static {
      BcFluidBerHelper.TankBounds start = new BcFluidBerHelper.TankBounds(0.0F, 4.0F, 4.0F, 2.0F, 12.0F, 12.0F);
      BcFluidBerHelper.TankBounds end = new BcFluidBerHelper.TankBounds(14.0F, 4.0F, 4.0F, 16.0F, 12.0F, 12.0F);
      RenderHeatExchange.TankSideData sides = new RenderHeatExchange.TankSideData(start, end);
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

      RenderHeatExchange.TankSideData rotateY() {
         return new RenderHeatExchange.TankSideData(this.start.rotateY(), this.end.rotateY());
      }
   }
}
