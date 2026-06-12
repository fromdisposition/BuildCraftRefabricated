/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;


import buildcraft.lib.fluid.meta.FluidAttributes;
import buildcraft.lib.client.fluid.BcFluidPipeQuads;
import buildcraft.lib.client.fluid.BcFluidRenderLookup;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;

public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
   INSTANCE;

   /** BC 8.0 pipe-fluid inset — keeps fluid inside the pipe shell at block faces. */
   private static final double CENTER_INSET = 0.26;
   private static final double CENTER_OUTSET = 0.74;
   /** BC 8.0 arm centre offset from block centre toward the connected face. */
   private static final double ARM_OFFSET = 0.245;
   private static final double ARM_RADIUS = 0.24;
   /** Thin axis padding so arms meet the block face exactly (0.005 + size/2). */
   private static final double ARM_AXIS_PAD = 0.005;
   /** Sub-voxel overlap to hide seams between adjacent pipe blocks. */
   private static final double FACE_OVERLAP = 0.002;

   private static final ThreadLocal<double[]> SCRATCH_AMOUNTS = ThreadLocal.withInitial(() -> new double[7]);
   private static final ThreadLocal<double[]> SCRATCH_OFFSET_X = ThreadLocal.withInitial(() -> new double[7]);
   private static final ThreadLocal<double[]> SCRATCH_OFFSET_Y = ThreadLocal.withInitial(() -> new double[7]);
   private static final ThreadLocal<double[]> SCRATCH_OFFSET_Z = ThreadLocal.withInitial(() -> new double[7]);

   public void render(PipeFlowFluids flow, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack poseStack) {
      FluidStack forRender = flow.getFluidStackForRender();
      if (forRender != null && !forRender.isEmpty() && bb != null) {
         ensureRenderCache(flow, forRender);
         TextureAtlasSprite sprite = flow.renderCacheSprite;
         if (sprite != null) {
            float r = flow.renderCacheTintR / 255.0F;
            float g = flow.renderCacheTintG / 255.0F;
            float b = flow.renderCacheTintB / 255.0F;
            float a = flow.renderCacheTintA / 255.0F;
            int packedLight = PipeRenderContext.getPackedLight();
            double[] amounts = SCRATCH_AMOUNTS.get();
            double[] offsetX = SCRATCH_OFFSET_X.get();
            double[] offsetY = SCRATCH_OFFSET_Y.get();
            double[] offsetZ = SCRATCH_OFFSET_Z.get();
            flow.writeAmountsForRender(partialTicks, amounts);
            flow.writeOffsetsForRender(partialTicks, offsetX, offsetY, offsetZ);
            boolean gas = FluidAttributes.of(forRender.getFluid()).isLighterThanAir();
            boolean horizontal = false;
            boolean vertical = flow.pipe.isConnected(gas ? Direction.DOWN : Direction.UP);
            int centerIdx = EnumPipePart.CENTER.getIndex();
            double centerAmount = amounts[centerIdx];

            for (Direction face : Direction.values()) {
               int fi = face.ordinal();
               double amount = amounts[fi];
               if (face.getAxis() != Axis.Y) {
                  horizontal |= flow.pipe.isConnected(face) && amount > 0.0;
               }

               boolean bridge = amount <= 0.0 && centerAmount > 0.0 && flow.pipe.isConnected(face);
               if (amount > 0.0 || bridge) {
                  double renderAmount = amount > 0.0 ? amount : centerAmount;
                  double size = ((Pipe)flow.pipe).getConnectedDist(face);
                  double ox = offsetX[fi];
                  double oy = offsetY[fi];
                  double oz = offsetZ[fi];
                  double[] bounds = armBounds(face, size, renderAmount, flow.capacity, ox, oy, oz);
                  int faceSkipMask = centerAmount > 0.0 ? 1 << face.getOpposite().ordinal() : 0;
                  double cuboidAmount = face.getAxis() == Axis.Y ? 1.0 : renderAmount;
                  double cuboidCapacity = face.getAxis() == Axis.Y ? 1.0 : flow.capacity;
                  renderFluidCuboid(
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
                     r,
                     g,
                     b,
                     a,
                     packedLight,
                     bb,
                     poseStack
                  );
               }
            }

            boolean renderedHorizCenter = false;
            double horizPos = CENTER_INSET;
            if (horizontal || !vertical) {
               double ox = offsetX[centerIdx];
               double oy = offsetY[centerIdx];
               double oz = offsetZ[centerIdx];
               renderFluidCuboid(
                  sprite,
                  CENTER_INSET + ox,
                  CENTER_INSET + oy,
                  CENTER_INSET + oz,
                  CENTER_OUTSET + ox,
                  CENTER_OUTSET + oy,
                  CENTER_OUTSET + oz,
                  centerAmount,
                  flow.capacity,
                  gas,
                  0,
                  r,
                  g,
                  b,
                  a,
                  packedLight,
                  bb,
                  poseStack
               );
               horizPos += 0.48 * centerAmount / flow.capacity;
               renderedHorizCenter = true;
            }

            if (vertical && horizPos < CENTER_OUTSET) {
               double perc = Math.sqrt(centerAmount / flow.capacity);
               double minXZ = 0.5 - ARM_RADIUS * perc;
               double maxXZ = 0.5 + ARM_RADIUS * perc;
               double ox = offsetX[centerIdx];
               double oy = offsetY[centerIdx];
               double oz = offsetZ[centerIdx];
               double yMin = gas ? CENTER_INSET : horizPos;
               double yMax = gas ? 1.0 - horizPos : CENTER_OUTSET;
               int pillarSkipMask = renderedHorizCenter ? 1 << (gas ? Direction.UP.ordinal() : Direction.DOWN.ordinal()) : 0;
               renderFluidCuboid(
                  sprite,
                  minXZ + ox,
                  yMin + oy,
                  minXZ + oz,
                  maxXZ + ox,
                  yMax + oy,
                  maxXZ + oz,
                  1.0,
                  1.0,
                  gas,
                  pillarSkipMask,
                  r,
                  g,
                  b,
                  a,
                  packedLight,
                  bb,
                  poseStack
               );
            }
         }
      }
   }

   /** Returns {minX, minY, minZ, maxX, maxY, maxZ} in block space. */
   private static double[] armBounds(Direction face, double size, double amount, double capacity, double ox, double oy, double oz) {
      double shift = ARM_OFFSET + size * 0.5;
      double cx = 0.5 + face.getStepX() * shift + ox;
      double cy = 0.5 + face.getStepY() * shift + oy;
      double cz = 0.5 + face.getStepZ() * shift + oz;
      double rx = ARM_RADIUS;
      double ry = ARM_RADIUS;
      double rz = ARM_RADIUS;
      if (face.getAxis() == Axis.Y) {
         double perc = Math.sqrt(amount / capacity);
         rx = perc * ARM_RADIUS;
         rz = perc * ARM_RADIUS;
         ry = ARM_AXIS_PAD + size * 0.5;
      } else {
         double axisHalf = ARM_AXIS_PAD + size * 0.5;
         switch (face.getAxis()) {
            case X:
               rx = axisHalf;
               break;
            case Z:
               rz = axisHalf;
               break;
            default:
               break;
         }
      }

      double[] bounds = new double[]{cx - rx, cy - ry, cz - rz, cx + rx, cy + ry, cz + rz};
      extendToBlockFace(face, bounds);
      return bounds;
   }

   private static void extendToBlockFace(Direction face, double[] bounds) {
      double overlap = face.getAxisDirection() == AxisDirection.POSITIVE ? FACE_OVERLAP : -FACE_OVERLAP;
      switch (face) {
         case EAST:
            bounds[3] = 1.0 + overlap;
            break;
         case WEST:
            bounds[0] = -overlap;
            break;
         case UP:
            bounds[4] = 1.0 + overlap;
            break;
         case DOWN:
            bounds[1] = -overlap;
            break;
         case SOUTH:
            bounds[5] = 1.0 + overlap;
            break;
         case NORTH:
            bounds[2] = -overlap;
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
      if (fluidStack.getFluid() != flow.renderCacheFluid) {
         flow.renderCacheFluid = fluidStack.getFluid();
         flow.renderCacheSprite = BcFluidRenderLookup.pipeSprite(fluidStack);
         float[] rgba = BcFluidRenderLookup.pipeVertexRgba(fluidStack);
         flow.renderCacheTintR = (int)(rgba[0] * 255.0F);
         flow.renderCacheTintG = (int)(rgba[1] * 255.0F);
         flow.renderCacheTintB = (int)(rgba[2] * 255.0F);
         flow.renderCacheTintA = (int)(rgba[3] * 255.0F);
         flow.renderCacheTranslucent = BcFluidRenderLookup.translucent(fluidStack);
      }
   }

   private static void renderFluidCuboid(
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
      float r,
      float g,
      float b,
      float a,
      int packedLight,
      VertexConsumer bb,
      PoseStack poseStack
   ) {
      if (!(amount <= 0.0) && !(capacity <= 0.0)) {
         double height = Math.min(amount / capacity, 1.0);
         float realMinX = (float)minX;
         float realMinY = (float)minY;
         float realMinZ = (float)minZ;
         float realMaxX = (float)maxX;
         float realMaxZ = (float)maxZ;
         float realMaxY;
         if (gas) {
            realMaxY = (float)maxY;
            realMinY = (float)(maxY - (maxY - minY) * height);
         } else {
            realMaxY = (float)(minY + (maxY - minY) * height);
         }

         BcFluidPipeQuads.emitPipeCuboid(
            poseStack.last(), bb, sprite, null, realMinX, realMinY, realMinZ, realMaxX, realMaxY, realMaxZ, skipFaceMask, r, g, b, a, packedLight
         );
      }
   }
}
