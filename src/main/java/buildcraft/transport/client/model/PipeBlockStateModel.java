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
import net.minecraft.world.level.BlockAndTintGetter;
*///?}
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
//? if >= 26.1 {
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material.Baked;
//?} else {
/*import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
*///?}
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pipe block model. The vanilla delegate carries the (empty) baked state; on top of it this emits the pipe
 * PAINT shell as chunk translucent geometry via the Fabric renderer API, driven by the tile's render data
 * ({@link PipeModelKey} snapshot).
 *
 * <p>The paint used to be a block-entity submit, but BER translucency draws in a separate pass from
 * translucent terrain, so it can never sort against water/oil: either the paint's depth writes hid the fluid
 * behind the pipe, or (without depth writes) the fluid drew over the paint and washed the tint out, plus the
 * BER-vs-terrain transform mismatch z-fought on shared planes. As chunk geometry the shell is depth-written,
 * sorted per-quad against fluids in the same translucent pass — exactly how stained glass behaves — and costs
 * nothing per frame (rebuilt only when the pipe's colour/connections change, via the existing
 * sendBlockUpdated in TilePipeHolder#refreshClientModel).
 */
public class PipeBlockStateModel implements BlockStateModel {
   private static final float BOUNDARY_EPS = 1.0E-4F;
   private final BlockStateModel vanillaDelegate;

   public PipeBlockStateModel(BlockStateModel vanillaDelegate) {
      this.vanillaDelegate = vanillaDelegate;
   }

   //? if >= 26.1 {
   public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
   //?} else {
   /*public void collectParts(RandomSource random, List<BlockModelPart> parts) {
   *///?}
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
      if (blockView.getBlockEntityRenderData(pos) instanceof PipeModelKey key && key.colour != null) {
         int alpha = key.definition != null && key.definition.flowType == PipeApi.flowFluids ? 255 : ModelPipe.PIPE_PAINT_ALPHA;
         emitPaint(emitter, key, alpha, cullTest);
      }
   }

   private static void emitPaint(QuadEmitter emitter, PipeModelKey key, int alpha, Predicate<Direction> cullTest) {
      List<MutableQuad> quads = PipeMutableQuadCache.maskQuads(new PipeModelCacheBase.PipeBaseCutoutKey(key), alpha);

      for (int i = 0; i < quads.size(); i++) {
         MutableQuad q = quads.get(i);
         Direction boundary = boundaryFace(q);
         if (boundary == null || !cullTest.test(boundary)) {
            emitQuad(emitter, q);
         }
      }
   }

   private static void emitQuad(QuadEmitter emitter, MutableQuad q) {
      emitVertex(emitter, 0, q.vertex_0);
      emitVertex(emitter, 1, q.vertex_1);
      emitVertex(emitter, 2, q.vertex_2);
      emitVertex(emitter, 3, q.vertex_3);
      //? if >= 26.1 {
      emitter.chunkLayer(ChunkSectionLayer.TRANSLUCENT);
      //?} else {
      /*emitter.renderLayer(ChunkSectionLayer.TRANSLUCENT);
      *///?}
      // Flat like the old overlay: the shell hugs the already-shaded pipe body, so diffuse/AO would double-darken.
      emitter.diffuseShade(false);
      emitter.ambientOcclusion(TriState.FALSE);
      emitter.emit();
   }

   private static void emitVertex(QuadEmitter emitter, int i, MutableVertex v) {
      emitter.pos(i, v.position_x, v.position_y, v.position_z);
      emitter.color(i, (v.colour_a & 0xFF) << 24 | (v.colour_r & 0xFF) << 16 | (v.colour_g & 0xFF) << 8 | v.colour_b & 0xFF);
      emitter.uv(i, v.tex_u, v.tex_v);
   }

   /**
    * The block face this quad lies flat on (all four vertices at 0 or 1 on one axis), or null. Caps produced by
    * connections end exactly on the block boundary; testing them against the neighbour via the model's cull
    * test culls them like vanilla full-block faces, which both skips hidden geometry and avoids z-fighting a
    * solid neighbour's coplanar face.
    */
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
