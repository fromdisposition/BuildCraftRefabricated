/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.silicon.client.model.plug.PlugBakerFacade;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
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

// vanilla 1.21.1 has no ItemModel/per-context applyTransform, so item quads are emitted via Fabric's FabricBakedModel.emitItemQuads.
public class FacadeItemModel implements BakedModel {
   // separate cutout/translucent materials: the default finder picks an opaque layer, which drops alpha for translucent source blocks (stained glass, ice).
   private static final RenderMaterial CUTOUT_MATERIAL =
      RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(BlendMode.CUTOUT).find();
   private static final RenderMaterial TRANSLUCENT_MATERIAL =
      RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(BlendMode.TRANSLUCENT).find();
   private static final LoadingCache<KeyPlugFacade, List<BakedQuad>> guiCache = CacheBuilder.newBuilder()
      .expireAfterAccess(1L, TimeUnit.MINUTES)
      .build(CacheLoader.from(key -> {
         List<BakedQuad> quads = new ArrayList<>();
         float offsetZ = 0.4375F;
         for (MutableQuad quad : PlugBakerFacade.INSTANCE.bakeForKey(key)) {
            quad.setShade(false);
            // bake the base (world-less) tint here; resolveWorldTint(null) returns the default colour used for biome-tinted blocks in the inventory.
            int tint = quad.getTint();
            int rgb = tint >= 0 ? key.resolveWorldTint(tint, null, null) : -1;
            int r = rgb == -1 ? 255 : rgb >> 16 & 0xFF;
            int g = rgb == -1 ? 255 : rgb >> 8 & 0xFF;
            int b = rgb == -1 ? 255 : rgb & 0xFF;
            quad.vertex_0.translatef(0.0F, 0.0F, offsetZ).normalf(0.0F, 1.0F, 0.0F).colouri(r, g, b, 255);
            quad.vertex_1.translatef(0.0F, 0.0F, offsetZ).normalf(0.0F, 1.0F, 0.0F).colouri(r, g, b, 255);
            quad.vertex_2.translatef(0.0F, 0.0F, offsetZ).normalf(0.0F, 1.0F, 0.0F).colouri(r, g, b, 255);
            quad.vertex_3.translatef(0.0F, 0.0F, offsetZ).normalf(0.0F, 1.0F, 0.0F).colouri(r, g, b, 255);
            quads.add(quad.toBakedItem());
         }
         return quads;
      }));

   public FacadeItemModel() {
   }

   public static void onModelBake() {
      guiCache.invalidateAll();
   }

   @Override
   public boolean isVanillaAdapter() {
      return false;
   }

   @Override
   public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
      FacadeInstance inst = ItemPluggableFacade.getStates(stack);
      FacadePhasedState state = inst.getCurrentStateForStack();
      KeyPlugFacade key = new KeyPlugFacade("item", Direction.NORTH, state.stateInfo.state, inst.isHollow());
      List<BakedQuad> quads = guiCache.getUnchecked(key);
      RenderMaterial material = ItemBlockRenderTypes.getChunkRenderType(state.stateInfo.state) == RenderType.translucent()
         ? TRANSLUCENT_MATERIAL
         : CUTOUT_MATERIAL;
      QuadEmitter emitter = context.getEmitter();
      for (BakedQuad quad : quads) {
         emitter.fromVanilla(quad, material, null);
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
      // false avoids the block-camera tilt, which shows the slab's back; the GUI transform below rotates 180 deg to face the viewer instead.
      return false;
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

   // GUI transform must stay flat (rot 0, scale 1); the block tilt (30/225) turns the flat slab into a 3D block.
   private static final ItemTransforms TRANSFORMS = buildBlockTransforms();

   private static ItemTransforms buildBlockTransforms() {
      return new ItemTransforms(
         xform(75, 45, 0, 0, 2.5F, 0, 0.375F),   // thirdperson_lefthand
         xform(75, 45, 0, 0, 2.5F, 0, 0.375F),   // thirdperson_righthand
         xform(0, 225, 0, 0, 0, 0, 0.4F),        // firstperson_lefthand
         xform(0, 45, 0, 0, 0, 0, 0.4F),         // firstperson_righthand
         xform(0, 0, 0, 0, 0, 0, 1.0F),          // head
         xform(0, 180, 0, 0, 0, 0, 1.0F),        // gui
         xform(0, 0, 0, 0, 3, 0, 0.25F),         // ground
         xform(0, 0, 0, 0, 0, 0, 0.5F)           // fixed
      );
   }

   private static ItemTransform xform(float rx, float ry, float rz, float tx, float ty, float tz, float scale) {
      return new ItemTransform(new Vector3f(rx, ry, rz), new Vector3f(tx / 16.0F, ty / 16.0F, tz / 16.0F), new Vector3f(scale, scale, scale));
   }
}
