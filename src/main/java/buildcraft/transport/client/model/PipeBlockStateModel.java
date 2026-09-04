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
import java.util.function.Predicate;
//? if >= 26.1 {
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
//?} else {
/*import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
*///?}
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
//? if >= 26.1 {
import net.minecraft.client.resources.model.sprite.Material.Baked;
//?} else {
/*import net.minecraft.client.renderer.texture.TextureAtlasSprite;
*///?}
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rebuilt only when a pipe's colour or connections change, via TilePipeHolder#refreshClientModel.
 * Paint must be chunk geometry: BER translucency draws in a separate pass and cannot sort against water/oil.
 */
public class PipeBlockStateModel implements BlockStateModel {
   private static final float BOUNDARY_EPS = 1.0E-4F;
   private final BlockStateModel vanillaDelegate;

   public PipeBlockStateModel(BlockStateModel vanillaDelegate) {
      this.vanillaDelegate = vanillaDelegate;
   }

   public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
      this.vanillaDelegate.collectParts(random, parts);
   }

   //? if >= 26.1 {
   public Baked particleMaterial() {
      return this.vanillaDelegate.particleMaterial();
   }

   public int materialFlags() {
      return this.vanillaDelegate.materialFlags();
   }
   //?} else {
   /*// 1.21.x BlockStateModel exposes particleIcon() (a sprite) and has no materialFlags().
   public TextureAtlasSprite particleIcon() {
      return this.vanillaDelegate.particleIcon();
   }
   *///?}

   @Override
   public void emitQuads(
      QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest
   ) {
      this.vanillaDelegate.emitQuads(emitter, blockView, pos, state, random, cullTest);
      if (blockView.getBlockEntityRenderData(pos) instanceof PipeModelKey key) {
         PipeModelCacheBase.PipeBaseCutoutKey cutoutKey = new PipeModelCacheBase.PipeBaseCutoutKey(key);
         emitQuads(emitter, PipeMutableQuadCache.cutoutQuads(cutoutKey), ChunkSectionLayer.CUTOUT, cullTest);
         if (key.colour != null) {
            int alpha = key.definition != null && key.definition.flowType == PipeApi.flowFluids ? 255 : ModelPipe.PIPE_PAINT_ALPHA;
            emitQuads(emitter, PipeMutableQuadCache.maskQuads(cutoutKey, alpha), ChunkSectionLayer.TRANSLUCENT, cullTest);
         }
      }
   }

   private static void emitQuads(QuadEmitter emitter, List<MutableQuad> quads, ChunkSectionLayer layer, Predicate<Direction> cullTest) {
      for (int i = 0; i < quads.size(); i++) {
         MutableQuad q = quads.get(i);
         Direction boundary = boundaryFace(q);
         if (boundary == null || !cullTest.test(boundary)) {
            emitQuad(emitter, q, layer);
         }
      }
   }

   private static void emitQuad(QuadEmitter emitter, MutableQuad q, ChunkSectionLayer layer) {
      emitVertex(emitter, 0, q.vertex_0);
      emitVertex(emitter, 1, q.vertex_1);
      emitVertex(emitter, 2, q.vertex_2);
      emitVertex(emitter, 3, q.vertex_3);
      //? if >= 26.1 {
      emitter.chunkLayer(layer);
      //?} else {
      /*emitter.renderLayer(layer);
      *///?}
      // Flat shading: templates bake their own colours, and diffuse/AO would double-darken the shell against the body.
      emitter.shadeDirectionOverride(net.minecraft.core.Direction.UP);
      emitter.ambientOcclusion(TriState.FALSE);
      emitter.emit();
   }

   private static void emitVertex(QuadEmitter emitter, int i, MutableVertex v) {
      emitter.pos(i, v.position_x, v.position_y, v.position_z);
      emitter.color(i, (v.colour_a & 0xFF) << 24 | (v.colour_r & 0xFF) << 16 | (v.colour_g & 0xFF) << 8 | v.colour_b & 0xFF);
      emitter.uv(i, v.tex_u, v.tex_v);
   }

   /** Used to cull boundary caps like vanilla full-block faces: skips hidden geometry and avoids z-fighting the neighbour. */
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
}
