/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client.model;

import buildcraft.core.BCCore;
import buildcraft.lib.client.fluid.FluidClientCache;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.SimpleFluidContent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class FluidShardItemModel implements ItemModel {
   private static final Field PROPERTIES_FIELD;
   private static final Field EXTENTS_FIELD;
   private final ItemModel vanillaDelegate;
   private final @Nullable ModelRenderProperties renderProperties;
   private final @Nullable Supplier<Vector3fc[]> extents;

   @SuppressWarnings("unchecked")
   public FluidShardItemModel(ItemModel vanillaDelegate) {
      this.vanillaDelegate = vanillaDelegate;
      if (vanillaDelegate instanceof CuboidItemModelWrapper wrapper) {
         try {
            this.renderProperties = (ModelRenderProperties)PROPERTIES_FIELD.get(wrapper);
            this.extents = (Supplier<Vector3fc[]>)EXTENTS_FIELD.get(wrapper);
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to read CuboidItemModelWrapper bake data", e);
         }
      } else {
         this.renderProperties = null;
         this.extents = null;
      }
   }

   public void update(
      ItemStackRenderState renderState,
      ItemStack stack,
      ItemModelResolver modelResolver,
      ItemDisplayContext displayContext,
      @Nullable ClientLevel level,
      @Nullable ItemOwner owner,
      int seed
   ) {
      this.vanillaDelegate.update(renderState, stack, modelResolver, displayContext, level, owner, seed);

      SimpleFluidContent content = (SimpleFluidContent)stack.getOrDefault(BCCore.FLUID_CONTENT, SimpleFluidContent.EMPTY);
      FluidStack fluid = content.copy();
      FluidClientCache.Appearance appearance = FluidClientCache.get(fluid);
      if (appearance == null || appearance.sprite() == null) {
         return;
      }

      List<BakedQuad> fluidQuads = createFluidQuads(appearance.sprite(), appearance.tint());
      if (fluidQuads.isEmpty()) {
         return;
      }

      renderState.appendModelIdentityElement(this);
      renderState.appendModelIdentityElement(fluid.getFluid());
      renderState.appendModelIdentityElement(displayContext);
      LayerRenderState layer = renderState.newLayer();
      layer.prepareQuadList().addAll(fluidQuads);
      if (this.extents != null) {
         layer.setExtents(this.extents);
      }

      if (this.renderProperties != null) {
         this.renderProperties.applyToLayer(layer, displayContext);
      }
   }

   private static List<BakedQuad> createFluidQuads(TextureAtlasSprite sprite, int tint) {
      List<BakedQuad> quads = new ArrayList<>(1);
      ModelUtil.UvFaceData uvs = ModelUtil.UvFaceData.from16(4, 4, 12, 12);
      MutableQuad quad = ModelUtil.createFace(
         Direction.SOUTH,
         new Vector3f(0.25F, 0.25F, 0.53125F),
         new Vector3f(0.25F, 0.75F, 0.53125F),
         new Vector3f(0.75F, 0.75F, 0.53125F),
         new Vector3f(0.75F, 0.25F, 0.53125F),
         uvs
      );
      quad.setSprite(sprite);
      quad.texFromSprite(sprite);
      quad.colouri(tint);
      quads.add(quad.toBakedItem());
      return quads;
   }

   static {
      try {
         PROPERTIES_FIELD = CuboidItemModelWrapper.class.getDeclaredField("properties");
         PROPERTIES_FIELD.setAccessible(true);
         EXTENTS_FIELD = CuboidItemModelWrapper.class.getDeclaredField("extents");
         EXTENTS_FIELD.setAccessible(true);
      } catch (NoSuchFieldException e) {
         throw new RuntimeException("Failed to access CuboidItemModelWrapper bake data", e);
      }
   }
}
