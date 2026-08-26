/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.transport.client.model.key.PipeModelKey;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class PipeBlockStateModel implements BakedModel, FabricBakedModel {
   private static final float BOUNDARY_EPS = 1.0E-4F;
   private final BakedModel vanillaDelegate;
   private final RenderMaterial defaultMaterial;
   private final RenderMaterial cutoutMaterial;
   private final RenderMaterial translucentMaterial;

   public PipeBlockStateModel(BakedModel vanillaDelegate) {
      this.vanillaDelegate = vanillaDelegate;
      Renderer renderer = RendererAccess.INSTANCE.getRenderer();
      if (renderer == null) {
         this.defaultMaterial = null;
         this.cutoutMaterial = null;
         this.translucentMaterial = null;
      } else {
         this.defaultMaterial = renderer.materialFinder().clear().find();
         this.cutoutMaterial = material(renderer, BlendMode.CUTOUT);
         this.translucentMaterial = material(renderer, BlendMode.TRANSLUCENT);
      }
   }

   private static RenderMaterial material(Renderer renderer, BlendMode blendMode) {
      MaterialFinder finder = renderer.materialFinder();
      return finder.clear().blendMode(blendMode).disableDiffuse(true).ambientOcclusion(TriState.FALSE).find();
   }

   @Override
   public boolean isVanillaAdapter() {
      return this.cutoutMaterial == null;
   }

   @Override
   public void emitBlockQuads(
      BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context
   ) {
      QuadEmitter emitter = context.getEmitter();
      this.emitDelegate(emitter, state, random.get());
      if (blockView.getBlockEntityRenderData(pos) instanceof PipeModelKey key) {
         PipeModelCacheBase.PipeBaseCutoutKey cutoutKey = new PipeModelCacheBase.PipeBaseCutoutKey(key);
         emitQuads(emitter, PipeMutableQuadCache.cutoutQuads(cutoutKey), this.cutoutMaterial);
         if (key.colour != null) {
            int alpha = key.definition != null && key.definition.flowType == PipeApi.flowFluids ? 255 : ModelPipe.PIPE_PAINT_ALPHA;
            emitQuads(emitter, PipeMutableQuadCache.maskQuads(cutoutKey, alpha), this.translucentMaterial);
         }
      }
   }

   @Override
   public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
      this.emitDelegate(context.getEmitter(), null, random.get());
   }

   private void emitDelegate(QuadEmitter emitter, BlockState state, RandomSource random) {
      this.emitDelegateFace(emitter, state, null, random);

      for (Direction face : Direction.values()) {
         this.emitDelegateFace(emitter, state, face, random);
      }
   }

   private void emitDelegateFace(QuadEmitter emitter, BlockState state, Direction face, RandomSource random) {
      List<BakedQuad> quads = this.vanillaDelegate.getQuads(state, face, random);

      for (int i = 0; i < quads.size(); i++) {
         emitter.fromVanilla(quads.get(i), this.defaultMaterial, face);
         emitter.emit();
      }
   }

   private static void emitQuads(QuadEmitter emitter, List<MutableQuad> quads, RenderMaterial material) {
      for (int i = 0; i < quads.size(); i++) {
         MutableQuad q = quads.get(i);
         emitVertex(emitter, 0, q.vertex_0);
         emitVertex(emitter, 1, q.vertex_1);
         emitVertex(emitter, 2, q.vertex_2);
         emitVertex(emitter, 3, q.vertex_3);
         emitter.material(material);
         emitter.cullFace(boundaryFace(q));
         emitter.emit();
      }
   }

   private static void emitVertex(QuadEmitter emitter, int i, MutableVertex v) {
      emitter.pos(i, v.position_x, v.position_y, v.position_z);
      emitter.color(i, (v.colour_a & 0xFF) << 24 | (v.colour_r & 0xFF) << 16 | (v.colour_g & 0xFF) << 8 | v.colour_b & 0xFF);
      emitter.uv(i, v.tex_u, v.tex_v);
   }

   private static Direction boundaryFace(MutableQuad q) {
      MutableVertex v0 = q.vertex_0;
      MutableVertex v1 = q.vertex_1;
      MutableVertex v2 = q.vertex_2;
      MutableVertex v3 = q.vertex_3;
      if (flat(v0.position_x, v1.position_x, v2.position_x, v3.position_x)) {
         Direction d = boundaryDir(v0.position_x, Direction.WEST, Direction.EAST);
         if (d != null) {
            return d;
         }
      }

      if (flat(v0.position_y, v1.position_y, v2.position_y, v3.position_y)) {
         Direction d = boundaryDir(v0.position_y, Direction.DOWN, Direction.UP);
         if (d != null) {
            return d;
         }
      }

      if (flat(v0.position_z, v1.position_z, v2.position_z, v3.position_z)) {
         return boundaryDir(v0.position_z, Direction.NORTH, Direction.SOUTH);
      }

      return null;
   }

   private static boolean flat(float a, float b, float c, float d) {
      return Math.abs(a - b) < BOUNDARY_EPS && Math.abs(a - c) < BOUNDARY_EPS && Math.abs(a - d) < BOUNDARY_EPS;
   }

   private static Direction boundaryDir(float coord, Direction min, Direction max) {
      if (coord < BOUNDARY_EPS) {
         return min;
      } else {
         return coord > 1.0F - BOUNDARY_EPS ? max : null;
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
      return this.vanillaDelegate.getQuads(state, side, random);
   }

   @Override
   public boolean useAmbientOcclusion() {
      return this.vanillaDelegate.useAmbientOcclusion();
   }

   @Override
   public boolean isGui3d() {
      return this.vanillaDelegate.isGui3d();
   }

   @Override
   public boolean usesBlockLight() {
      return this.vanillaDelegate.usesBlockLight();
   }

   @Override
   public boolean isCustomRenderer() {
      return this.vanillaDelegate.isCustomRenderer();
   }

   @Override
   public TextureAtlasSprite getParticleIcon() {
      return this.vanillaDelegate.getParticleIcon();
   }

   @Override
   public ItemTransforms getTransforms() {
      return this.vanillaDelegate.getTransforms();
   }

   @Override
   public ItemOverrides getOverrides() {
      return this.vanillaDelegate.getOverrides();
   }
}
