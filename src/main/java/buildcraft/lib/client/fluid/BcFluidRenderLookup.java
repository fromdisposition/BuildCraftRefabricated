/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;


import buildcraft.lib.fluid.identity.FluidIdentity;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.fabric.transfer.fluid.FluidVariants;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

/**
 * Client fluid sprite/tint lookup.
 * World blocks and pipes use pre-baked {@code block/fluids/baked/*} sprites from {@link FluidModel}.
 * Vertex-tinted surfaces (tanks, blueprint preview) use shared {@code heat_*_white} templates.
 */
public final class BcFluidRenderLookup {
   private BcFluidRenderLookup() {
   }

   public enum SpriteKind {
      STILL,
      FLOWING
   }

   /** World / appearance cache — pre-baked colored block sprites. */
   public static TextureAtlasSprite sprite(FluidStack stack, SpriteKind kind) {
      if (stack == null || stack.isEmpty()) {
         return missingSprite();
      }

      return resolveBakedSprite(stack.getFluid(), kind);
   }

   public static TextureAtlasSprite sprite(Fluid fluid, SpriteKind kind) {
      return resolveBakedSprite(fluid, kind);
   }

   /** Pre-baked still sprite for pipe rendering (color is already in the texture). */
   public static TextureAtlasSprite pipeSprite(FluidStack stack) {
      return sprite(stack, SpriteKind.STILL);
   }

   /** Tanks/BER with per-vertex heat template tinting — shared white luminance sprites. */
   public static TextureAtlasSprite tintSprite(FluidStack stack, SpriteKind kind) {
      if (stack == null || stack.isEmpty()) {
         return missingSprite();
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(stack.getFluid());
      if (entry != null) {
         return whiteTemplateSprite(entry.heat(), kind);
      }

      return resolveBakedSprite(stack.getFluid(), kind);
   }

   public static TextureAtlasSprite tintSprite(Fluid fluid, SpriteKind kind) {
      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluid);
      if (entry != null) {
         return whiteTemplateSprite(entry.heat(), kind);
      }

      return resolveBakedSprite(fluid, kind);
   }

   private static FluidModel modelFor(Fluid fluid) {
      Minecraft minecraft = Minecraft.getInstance();
      FluidState state = fluid.defaultFluidState();
      return minecraft.getModelManager().getFluidStateModelSet().get(state);
   }

   public static int tint(FluidStack stack) {
      if (stack == null || stack.isEmpty()) {
         return -1;
      }

      BcFluidAppearance appearance = BcFluidAppearanceCache.get(stack);
      return appearance != null ? appearance.tint() : resolveTint(stack);
   }

   static TextureAtlasSprite resolveSprite(FluidStack stack, SpriteKind kind) {
      return stack != null && !stack.isEmpty() ? resolveBakedSprite(stack.getFluid(), kind) : missingSprite();
   }

   static TextureAtlasSprite resolveSprite(Fluid fluid, SpriteKind kind) {
      return resolveBakedSprite(fluid, kind);
   }

   private static TextureAtlasSprite resolveBakedSprite(Fluid fluid, SpriteKind kind) {
      Fluid canonical = FluidIdentity.canonicalFluid(fluid);
      if (canonical.isSame(Fluids.EMPTY)) {
         return missingSprite();
      }

      FluidModel model = modelFor(canonical);
      Baked material = kind == SpriteKind.FLOWING ? model.flowingMaterial() : model.stillMaterial();
      TextureAtlasSprite sprite = material.sprite();
      return sprite != null ? sprite : missingSprite();
   }

   private static TextureAtlasSprite whiteTemplateSprite(int heat, SpriteKind kind) {
      Identifier id = kind == SpriteKind.FLOWING ? BcFluidTintUtil.heatFlowWhiteSpriteId(heat) : BcFluidTintUtil.heatStillWhiteSpriteId(heat);
      return BcTextureAtlases.getBlockSprite(id);
   }

   public static int itemMaskTint(FluidStack stack, @Nullable BlockAndTintGetter level) {
      if (stack == null || stack.isEmpty()) {
         return -1;
      }

      Fluid fluid = FluidIdentity.canonicalFluid(stack.getFluid());
      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluid);
      if (entry != null) {
         return BcFluidTintUtil.computeAverageGuiTint(entry.texLight(), entry.texDark(), entry.heat());
      }

      if (fluid.isSame(Fluids.WATER) || fluid.isSame(Fluids.FLOWING_WATER)) {
         return 0xFF3F76E4;
      }

      if (fluid.isSame(Fluids.LAVA) || fluid.isSame(Fluids.FLOWING_LAVA)) {
         return BcFluidTintUtil.RENDER_TINT_WHITE;
      }

      return fabricTint(stack, level, BlockPos.ZERO);
   }

   private static int fabricTint(FluidStack stack, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
      FluidVariant variant = FluidVariants.toVariant(stack);
      int color = level != null && pos != null
         ? FluidVariantRendering.getColor(variant, level, pos)
         : FluidVariantRendering.getColor(variant);
      return ensureOpaqueArgb(color);
   }

   private static int ensureOpaqueArgb(int color) {
      if (color == 0) {
         return BcFluidTintUtil.RENDER_TINT_WHITE;
      }

      int alpha = color >>> 24 & 0xFF;
      return alpha == 0 ? 0xFF000000 | color & 0xFFFFFF : color;
   }

   static int resolveTint(FluidStack stack) {
      if (stack == null || stack.isEmpty()) {
         return -1;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(FluidIdentity.canonicalFluid(stack.getFluid()));
      if (entry != null) {
         return BcFluidTintUtil.RENDER_TINT_WHITE;
      }

      return fabricTint(stack, null, null);
   }

   public static boolean translucent(FluidStack stack) {
      if (stack == null || stack.isEmpty()) {
         return false;
      }

      BcFluidAppearance appearance = BcFluidAppearanceCache.get(stack);
      return appearance != null ? appearance.translucent() : resolveTranslucent(stack);
   }

   static boolean resolveTranslucent(FluidStack stack) {
      return stack != null && !stack.isEmpty() && resolveTranslucent(stack.getFluid());
   }

   public static boolean translucent(Fluid fluid) {
      return resolveTranslucent(fluid);
   }

   static boolean resolveTranslucent(Fluid fluid) {
      Fluid canonical = FluidIdentity.canonicalFluid(fluid);
      if (canonical.isSame(Fluids.EMPTY)) {
         return false;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(canonical);
      if (entry != null) {
         return entry.gaseous();
      }

      Block block = canonical.defaultFluidState().createLegacyBlock().getBlock();
      return FluidRenderingRegistry.isBlockTransparent(block);
   }

   public static float[] vertexRgba(FluidStack stack) {
      return rgbaFromTint(tint(stack));
   }

   /** Pipe vertex multiply: white for BC baked sprites, Fabric tint for foreign fluids. */
   public static float[] pipeVertexRgba(FluidStack stack) {
      return rgbaFromTint(tint(stack));
   }

   private static float[] rgbaFromTint(int color) {
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
