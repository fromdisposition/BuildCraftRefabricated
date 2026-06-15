/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.fluid.BcFluidTankRenderer;
import buildcraft.lib.fluid.identity.FluidIdentity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import buildcraft.lib.fluid.registry.FluidSmoother;
import buildcraft.lib.fluid.stack.FluidStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderTank implements BlockEntityRenderer<TileTank, TankRenderState> {
   public RenderTank(Context context) {
   }

   public TankRenderState createRenderState() {
      return new TankRenderState();
   }

   @Override
   public void extractRenderState(TileTank tile, TankRenderState state, float partialTick, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
      BlockEntityRenderer.super.extractRenderState(tile, state, partialTick, cameraPos, crumblingOverlay);
      this.extract(tile, state, partialTick);
   }

   private void extract(TileTank tile, TankRenderState state, float partialTick) {
      state.hasFluid = false;
      Level level = tile.getLevel();
      if (level == null) {
         return;
      }

      FluidSmoother.FluidStackInterp interp = tile.smoothedTank.getFluidForRender(partialTick);
      if (interp == null) {
         return;
      }

      FluidStack fluid = interp.fluid();
      double amount = interp.amount();
      int capacity = tile.smoothedTank.getCapacity();
      if (amount <= 0.0 || capacity <= 0) {
         return;
      }

      BcFluidAppearance appearance = BcFluidAppearanceCache.get(fluid);
      if (appearance == null) {
         return;
      }

      boolean connectedDown = isConnectedFluid(tile, Direction.DOWN);
      boolean connectedUp = isConnectedFluid(tile, Direction.UP);
      float fillRatio = (float)(amount / capacity);
      state.hasFluid = true;
      state.appearance = appearance;
      state.fluid = fluid;
      state.amount = amount;
      state.capacity = capacity;
      state.minY = connectedDown ? 0.0F : 0.01F;
      state.maxYFull = connectedUp ? 0.99999F : 0.99F;
      state.renderTop = !connectedUp || fillRatio < 1.0F;
      state.renderBottom = !connectedDown;
   }

   public void submit(TankRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      ProfilerFiller _profiler = Profiler.get();
      _profiler.push("buildcraft:tank_submit");

      try {
         if (!renderState.hasFluid || renderState.appearance == null) {
            return;
         }

         int light = renderState.lightCoords;
         poseStack.pushPose();
         collector.submitCustomGeometry(poseStack, BcFluidAppearanceCache.renderType(renderState.appearance), (pose, buffer) -> BcFluidTankRenderer.renderFilledBox(
            pose,
            buffer,
            renderState.appearance.sprite(),
            0.135F,
            renderState.minY,
            0.135F,
            0.865F,
            renderState.maxYFull,
            0.865F,
            renderState.fluid,
            renderState.amount,
            renderState.capacity,
            renderState.renderTop,
            renderState.renderBottom,
            light,
            OverlayTexture.NO_OVERLAY
         ));
         poseStack.popPose();
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
            if (!FluidIdentity.areEquivalentFluidStacks(thisFluid.copyWithAmount(1), otherFluid.copyWithAmount(1))) {
               return false;
            }

            Direction checkDir = FluidVariantAttributes.isLighterThanAir(FluidVariant.of(thisFluid.getFluid())) ? direction.getOpposite() : direction;
            return otherTank.fluidTank.getAmountMb() >= otherTank.fluidTank.getCapacityMb() || checkDir == Direction.UP;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
