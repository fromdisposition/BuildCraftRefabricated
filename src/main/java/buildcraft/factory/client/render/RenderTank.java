package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.fluid.BcFluidTankRenderer;
import buildcraft.lib.client.fluid.FluidClientCache;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;

public class RenderTank extends BcBlockEntityRenderer<TileTank, TankRenderState> {
   private static final float MIN_XZ = 0.135F;
   private static final float MAX_XZ = 0.865F;
   private static final float MIN_Y = 0.01F;
   private static final float MAX_Y = 0.99F;
   private static final float MIN_Y_CONNECTED = 0.0F;
   private static final float MAX_Y_CONNECTED = 0.99999F;

   public RenderTank(Context context) {
   }

   public TankRenderState createRenderState() {
      return new TankRenderState();
   }

   public void submit(TankRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      ProfilerFiller _profiler = Profiler.get();
      _profiler.push("buildcraft:tank_submit");

      try {
         TileTank tile = renderState.tile;
         if (tile == null) {
            return;
         }

         Level level = tile.getLevel();
         if (level == null) {
            return;
         }

         float partialTicks = renderState.partialTick;
         FluidSmoother.FluidStackInterp interp = tile.smoothedTank.getFluidForRender(partialTicks);
         if (interp == null) {
            return;
         }

         FluidStack fluid = interp.fluid();
         double amount = interp.amount();
         int capacity = tile.smoothedTank.getCapacity();
         if (amount <= 0.0 || capacity <= 0) {
            return;
         }

         FluidClientCache.Appearance appearance = FluidClientCache.get(fluid);
         if (appearance != null) {
            TextureAtlasSprite sprite = appearance.sprite();
            boolean connectedDown = isConnectedFluid(tile, Direction.DOWN);
            boolean connectedUp = isConnectedFluid(tile, Direction.UP);
            float minY = connectedDown ? 0.0F : 0.01F;
            float maxYFull = connectedUp ? 0.99999F : 0.99F;
            float fillRatio = (float)(amount / capacity);
            int light = renderState.light;
            int overlay = OverlayTexture.NO_OVERLAY;
            poseStack.pushPose();
            BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(FluidClientCache.renderType(appearance));
            Pose pose = poseStack.last();
            BcFluidTankRenderer.renderFilledBox(
               pose,
               buffer,
               sprite,
               0.135F,
               minY,
               0.135F,
               0.865F,
               maxYFull,
               0.865F,
               fluid,
               amount,
               capacity,
               !connectedUp || fillRatio < 1.0F,
               !connectedDown,
               light,
               overlay
            );
            poseStack.popPose();
            return;
         }
      } finally {
         _profiler.pop();
      }
   }

   private static boolean isConnectedFluid(TileTank tile, Direction direction) {
      if (tile.getLevel() == null) {
         return false;
      }

      BlockPos neighborPos = tile.getBlockPos().relative(direction);
      if (tile.getLevel().getBlockEntity(neighborPos) instanceof TileTank otherTank) {
         if (!TileTank.canTanksConnect(tile, otherTank, direction)) {
            return false;
         }

         FluidStack otherFluid = otherTank.fluidTank.getFluidStack();
         FluidStack thisFluid = tile.fluidTank.getFluidStack();
         if (!otherFluid.isEmpty() && !thisFluid.isEmpty()) {
            if (!FluidUtilBC.areEquivalentFluidStacks(thisFluid.copyWithAmount(1), otherFluid.copyWithAmount(1))) {
               return false;
            }

            Direction checkDir = FluidUtilBC.isGaseous(thisFluid.copyWithAmount(1)) ? direction.getOpposite() : direction;
            return otherTank.fluidTank.getAmountMb() >= otherTank.fluidTank.getCapacityMb() || checkDir == Direction.UP;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
