/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.transfer.fabric.TransferConvert;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class BcFluidRenderLookup {
   private BcFluidRenderLookup() {
   }

   public enum SpriteKind {
      STILL,
      FLOWING
   }

   public static TextureAtlasSprite sprite(FluidStack stack, SpriteKind kind) {
      return stack != null && !stack.isEmpty() ? sprite(stack.getFluid(), kind) : missingSprite();
   }

   public static TextureAtlasSprite sprite(Fluid fluid, SpriteKind kind) {
      Fluid canonical = FluidUtilBC.canonicalFluid(fluid);
      if (canonical.isSame(Fluids.EMPTY)) {
         return missingSprite();
      }

      FluidModel model = modelFor(canonical);
      Baked material = kind == SpriteKind.FLOWING ? model.flowingMaterial() : model.stillMaterial();
      TextureAtlasSprite sprite = material.sprite();
      return sprite != null ? sprite : missingSprite();
   }

   private static FluidModel modelFor(Fluid fluid) {
      Minecraft minecraft = Minecraft.getInstance();
      FluidState state = fluid.defaultFluidState();
      return minecraft.getModelManager().getFluidStateModelSet().get(state);
   }

   public static int tint(FluidStack stack) {
      return stack != null && !stack.isEmpty() ? FluidVariantRendering.getColor(TransferConvert.toVariant(stack)) : -1;
   }

   public static boolean translucent(FluidStack stack) {
      return stack != null && !stack.isEmpty() && translucent(stack.getFluid());
   }

   public static boolean translucent(Fluid fluid) {
      Fluid canonical = FluidUtilBC.canonicalFluid(fluid);
      if (canonical.isSame(Fluids.EMPTY)) {
         return false;
      }

      Block block = canonical.defaultFluidState().createLegacyBlock().getBlock();
      return FluidRenderingRegistry.isBlockTransparent(block);
   }

   public static float[] vertexRgba(FluidStack stack) {
      int color = tint(stack);
      float a = (color >> 24 & 0xFF) / 255.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      if (a <= 0.0F) {
         a = 1.0F;
      }

      return new float[]{r, g, b, a};
   }

   private static TextureAtlasSprite missingSprite() {
      return BcTextureAtlases.getBlockSprite(MissingTextureAtlasSprite.getLocation());
   }
}
