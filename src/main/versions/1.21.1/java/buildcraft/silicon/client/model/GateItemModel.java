/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.plug.PlugGateBaker;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableGate;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * 1.21.1 (versions/1.21.1) dynamic gate item model. Vanilla 1.21.1 has no ItemModel/ItemStackRenderState and
 * its ItemOverrides cannot be subclassed (private ctor) and BakedModel has no applyTransform/getRenderTypes
 * (those are Forge-only), so per-stack geometry is emitted through Fabric's FabricBakedModel.emitItemQuads:
 * the gate variant is read from the stack, base NORTH-facing quads from PlugGateBaker are emitted, and the
 * per-ItemDisplayContext camera placement is supplied by getTransforms() (built from the XFORM table).
 */
public class GateItemModel implements BakedModel {
   private static final RenderMaterial MATERIAL = RendererAccess.INSTANCE.getRenderer().materialFinder().find();
   private static final ItemTransforms TRANSFORMS = buildTransforms();
   private static final LoadingCache<GateVariant, List<BakedQuad>> cache = CacheBuilder.newBuilder()
      .expireAfterAccess(1L, TimeUnit.MINUTES)
      .build(CacheLoader.from(variant -> {
         List<BakedQuad> quads = new ArrayList<>();
         for (MutableQuad mq : PlugGateBaker.INSTANCE.bakeForItem(variant)) {
            mq.setShade(false);
            mq.setCalculatedNormal();
            quads.add(mq.toBakedItem());
         }
         return quads;
      }));

   public GateItemModel() {
   }

   public static void onModelBake() {
      cache.invalidateAll();
   }

   @Override
   public boolean isVanillaAdapter() {
      return false;
   }

   @Override
   public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
      GateVariant variant = ItemPluggableGate.getVariant(stack);
      if (variant == null) {
         return;
      }
      List<BakedQuad> quads = cache.getUnchecked(variant);
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

   /** Per-context camera transforms (degrees / 1-16 units / uniform scale), matching the modern XFORM table. */
   private static ItemTransforms buildTransforms() {
      EnumMap<ItemDisplayContext, float[]> x = new EnumMap<>(ItemDisplayContext.class);
      // rotX, rotY, rotZ, tx, ty, tz, scale
      x.put(ItemDisplayContext.GUI, new float[] { 0, 0, 0, 0, 0, 0, 1.8F });
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
}
