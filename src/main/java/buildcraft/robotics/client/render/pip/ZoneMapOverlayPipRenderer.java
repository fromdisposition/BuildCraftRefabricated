/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render.pip;

//? if >= 1.21.10 {

import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.GeometrySink;
import buildcraft.robotics.zone.ZonePlannerChunkKeys;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
*///?}
//? if >= 26.1 {
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
//?} else {
/*import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
*///?}
import org.joml.Matrix4f;

public class ZoneMapOverlayPipRenderer extends ZoneMapPipRendererBase<ZoneMapOverlayPipRenderState> {
   private static final int OVERLAY_ALPHA = 0x55;
   private static final int SELECTION_ALPHA = 0x99;
   private static final int HOVER_ALPHA = 0x99;
   private long lastStamp = Long.MIN_VALUE;
   // Single-slot bakes for the painted-zone overlay and the drag selection (see emitOverlay).
   private BakedBoxes overlayBaked;
   private int overlayBakedStamp;
   private int overlayBakedTerrain;
   private BakedBoxes selectionBaked;
   private int selBakedX0;
   private int selBakedZ0;
   private int selBakedX1;
   private int selBakedZ1;
   private int selBakedColour;
   private int selBakedTerrain;


   //? if >= 26.2 {
   public ZoneMapOverlayPipRenderer() {
      super();
   }
   //?} else {
   /*public ZoneMapOverlayPipRenderer(BufferSource bufferSource) {
      super(bufferSource);
   }
   *///?}

   @Override
   public Class<ZoneMapOverlayPipRenderState> getRenderStateClass() {
      return ZoneMapOverlayPipRenderState.class;
   }

   @Override
   protected String getTextureLabel() {
      return "buildcraft_zone_map_overlay";
   }

   @Override
   protected boolean textureIsReadyToBlit(ZoneMapOverlayPipRenderState state) {
      return state.map().overlayRenderStamp() == this.lastStamp;
   }

   @Override
   protected ZoneMapPipRenderState camera(ZoneMapOverlayPipRenderState state) {
      return state.map();
   }

   @Override
   protected void beforeRender(ZoneMapOverlayPipRenderState state) {
      this.lastStamp = state.map().overlayRenderStamp();
   }

   @Override
   protected void emit(ZoneMapOverlayPipRenderState state, PoseStack poseStack, GeometrySink sink) {
      sink.submit(poseStack, BCLibRenderTypes.zoneMap(true), (pose, vc) -> this.emitOverlay(state.map(), pose, vc));
   }

   // Shared by every version branch: both paths hand in the terrain-layer VertexConsumer. The painted zones and
   // the drag selection are baked into flat vertex arrays (with row-greedy merging) and re-baked only when their
   // inputs change — the old per-cell path re-did a height lookup and emitted a full five-face cuboid for every
   // painted cell on every texture re-render, which is what made panning a map with a large zone crawl.
   private void emitOverlay(ZoneMapPipRenderState state, Pose pose, VertexConsumer vc) {
      ZonePlannerMapColours cache = state.colours();
      int originX = state.originX();
      int originZ = state.originZ();

      int[] cells = state.overlayCells();
      if (cells != null && cells.length != 0) {
         if (this.overlayBaked == null || this.overlayBakedStamp != state.overlayStamp() || this.overlayBakedTerrain != state.terrainVersion()) {
            this.overlayBaked = BakedBoxes.bakeCells(cache, cells, state.overlayColours(), state.overlayColour(), OVERLAY_ALPHA);
            this.overlayBakedStamp = state.overlayStamp();
            this.overlayBakedTerrain = state.terrainVersion();
         }

         this.overlayBaked.emit(vc, pose, originX, originZ);
      } else {
         this.overlayBaked = null;
      }

      if (state.hasSelection()) {
         if (this.selectionBaked == null
            || this.selBakedX0 != state.selX0()
            || this.selBakedZ0 != state.selZ0()
            || this.selBakedX1 != state.selX1()
            || this.selBakedZ1 != state.selZ1()
            || this.selBakedColour != state.selColour()
            || this.selBakedTerrain != state.terrainVersion()) {
            int minX = Math.min(state.selX0(), state.selX1());
            int maxX = Math.max(state.selX0(), state.selX1());
            int minZ = Math.min(state.selZ0(), state.selZ1());
            int maxZ = Math.max(state.selZ0(), state.selZ1());
            this.selectionBaked = BakedBoxes.bakeRect(cache, minX, maxX, minZ, maxZ, state.selColour(), SELECTION_ALPHA);
            this.selBakedX0 = state.selX0();
            this.selBakedZ0 = state.selZ0();
            this.selBakedX1 = state.selX1();
            this.selBakedZ1 = state.selZ1();
            this.selBakedColour = state.selColour();
            this.selBakedTerrain = state.terrainVersion();
         }

         this.selectionBaked.emit(vc, pose, originX, originZ);
      } else {
         this.selectionBaked = null;
      }

      if (state.hasHover()) {
         int wx = state.hoverX();
         int wz = state.hoverZ();
         long key = ZonePlannerChunkKeys.chunkKey(wx >> 4, wz >> 4);
         int h = cache.heightAt(key, wx, wz);
         if (h != ZonePlannerMapColours.NO_HEIGHT) {
            int c = cache.colourAt(key, wx, wz);
            int r = (int)((c >> 16 & 0xFF) * 0.7F);
            int g = (int)((c >> 8 & 0xFF) * 0.7F);
            int b = (int)((c & 0xFF) * 0.7F);
            float x0 = wx - originX;
            float z0 = wz - originZ;
            emitFilledCuboid(vc, pose, x0 - 0.04F, z0 - 0.04F, x0 + 1.04F, z0 + 1.04F, h + 0.5F, h + 1.7F, r, g, b, HOVER_ALPHA);
         }
      }
   }

   private static void emitFilledCuboid(
      VertexConsumer vc, Pose pose, float x0, float z0, float x1, float z1, float yb, float yt, int r, int g, int b, int a
   ) {
      emitQuad(vc, pose, x0, yt, z1, x1, yt, z1, x1, yt, z0, x0, yt, z0, r, g, b, a);
      emitQuad(vc, pose, x0, yb, z0, x0, yt, z0, x1, yt, z0, x1, yb, z0, r, g, b, a);
      emitQuad(vc, pose, x1, yb, z1, x1, yt, z1, x0, yt, z1, x0, yb, z1, r, g, b, a);
      emitQuad(vc, pose, x0, yb, z1, x0, yt, z1, x0, yt, z0, x0, yb, z0, r, g, b, a);
      emitQuad(vc, pose, x1, yb, z0, x1, yt, z0, x1, yt, z1, x1, yb, z1, r, g, b, a);
   }

   private static void emitQuad(
      VertexConsumer vc, Pose pose, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz,
      float dx, float dy, float dz, int r, int g, int b, int a
   ) {
      vc.addVertex(pose, ax, ay, az).setColor(r, g, b, a).setUv(0.0F, 0.0F).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(pose, bx, by, bz).setColor(r, g, b, a).setUv(0.0F, 0.0F).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(pose, cx, cy, cz).setColor(r, g, b, a).setUv(0.0F, 0.0F).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(pose, dx, dy, dz).setColor(r, g, b, a).setUv(0.0F, 0.0F).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
   }

   @Override
   public void close() {
      super.close();
      this.overlayBaked = null;
      this.selectionBaked = null;
   }

   /**
    * Painted-zone / selection boxes baked into flat vertex arrays with row-greedy merging: adjacent cells of
    * equal colour and terrain height collapse into one cuboid (the coincident walls between them disappear,
    * which also removes the double-blended seam lines the per-cell cuboids used to draw), and the per-cell
    * height lookups run once at bake time instead of on every texture re-render.
    */
   private static final class BakedBoxes {
      private final float[] px;
      private final float[] py;
      private final float[] pz;
      private final int[] argb;

      private BakedBoxes(float[] px, float[] py, float[] pz, int[] argb) {
         this.px = px;
         this.py = py;
         this.pz = pz;
         this.argb = argb;
      }

      void emit(VertexConsumer vc, Pose pose, int originX, int originZ) {
         for (int i = 0; i < this.argb.length; i++) {
            int c = this.argb[i];
            vc.addVertex(pose, this.px[i] - originX, this.py[i], this.pz[i] - originZ)
               .setColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, c >>> 24)
               .setUv(0.0F, 0.0F)
               .setLight(LightCoordsUtil.FULL_BRIGHT)
               .setNormal(0.0F, 1.0F, 0.0F);
         }
      }

      private static int colourOf(int[] cellColours, int index, int single) {
         return cellColours != null && index < cellColours.length ? cellColours[index] : single;
      }

      static BakedBoxes bakeCells(ZonePlannerMapColours cache, int[] cells, int[] cellColours, int single, int alpha) {
         Map<Integer, LongArrayList> rows = new HashMap<>();

         for (int i = 0; i + 1 < cells.length; i += 2) {
            if ((colourOf(cellColours, i / 2, single) >>> 24) != 0) {
               rows.computeIfAbsent(cells[i + 1], k -> new LongArrayList()).add((long)cells[i] << 32 | (i / 2 & 0xFFFFFFFFL));
            }
         }

         FloatList xs = new FloatList();
         FloatList ys = new FloatList();
         FloatList zs = new FloatList();
         IntList cs = new IntList();

         for (Map.Entry<Integer, LongArrayList> row : rows.entrySet()) {
            int wz = row.getKey();
            long[] sorted = row.getValue().toLongArray();
            Arrays.sort(sorted);
            int k = 0;

            while (k < sorted.length) {
               int x = (int)(sorted[k] >> 32);
               int colour = colourOf(cellColours, (int)sorted[k], single);
               k++;
               int h = cache.heightAt(ZonePlannerChunkKeys.chunkKey(x >> 4, wz >> 4), x, wz);
               if (h == ZonePlannerMapColours.NO_HEIGHT) {
                  continue;
               }

               int runEnd = x;

               while (k < sorted.length) {
                  int nx = (int)(sorted[k] >> 32);
                  if (nx != runEnd + 1 || colourOf(cellColours, (int)sorted[k], single) != colour) {
                     break;
                  }

                  if (cache.heightAt(ZonePlannerChunkKeys.chunkKey(nx >> 4, wz >> 4), nx, wz) != h) {
                     break;
                  }

                  runEnd = nx;
                  k++;
               }

               addCuboid(xs, ys, zs, cs, x, wz, runEnd + 1, wz + 1, h + 1.02F, h + 2.0F, alpha << 24 | colour & 0xFFFFFF);
            }
         }

         return new BakedBoxes(xs.toArray(), ys.toArray(), zs.toArray(), cs.toArray());
      }

      static BakedBoxes bakeRect(ZonePlannerMapColours cache, int minX, int maxX, int minZ, int maxZ, int colour, int alpha) {
         FloatList xs = new FloatList();
         FloatList ys = new FloatList();
         FloatList zs = new FloatList();
         IntList cs = new IntList();
         int argb = alpha << 24 | colour & 0xFFFFFF;

         for (int wz = minZ; wz <= maxZ; wz++) {
            int wx = minX;

            while (wx <= maxX) {
               int h = cache.heightAt(ZonePlannerChunkKeys.chunkKey(wx >> 4, wz >> 4), wx, wz);
               if (h == ZonePlannerMapColours.NO_HEIGHT) {
                  wx++;
                  continue;
               }

               int runStart = wx;

               do {
                  wx++;
               } while (wx <= maxX && cache.heightAt(ZonePlannerChunkKeys.chunkKey(wx >> 4, wz >> 4), wx, wz) == h);

               addCuboid(xs, ys, zs, cs, runStart, wz, wx, wz + 1, h + 1.02F, h + 2.0F, argb);
            }
         }

         return new BakedBoxes(xs.toArray(), ys.toArray(), zs.toArray(), cs.toArray());
      }

      /** Same five faces and windings as the old per-cell emitFilledCuboid, spanning a merged run. */
      private static void addCuboid(
         FloatList xs, FloatList ys, FloatList zs, IntList cs, float x0, float z0, float x1, float z1, float yb, float yt, int argb
      ) {
         addQuad(xs, ys, zs, cs, x0, yt, z1, x1, yt, z1, x1, yt, z0, x0, yt, z0, argb);
         addQuad(xs, ys, zs, cs, x0, yb, z0, x0, yt, z0, x1, yt, z0, x1, yb, z0, argb);
         addQuad(xs, ys, zs, cs, x1, yb, z1, x1, yt, z1, x0, yt, z1, x0, yb, z1, argb);
         addQuad(xs, ys, zs, cs, x0, yb, z1, x0, yt, z1, x0, yt, z0, x0, yb, z0, argb);
         addQuad(xs, ys, zs, cs, x1, yb, z0, x1, yt, z0, x1, yt, z1, x1, yb, z1, argb);
      }

      private static void addQuad(
         FloatList xs, FloatList ys, FloatList zs, IntList cs, float ax, float ay, float az, float bx, float by, float bz,
         float cx, float cy, float cz, float dx, float dy, float dz, int argb
      ) {
         xs.add(ax);
         ys.add(ay);
         zs.add(az);
         cs.add(argb);
         xs.add(bx);
         ys.add(by);
         zs.add(bz);
         cs.add(argb);
         xs.add(cx);
         ys.add(cy);
         zs.add(cz);
         cs.add(argb);
         xs.add(dx);
         ys.add(dy);
         zs.add(dz);
         cs.add(argb);
      }
   }

}
//?}
