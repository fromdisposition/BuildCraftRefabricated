/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;


import buildcraft.lib.fluid.meta.FluidAttributes;
import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.fluid.BcFluidPipeQuads;
import buildcraft.lib.client.fluid.BcFluidRenderLookup;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.material.Fluid;

public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
   INSTANCE;

   private static final double ARM_RADIUS = 0.24;
   private static final double CENTER_INSET = 0.26;
   private static final double CENTER_OUTSET = 0.74;
   private static final double ARM_AXIS_PAD = 0.005;
   private static final double FACE_OVERLAP = 0.002;

   private static final ThreadLocal<double[]> SCRATCH_AMOUNTS = ThreadLocal.withInitial(() -> new double[7]);
   private static final ThreadLocal<double[]> SCRATCH_BOUNDS = ThreadLocal.withInitial(() -> new double[6]);

   @Override
   public void render(PipeFlowFluids flow, double x, double y, double z, float partialTicks, VertexConsumer bb, Pose pose) {
      FluidStack forRender = flow.getFluidStackForRender();
      if (forRender != null && !forRender.isEmpty() && bb != null) {
         ensureRenderCache(flow, forRender);
         TextureAtlasSprite sprite = flow.renderCacheSprite;
         if (sprite != null) {
            float[] rgba = flow.renderCacheRgba;
            int packedLight = PipeRenderContext.getPackedLight();
            double[] amounts = SCRATCH_AMOUNTS.get();
            flow.writeAmountsForRender(partialTicks, amounts);
            boolean gas = FluidAttributes.of(forRender.getFluid()).isLighterThanAir();
            boolean horizontal = false;
            boolean vertical = flow.pipe.isConnected(gas ? Direction.DOWN : Direction.UP);
            int centerIdx = EnumPipePart.CENTER.getIndex();

            for (Direction face : Direction.values()) {
               int fi = face.ordinal();
               double amount = amounts[fi];
               if (face.getAxis() != Axis.Y) {
                  horizontal |= flow.pipe.isConnected(face) && amount > 0.0;
               }

               if (amount > 0.0) {
                  double size = ((Pipe)flow.pipe).getConnectedDist(face);
                  double[] bounds = armBounds(face, size, amount, flow.capacity);
                  int faceSkipMask = amounts[centerIdx] > 0.0 ? 1 << face.getOpposite().ordinal() : 0;
                  double cuboidAmount = face.getAxis() == Axis.Y ? 1.0 : amount;
                  double cuboidCapacity = face.getAxis() == Axis.Y ? 1.0 : flow.capacity;
                  renderFluidCuboid(
                     pose,
                     sprite,
                     bounds[0],
                     bounds[1],
                     bounds[2],
                     bounds[3],
                     bounds[4],
                     bounds[5],
                     cuboidAmount,
                     cuboidCapacity,
                     gas,
                     faceSkipMask,
                     rgba,
                     packedLight,
                     bb
                  );
               }
            }

            double centerAmount = amounts[centerIdx];
            boolean renderedHorizCenter = false;
            double horizPos = CENTER_INSET;
            if (horizontal || !vertical) {
               renderFluidCuboid(
                  pose,
                  sprite,
                  CENTER_INSET,
                  CENTER_INSET,
                  CENTER_INSET,
                  CENTER_OUTSET,
                  CENTER_OUTSET,
                  CENTER_OUTSET,
                  centerAmount,
                  flow.capacity,
                  gas,
                  0,
                  rgba,
                  packedLight,
                  bb
               );
               horizPos += 0.48 * centerAmount / flow.capacity;
               renderedHorizCenter = true;
            }

            if (vertical && horizPos < CENTER_OUTSET) {
               double perc = Math.sqrt(centerAmount / flow.capacity);
               double minXZ = 0.5 - ARM_RADIUS * perc;
               double maxXZ = 0.5 + ARM_RADIUS * perc;
               double yMin = gas ? CENTER_INSET : horizPos;
               double yMax = gas ? 1.0 - horizPos : CENTER_OUTSET;
               int pillarSkipMask = renderedHorizCenter ? 1 << (gas ? Direction.UP.ordinal() : Direction.DOWN.ordinal()) : 0;
               renderFluidCuboid(
                  pose, sprite, minXZ, yMin, minXZ, maxXZ, yMax, maxXZ, 1.0, 1.0, gas, pillarSkipMask, rgba, packedLight, bb
               );
            }
         }
      }
   }

   private static double[] armBounds(Direction face, double size, double amount, double capacity) {
      double shift = ARM_RADIUS + size / 2.0;
      double cx = 0.5 + face.getStepX() * shift;
      double cy = 0.5 + face.getStepY() * shift;
      double cz = 0.5 + face.getStepZ() * shift;
      double rx = ARM_RADIUS;
      double ry = ARM_RADIUS;
      double rz = ARM_RADIUS;
      double axisHalf = ARM_AXIS_PAD + size / 2.0;
      switch (face.getAxis()) {
         case X:
            rx = axisHalf;
            break;
         case Y:
            ry = axisHalf;
            break;
         case Z:
            rz = axisHalf;
      }

      if (face.getAxis() == Axis.Y) {
         double perc = Math.sqrt(amount / capacity);
         rx = perc * ARM_RADIUS;
         rz = perc * ARM_RADIUS;
      }

      double[] bounds = SCRATCH_BOUNDS.get();
      bounds[0] = cx - rx;
      bounds[1] = cy - ry;
      bounds[2] = cz - rz;
      bounds[3] = cx + rx;
      bounds[4] = cy + ry;
      bounds[5] = cz + rz;
      extendArmToBlockFace(face, bounds);
      return bounds;
   }

   private static void extendArmToBlockFace(Direction face, double[] bounds) {
      double overlap = face.getAxisDirection() == AxisDirection.POSITIVE ? FACE_OVERLAP : -FACE_OVERLAP;
      switch (face) {
         case EAST:
            bounds[3] = Math.max(bounds[3], 1.0 + overlap);
            break;
         case WEST:
            bounds[0] = Math.min(bounds[0], -overlap);
            break;
         case UP:
            bounds[4] = Math.max(bounds[4], 1.0 + overlap);
            break;
         case DOWN:
            bounds[1] = Math.min(bounds[1], -overlap);
            break;
         case SOUTH:
            bounds[5] = Math.max(bounds[5], 1.0 + overlap);
            break;
         case NORTH:
            bounds[2] = Math.min(bounds[2], -overlap);
            break;
         default:
            break;
      }
   }

   public static void prepareRenderCache(PipeFlowFluids flow) {
      FluidStack forRender = flow.getFluidStackForRender();
      if (forRender != null && !forRender.isEmpty()) {
         ensureRenderCache(flow, forRender);
      }
   }

   private static void ensureRenderCache(PipeFlowFluids flow, FluidStack fluidStack) {
      Fluid current = fluidStack.getFluid();
      if (current != flow.renderCacheFluid) {
         flow.renderCacheFluid = current;
         BcFluidAppearance appearance = BcFluidAppearanceCache.get(fluidStack);
         flow.renderCacheAppearance = appearance;
         if (appearance != null) {
            flow.renderCacheSprite = appearance.sprite();
            flow.renderCacheTranslucent = appearance.translucent();
         } else {
            flow.renderCacheSprite = BcFluidRenderLookup.sprite(fluidStack, BcFluidRenderLookup.SpriteKind.STILL);
            flow.renderCacheTranslucent = BcFluidRenderLookup.translucent(fluidStack);
         }

         BcFluidRenderLookup.writeVertexRgba(fluidStack, flow.renderCacheRgba);
      }
   }

   private static void renderFluidCuboid(
      Pose pose,
      TextureAtlasSprite sprite,
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      double amount,
      double capacity,
      boolean gas,
      int skipFaceMask,
      float[] rgba,
      int packedLight,
      VertexConsumer bb
   ) {
      if (amount <= 0.0 || capacity <= 0.0) {
         return;
      }

      double height = Math.min(amount / capacity, 1.0);
      float realMinX = (float)minX;
      float realMinZ = (float)minZ;
      float realMaxX = (float)maxX;
      float realMaxZ = (float)maxZ;
      float realMinY;
      float realMaxY;
      if (gas) {
         realMaxY = (float)maxY;
         realMinY = (float)(maxY - (maxY - minY) * height);
      } else {
         realMinY = (float)minY;
         realMaxY = (float)(minY + (maxY - minY) * height);
      }

      BcFluidPipeQuads.emitPipeCuboid(
         pose, bb, sprite, realMinX, realMinY, realMinZ, realMaxX, realMaxY, realMaxZ, skipFaceMask, rgba, packedLight
      );
   }
}
