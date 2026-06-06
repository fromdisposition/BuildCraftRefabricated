package buildcraft.lib.client.fluid;

import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluids.FluidStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class BcFluidBerHelper {
   private BcFluidBerHelper() {
   }

   public static void renderSmoothedFluid(
      FluidSmoother smoother, BcFluidBerHelper.TankBounds bounds, PoseStack poseStack, BufferSource bufferSource, int light, float partialTicks
   ) {
      FluidSmoother.FluidStackInterp interp = smoother.getFluidForRender(partialTicks);
      if (interp != null && !(interp.amount() <= 0.0)) {
         FluidStack fluid = interp.fluid();
         int capacity = smoother.getCapacity();
         if (capacity > 0) {
            FluidClientCache.Appearance appearance = FluidClientCache.get(fluid);
            if (appearance != null) {
               float shrink = 0.015625F;
               float minX = bounds.minX / 16.0F + shrink;
               float minY = bounds.minY / 16.0F + shrink;
               float minZ = bounds.minZ / 16.0F + shrink;
               float maxX = bounds.maxX / 16.0F - shrink;
               float maxY = bounds.maxY / 16.0F - shrink;
               float maxZ = bounds.maxZ / 16.0F - shrink;
               VertexConsumer buffer = bufferSource.getBuffer(FluidClientCache.renderType(appearance));
               Pose pose = poseStack.last();
               BcFluidTankRenderer.renderFilledBox(
                  pose,
                  buffer,
                  appearance.sprite(),
                  minX,
                  minY,
                  minZ,
                  maxX,
                  maxY,
                  maxZ,
                  fluid,
                  interp.amount(),
                  capacity,
                  true,
                  true,
                  light,
                  OverlayTexture.NO_OVERLAY
               );
            }
         }
      }
   }

   public static final class TankBounds {
      public final float minX;
      public final float minY;
      public final float minZ;
      public final float maxX;
      public final float maxY;
      public final float maxZ;

      public TankBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
         this.minX = minX;
         this.minY = minY;
         this.minZ = minZ;
         this.maxX = maxX;
         this.maxY = maxY;
         this.maxZ = maxZ;
      }

      public BcFluidBerHelper.TankBounds rotateY() {
         float newMinX = 16.0F - this.maxZ;
         float newMinZ = this.minX;
         float newMaxX = 16.0F - this.minZ;
         float newMaxZ = this.maxX;
         return new BcFluidBerHelper.TankBounds(
            Math.min(newMinX, newMaxX), this.minY, Math.min(newMinZ, newMaxZ), Math.max(newMinX, newMaxX), this.maxY, Math.max(newMinZ, newMaxZ)
         );
      }
   }
}
