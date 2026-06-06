package buildcraft.transport.client.render;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.client.fluid.BcFluidQuadEmitter;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
   INSTANCE;

   private static final ThreadLocal<double[]> SCRATCH_AMOUNTS = ThreadLocal.withInitial(() -> new double[7]);

   public void render(PipeFlowFluids flow, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack poseStack) {
      FluidStack forRender = flow.getFluidStackForRender();
      if (forRender != null && !forRender.isEmpty() && bb != null) {
         ensureRenderCache(flow, forRender);
         Identifier flowTexture = flow.renderCacheSpriteId;
         if (flowTexture != null) {
            TextureAtlas atlas = (TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(BcTextureAtlases.BLOCKS_TEXTURE);
            TextureAtlasSprite sprite = atlas.getSprite(flowTexture);
            float r = flow.renderCacheTintR / 255.0F;
            float g = flow.renderCacheTintG / 255.0F;
            float b = flow.renderCacheTintB / 255.0F;
            float a = flow.renderCacheTintA / 255.0F;
            int packedLight = PipeRenderContext.getPackedLight();
            double[] amounts = SCRATCH_AMOUNTS.get();
            flow.writeAmountsForRender(partialTicks, amounts);
            boolean gas = FluidUtilBC.isGaseous(forRender.getFluid());
            boolean horizontal = false;
            boolean vertical = flow.pipe.isConnected(gas ? Direction.DOWN : Direction.UP);

            for (Direction face : Direction.values()) {
               double size = ((Pipe)flow.pipe).getConnectedDist(face);
               int fi = face.ordinal();
               double amount = amounts[fi];
               if (face.getAxis() != Axis.Y) {
                  horizontal |= flow.pipe.isConnected(face) && amount > 0.0;
               }

               if (!(amount <= 0.0)) {
                  double centerShift = 0.24 + size / 2.0;
                  double cx = 0.5 + face.getStepX() * centerShift;
                  double cy = 0.5 + face.getStepY() * centerShift;
                  double cz = 0.5 + face.getStepZ() * centerShift;
                  double rx = 0.24;
                  double ry = 0.24;
                  double rz = 0.24;
                  double faceAxisRadius = size / 2.0;
                  switch (face.getAxis()) {
                     case X:
                        rx = faceAxisRadius;
                        break;
                     case Y:
                        ry = faceAxisRadius;
                        break;
                     case Z:
                        rz = faceAxisRadius;
                  }

                  if (face.getAxis() == Axis.Y) {
                     double perc = Math.sqrt(amount / flow.capacity);
                     rx = perc * 0.24;
                     rz = perc * 0.24;
                  }

                  double cuboidAmount = face.getAxis() == Axis.Y ? 1.0 : amount;
                  double cuboidCapacity = face.getAxis() == Axis.Y ? 1.0 : flow.capacity;
                  int faceSkipMask = 0;
                  int centerIdx = EnumPipePart.CENTER.getIndex();
                  if (amounts[centerIdx] > 0.0) {
                     faceSkipMask = 1 << face.getOpposite().ordinal();
                  }

                  renderFluidCuboid(
                     flow.renderCacheEntry,
                     sprite,
                     cx - rx,
                     cy - ry,
                     cz - rz,
                     cx + rx,
                     cy + ry,
                     cz + rz,
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

            int ci = EnumPipePart.CENTER.getIndex();
            double centerAmount = amounts[ci];
            boolean renderedHorizCenter = false;
            double horizPos = 0.26;
            if (horizontal || !vertical) {
               renderFluidCuboid(
                  flow.renderCacheEntry, sprite, 0.26, 0.26, 0.26, 0.74, 0.74, 0.74, centerAmount, flow.capacity, gas, 0, r, g, b, a, packedLight, bb, poseStack
               );
               horizPos += 0.48 * centerAmount / flow.capacity;
               renderedHorizCenter = true;
            }

            if (vertical && horizPos < 0.74) {
               double perc = Math.sqrt(centerAmount / flow.capacity);
               double minXZ = 0.5 - 0.24 * perc;
               double maxXZ = 0.5 + 0.24 * perc;
               double yMin = gas ? 0.26 : horizPos;
               double yMax = gas ? 1.0 - horizPos : 0.74;
               int pillarSkipMask = 0;
               if (renderedHorizCenter) {
                  pillarSkipMask = 1 << (gas ? Direction.UP.ordinal() : Direction.DOWN.ordinal());
               }

               renderFluidCuboid(
                  flow.renderCacheEntry, sprite, minXZ, yMin, minXZ, maxXZ, yMax, maxXZ, 1.0, 1.0, gas, pillarSkipMask, r, g, b, a, packedLight, bb, poseStack
               );
            }
         }
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
         FluidUtilBC.FluidAppearance appearance = FluidUtilBC.appearance(fluidStack, FluidUtilBC.FluidRenderContext.PIPE);
         flow.renderCacheSpriteId = appearance.texture();
         flow.renderCacheEntry = BCEnergyFluidsFabric.findEntry(current);
         flow.renderCacheBcGradient = flow.renderCacheEntry != null;
         if (flow.renderCacheEntry != null) {
            flow.renderCacheTexLight = flow.renderCacheEntry.texLight();
            flow.renderCacheTexDark = flow.renderCacheEntry.texDark();
            flow.renderCacheHeat = flow.renderCacheEntry.heat();
         } else {
            flow.renderCacheTexLight = 0;
            flow.renderCacheTexDark = 0;
            flow.renderCacheHeat = -1;
         }

         int color = appearance.tintArgb();
         flow.renderCacheTintR = color >> 16 & 0xFF;
         flow.renderCacheTintG = color >> 8 & 0xFF;
         flow.renderCacheTintB = color & 0xFF;
         int alpha = color >> 24 & 0xFF;
         flow.renderCacheTintA = alpha == 0 ? 255 : alpha;
         flow.renderCacheTranslucent = FluidUtilBC.shouldRenderTranslucent(fluidStack);
      }
   }

   private static void renderFluidCuboid(
      BCEnergyFluidsFabric.FluidEntry entry,
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
         float realMinX;
         float realMinY;
         float realMinZ;
         float realMaxX;
         float realMaxY;
         float realMaxZ;
         if (gas) {
            realMinX = (float)minX;
            realMinZ = (float)minZ;
            realMinY = (float)(maxY - (maxY - minY) * height);
            realMaxX = (float)maxX;
            realMaxY = (float)maxY;
            realMaxZ = (float)maxZ;
         } else {
            realMinX = (float)minX;
            realMinY = (float)minY;
            realMinZ = (float)minZ;
            realMaxX = (float)maxX;
            realMaxZ = (float)maxZ;
            realMaxY = (float)(minY + (maxY - minY) * height);
         }

         if (entry != null) {
            BcFluidQuadEmitter.emitPipeCuboid(
               poseStack.last(), bb, sprite, entry, realMinX, realMinY, realMinZ, realMaxX, realMaxY, realMaxZ, skipFaceMask, r, g, b, a, packedLight
            );
         } else {
            BcFluidQuadEmitter.emitStaticPipeCuboid(
               poseStack.last(), bb, sprite, realMinX, realMinY, realMinZ, realMaxX, realMaxY, realMaxZ, skipFaceMask, r, g, b, a, packedLight
            );
         }
      }
   }
}
