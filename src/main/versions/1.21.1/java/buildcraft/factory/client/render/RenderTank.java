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
import buildcraft.lib.fluid.registry.FluidSmoother;
import buildcraft.lib.fluid.stack.FluidStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * 1.21.1 (versions/1.21.1) tank renderer: immediate-mode port of the shared render-state/submit BER. Computes
 * the smoothed fluid level inline and draws the filled box through the version-neutral BcFluidTankRenderer.
 */
public class RenderTank implements BlockEntityRenderer<TileTank> {
   public RenderTank(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public void render(TileTank tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
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
      float fillRatio = (float) (amount / capacity);
      float minY = connectedDown ? 0.0F : 0.01F;
      float maxYFull = connectedUp ? 0.99999F : 0.99F;
      boolean renderTop = !connectedUp || fillRatio < 1.0F;
      boolean renderBottom = !connectedDown;

      poseStack.pushPose();
      VertexConsumer buffer = buffers.getBuffer(BcFluidAppearanceCache.renderType(appearance));
      BcFluidTankRenderer.renderFilledBox(
         poseStack.last(),
         buffer,
         appearance.sprite(),
         0.135F,
         minY,
         0.135F,
         0.865F,
         maxYFull,
         0.865F,
         fluid,
         amount,
         capacity,
         renderTop,
         renderBottom,
         light,
         OverlayTexture.NO_OVERLAY
      );
      poseStack.popPose();
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
