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
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * 1.21.1 (versions/1.21.1) dynamic lens/filter item model. Emits the lens geometry (frame cutout + coloured
 * translucent overlay) per-stack via Fabric's FabricBakedModel.emitItemQuads; per-context placement comes from
 * getTransforms() (the XFORM table). Vanilla 1.21.1 lacks ItemModel / applyTransform.
 */
public class LensItemModel implements BakedModel {
   private static final RenderMaterial MATERIAL = RendererAccess.INSTANCE.getRenderer().materialFinder().find();
   private static final ItemTransforms TRANSFORMS = buildTransforms();
   private static final LoadingCache<LensKey, List<BakedQuad>> cache = CacheBuilder.newBuilder()
      .expireAfterAccess(1L, TimeUnit.MINUTES)
      .build(CacheLoader.from(lk -> {
         List<BakedQuad> quads = new ArrayList<>();
         for (MutableQuad mq : PlugBakerLens.bakeForItem(lk.colour(), lk.isFilter(), true)) {
            transformForItem(mq, true);
            quads.add(mq.toBakedItem());
         }
         for (MutableQuad mq : PlugBakerLens.bakeForItem(lk.colour(), lk.isFilter(), false)) {
            transformForItem(mq, false);
            quads.add(mq.toBakedTranslucent());
         }
         return quads;
      }));

   public LensItemModel() {
   }

   public static void onModelBake() {
      cache.invalidateAll();
   }

   /**
    * The lens geometry is baked in the plug_lens.json frame (WEST-facing pluggable); per-context placement
    * (incl. the GUI 90deg yaw) comes entirely from getTransforms(), matching the model JSON's display block.
    * No geometry pre-rotation here — that double-rotated the GUI view (lens faced the wrong way / looked oversized).
    */
   private static void transformForItem(MutableQuad mq, boolean resetColors) {
      mq.setShade(false);
      mq.setCalculatedNormal();
      if (resetColors) {
         mq.vertex_0.colouri(255, 255, 255, 255);
         mq.vertex_1.colouri(255, 255, 255, 255);
         mq.vertex_2.colouri(255, 255, 255, 255);
         mq.vertex_3.colouri(255, 255, 255, 255);
      }
   }

   @Override
   public boolean isVanillaAdapter() {
      return false;
   }

   @Override
   public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
      DyeColor colour = ItemPluggableLens.getColour(stack);
      boolean isFilter = ItemPluggableLens.isFilter(stack);
      List<BakedQuad> quads = cache.getUnchecked(new LensKey(colour, isFilter));
      QuadEmitter emitter = context.getEmitter();
      for (BakedQuad quad : quads) {
         emitter.fromVanilla(quad, MATERIAL, null);
         emitter.emit();
      }
   }

   @Override
   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
      return List.of();
   }

   @Override
   public boolean useAmbientOcclusion() {
      return false;
   }

   @Override
   public boolean isGui3d() {
      return true;
   }

   @Override
   public boolean usesBlockLight() {
      return false;
   }

   @Override
   public boolean isCustomRenderer() {
      return false;
   }

   @Override
   public TextureAtlasSprite getParticleIcon() {
      return Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
   }

   @Override
   public ItemTransforms getTransforms() {
      return TRANSFORMS;
   }

   @Override
   public ItemOverrides getOverrides() {
      return ItemOverrides.EMPTY;
   }

   private static ItemTransforms buildTransforms() {
      EnumMap<ItemDisplayContext, float[]> x = new EnumMap<>(ItemDisplayContext.class);
      x.put(ItemDisplayContext.GUI, new float[] { 0, 90, 0, 0, 0, 0, 1.8F });
      x.put(ItemDisplayContext.GROUND, new float[] { 0, 0, 0, 0, 3, 0, 0.9F });
      x.put(ItemDisplayContext.HEAD, new float[] { 0, 0, 0, 0, 0, 0, 1.8F });
      x.put(ItemDisplayContext.FIXED, new float[] { 0, 0, 0, 0, 0, 0, 1.53F });
      x.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new float[] { 0, 45, 0, 0, 0, -4, 0.72F });
      x.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new float[] { 0, 225, 0, 0, 0, -4, 0.72F });
      x.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new float[] { 75, 225, 0, 0, 2.5F, 0, 0.675F });
      x.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new float[] { 75, 45, 0, 0, 2.5F, 0, 0.675F });
      return new ItemTransforms(
         xform(x.get(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)),
         xform(x.get(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)),
         xform(x.get(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)),
         xform(x.get(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)),
         xform(x.get(ItemDisplayContext.HEAD)),
         xform(x.get(ItemDisplayContext.GUI)),
         xform(x.get(ItemDisplayContext.GROUND)),
         xform(x.get(ItemDisplayContext.FIXED))
      );
   }

   private static ItemTransform xform(float[] v) {
      return new ItemTransform(
         new Vector3f(v[0], v[1], v[2]),
         new Vector3f(v[3] / 16.0F, v[4] / 16.0F, v[5] / 16.0F),
         new Vector3f(v[6], v[6], v[6])
      );
   }

   private record LensKey(@Nullable DyeColor colour, boolean isFilter) {
   }
}
