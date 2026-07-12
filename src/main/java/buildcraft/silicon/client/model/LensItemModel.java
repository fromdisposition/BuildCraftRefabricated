/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.plug.PlugBakerLens;
import buildcraft.silicon.item.ItemPluggableLens;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LensItemModel implements ItemModel {
   private static final LensItemModel.ContextXform DEFAULT_XFORM = new LensItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.8F);
   private static final EnumMap<ItemDisplayContext, LensItemModel.ContextXform> XFORMS = new EnumMap<>(ItemDisplayContext.class);
   private static final LoadingCache<LensItemModel.CacheKey, List<BakedQuad>> cache = CacheBuilder.newBuilder()
      //? if >= 26.2 {
      .expireAfterAccess(java.time.Duration.ofMinutes(1))
      //?} else {
      /*.expireAfterAccess(1L, TimeUnit.MINUTES)
      *///?}
      .build(CacheLoader.from(key -> {
         LensItemModel.ContextXform xform = XFORMS.getOrDefault(key.context(), DEFAULT_XFORM);
         LensItemModel.LensKey lk = key.lensKey();
         List<BakedQuad> quads = new ArrayList<>();

         for (MutableQuad mq : PlugBakerLens.bakeForItem(lk.colour(), lk.isFilter(), true)) {
            transformForItem(mq, xform, true);
            quads.add(mq.toBakedItem());
         }

         for (MutableQuad mq : PlugBakerLens.bakeForItem(lk.colour(), lk.isFilter(), false)) {
            transformForItem(mq, xform, false);
            // The render-state item pipeline (1.21.10+, every version that uses this shared model) colours items
            // through the layer tint, not per-vertex quad colour, so the dye PlugBakerLens applies via colouri(...)
            // never reaches the GUI on bake. Tag the overlay with tintIndex 0; the actual dye is supplied through
            // the render-state layer tint in update() (tintLayers() on 26.1+, prepareTintLayers() on 1.21.10/11).
            mq.setTint(0);
            quads.add(mq.toBakedItem());
         }

         return quads;
      }));

   private static void transformForItem(MutableQuad mq, LensItemModel.ContextXform x, boolean resetColors) {
      mq.setShade(false);
      mq.rotate(Direction.WEST, Direction.NORTH, 0.5F, 0.5F, 0.5F);
      mq.translatef(-0.5F, -0.5F, -0.5F);
      if (x.rotX() != 0.0F) {
         mq.rotateX((float)Math.toRadians(x.rotX()));
      }

      if (x.rotY() != 0.0F) {
         mq.rotateY((float)Math.toRadians(x.rotY()));
      }

      if (x.rotZ() != 0.0F) {
         mq.rotateZ((float)Math.toRadians(x.rotZ()));
      }

      if (x.scale() != 1.0F) {
         mq.scalef(x.scale());
      }

      mq.translatef(0.5F, 0.5F, 0.5F);
      if (x.tx() != 0.0F || x.ty() != 0.0F || x.tz() != 0.0F) {
         mq.translatef(x.tx() / 16.0F, x.ty() / 16.0F, x.tz() / 16.0F);
      }

      mq.setCalculatedNormal();
      if (resetColors) {
         mq.vertex_0.colouri(255, 255, 255, 255);
         mq.vertex_1.colouri(255, 255, 255, 255);
         mq.vertex_2.colouri(255, 255, 255, 255);
         mq.vertex_3.colouri(255, 255, 255, 255);
      }
   }

   public static void onModelBake() {
      cache.invalidateAll();
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
      DyeColor colour = ItemPluggableLens.getColour(stack);
      boolean isFilter = ItemPluggableLens.isFilter(stack);
      LensItemModel.CacheKey key = new LensItemModel.CacheKey(new LensItemModel.LensKey(colour, isFilter), displayContext);
      List<BakedQuad> quads = (List<BakedQuad>)cache.getUnchecked(key);
      if (!quads.isEmpty()) {
         for (BakedQuad quad : quads) {
            //? if >= 26.1 {
            if ((quad.materialInfo().flags() & 2) != 0) {
            //?} else {
            /*if (false) {
            *///?}
               renderState.setAnimated();
               break;
            }
         }

         renderState.appendModelIdentityElement(this);
         renderState.appendModelIdentityElement(key.lensKey());
         renderState.appendModelIdentityElement(displayContext);
         LayerRenderState layer = renderState.newLayer();
         layer.prepareQuadList().addAll(quads);
         //? if >= 26.1 {
         // Supply the dye colour for tint layer 0 (the overlay quads are tagged with tintIndex 0). The render-state
         // item pipeline ignores per-vertex quad colour, so this layer tint is the only way the dye reaches the GUI.
         int lensTint = colour != null ? 0xFF000000 | colour.getTextureDiffuseColor() & 0xFFFFFF : 0xFF3F76E4;
         layer.tintLayers().add(lensTint);
         //?} else {
         /*// 1.21.10/1.21.11: dye reaches the GUI via tint layer 0 (prepareTintLayers(int)[] here, not 26.1's
         // tintLayers() IntList). CRUCIAL: the < 26.1 BakedQuad carries NO render type (the 26.x MaterialInfo
         // does), so the layer's render type MUST be set explicitly — without it the layer renders with the
         // wrong/default type and the whole lens breaks. cutout matches what 26.x bakes (cutoutBlockItemSheet).
         int lensTint = colour != null ? 0xFF000000 | colour.getTextureDiffuseColor() & 0xFFFFFF : 0xFF3F76E4;
         layer.prepareTintLayers(1)[0] = lensTint;
         layer.setRenderType(buildcraft.lib.client.render.BCLibRenderTypes.cutoutBlockSheet());
         *///?}
      }
   }

   static {
      // 1.35, not the gate's 1.8: the GUI scale is applied literally to the baked quads (no fit-to-slot), and the
      // lens/filter plate is an 8px face (0.25..0.75) vs the gate's 6px body — 8*1.35 = 6*1.8 = 10.8px, so filters
      // fill the inventory slot with the same visual weight as gates instead of overflowing the frame.
      XFORMS.put(ItemDisplayContext.GUI, new LensItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.35F));
      XFORMS.put(ItemDisplayContext.GROUND, new LensItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.9F));
      XFORMS.put(ItemDisplayContext.HEAD, new LensItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.8F));
      XFORMS.put(ItemDisplayContext.FIXED, new LensItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.53F));
      XFORMS.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new LensItemModel.ContextXform(0.0F, 45.0F, 0.0F, 0.0F, 0.0F, -4.0F, 0.72F));
      XFORMS.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new LensItemModel.ContextXform(0.0F, 225.0F, 0.0F, 0.0F, 0.0F, -4.0F, 0.72F));
      XFORMS.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new LensItemModel.ContextXform(75.0F, 225.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.675F));
      XFORMS.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new LensItemModel.ContextXform(75.0F, 45.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.675F));
   }

   private record CacheKey(LensItemModel.LensKey lensKey, ItemDisplayContext context) {
   }

   private record ContextXform(float rotX, float rotY, float rotZ, float tx, float ty, float tz, float scale) {
   }

   private record LensKey(@Nullable DyeColor colour, boolean isFilter) {
   }
}
