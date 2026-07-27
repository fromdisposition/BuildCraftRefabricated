/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render.pip;

import buildcraft.lib.client.render.BCLibRenderTypes;
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

public class ZoneMapPipRenderer extends PictureInPictureRenderer<ZoneMapPipRenderState> {
   private static final int OVERLAY_ALPHA = 0x55;
   private static final int SELECTION_ALPHA = 0x99;
   private static final int HOVER_ALPHA = 0x99;
   //? if >= 26.1 {
   private ProjectionMatrixBuffer perspBuffer;
   private ProjectionMatrixBuffer orthoRestoreBuffer;
   private final Projection orthoRestore = new Projection();
   //?} else {
   /*private PerspectiveProjectionMatrixBuffer perspBuffer;
   *///?}
   private final it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<CachedMesh> meshCache = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>();
   private long lastStamp = Long.MIN_VALUE;
   // Terrain version the cached chunk meshes were last validated against: chunk data can only change by
   // bumping the cache's global version (already part of the render stamp), so while it is unchanged every
   // cached mesh is valid and meshFor can skip the five per-chunk neighbour-version lookups per frame.
   private int lastMeshValidationVersion = Integer.MIN_VALUE;
   private boolean revalidateMeshes = true;
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
   public ZoneMapPipRenderer() {
      super();
   }
   //?} else {
   /*public ZoneMapPipRenderer(BufferSource bufferSource) {
      super(bufferSource);
   }
   *///?}

   @Override
   public Class<ZoneMapPipRenderState> getRenderStateClass() {
      return ZoneMapPipRenderState.class;
   }

   @Override
   protected String getTextureLabel() {
      return "buildcraft_zone_map";
   }

   @Override
   protected boolean textureIsReadyToBlit(ZoneMapPipRenderState state) {
      return state.renderStamp() == this.lastStamp;
   }

   //? if >= 26.2 {
   @Override
   protected void renderToTexture(ZoneMapPipRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      this.lastStamp = state.renderStamp();
      this.revalidateMeshes = state.terrainVersion() != this.lastMeshValidationVersion;
      this.lastMeshValidationVersion = state.terrainVersion();
      this.evictFarMeshes(state);
      Minecraft mc = Minecraft.getInstance();

      if (this.perspBuffer == null) {
         this.perspBuffer = new ProjectionMatrixBuffer("PIP zone map persp");
      }

      RenderSystem.setProjectionMatrix(this.perspBuffer.getBuffer(state.projMatrix()), ProjectionType.PERSPECTIVE);
      Pose pose = poseStack.last();
      pose.pose().set(state.viewMatrix());

      this.emitTerrain(state, poseStack, (SubmitNodeStorage) submitNodeCollector);

      mc.gameRenderer.featureRenderDispatcher().renderAllFeatures((SubmitNodeStorage) submitNodeCollector);
   }
   //?} else if >= 26.1 {
   /*@Override
   protected void renderToTexture(ZoneMapPipRenderState state, PoseStack poseStack) {
      this.lastStamp = state.renderStamp();
      this.revalidateMeshes = state.terrainVersion() != this.lastMeshValidationVersion;
      this.lastMeshValidationVersion = state.terrainVersion();
      this.evictFarMeshes(state);
      Minecraft mc = Minecraft.getInstance();
      int guiScale = (int)mc.getWindow().getGuiScale();
      int width = (state.x1() - state.x0()) * guiScale;
      int height = (state.y1() - state.y0()) * guiScale;

      if (this.perspBuffer == null) {
         this.perspBuffer = new ProjectionMatrixBuffer("PIP zone map persp");
         this.orthoRestoreBuffer = new ProjectionMatrixBuffer("PIP zone map ortho");
      }

      RenderSystem.setProjectionMatrix(this.perspBuffer.getBuffer(state.projMatrix()), ProjectionType.PERSPECTIVE);
      Pose pose = poseStack.last();
      pose.pose().set(state.viewMatrix());

      this.emitTerrain(state, pose);
      this.bufferSource.endBatch();

      this.orthoRestore.setupOrtho(-1000.0F, 1000.0F, width, height, true);
      RenderSystem.setProjectionMatrix(this.orthoRestoreBuffer.getBuffer(this.orthoRestore), ProjectionType.ORTHOGRAPHIC);
   }
   *///?} else {
   /*@Override
   protected void renderToTexture(ZoneMapPipRenderState state, PoseStack poseStack) {
      this.lastStamp = state.renderStamp();
      this.revalidateMeshes = state.terrainVersion() != this.lastMeshValidationVersion;
      this.lastMeshValidationVersion = state.terrainVersion();
      this.evictFarMeshes(state);
      if (this.perspBuffer == null) {
         this.perspBuffer = new PerspectiveProjectionMatrixBuffer("PIP zone map persp");
      }

      // backup/restore returns the GUI's ortho projection that the PIP framework set up.
      RenderSystem.backupProjectionMatrix();
      RenderSystem.setProjectionMatrix(this.perspBuffer.getBuffer(state.projMatrix()), ProjectionType.PERSPECTIVE);
      Pose pose = poseStack.last();
      pose.pose().set(state.viewMatrix());
      this.emitTerrain(state, pose);
      this.bufferSource.endBatch();
      RenderSystem.restoreProjectionMatrix();
   }
   *///?}

   //? if >= 26.2 {
   private void emitTerrain(ZoneMapPipRenderState state, PoseStack poseStack, SubmitNodeStorage storage) {
      ZonePlannerMapColours cache = state.colours();
      if (cache == null) return;
      int originX = state.originX();
      int originZ = state.originZ();
      int cx0 = state.minChunkX();
      int cx1 = state.maxChunkX();
      int cz0 = state.minChunkZ();
      int cz1 = state.maxChunkZ();
      storage.submitCustomGeometry(poseStack, BCLibRenderTypes.debugSolid(), (pose, vc) -> {
         for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
               long key = ZonePlannerChunkKeys.chunkKey(cx, cz);
               if (cache.versionOf(key) != 0) {
                  CachedMesh mesh = this.meshFor(cache, cx, cz, key);
                  mesh.emit(vc, pose, originX, originZ);
               }
            }
         }

         this.emitOverlay(state, pose, vc);
      });
   }
   //?} else {
   /*private void emitTerrain(ZoneMapPipRenderState state, Pose pose) {
      ZonePlannerMapColours cache = state.colours();
      if (cache != null) {
         int originX = state.originX();
         int originZ = state.originZ();
         int cx0 = state.minChunkX();
         int cx1 = state.maxChunkX();
         int cz0 = state.minChunkZ();
         int cz1 = state.maxChunkZ();
         VertexConsumer vc = this.bufferSource.getBuffer(BCLibRenderTypes.debugSolid());

         for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
               long key = ZonePlannerChunkKeys.chunkKey(cx, cz);
               if (cache.versionOf(key) != 0) {
                  CachedMesh mesh = this.meshFor(cache, cx, cz, key);
                  mesh.emit(vc, pose, originX, originZ);
               }
            }
         }

         this.emitOverlay(state, pose, vc);
      }
   }
   *///?}


   private void evictFarMeshes(ZoneMapPipRenderState state) {
      if (this.meshCache.size() > 1024) {
         int pad = 2;
         int minX = state.minChunkX() - pad;
         int maxX = state.maxChunkX() + pad;
         int minZ = state.minChunkZ() - pad;
         int maxZ = state.maxChunkZ() + pad;
         this.meshCache.keySet().removeIf(key -> {
            int cx = (int)(key & 0xFFFFFFFFL);
            int cz = (int)(key >> 32);
            return cx < minX || cx > maxX || cz < minZ || cz > maxZ;
         });
      }
   }

   
   private CachedMesh meshFor(ZonePlannerMapColours cache, int cx, int cz, long key) {
      // Fast path (see lastMeshValidationVersion): no chunk data changed since the last texture render, so a
      // cached mesh is valid as-is — one primitive map get instead of five neighbour-version lookups.
      if (!this.revalidateMeshes) {
         CachedMesh cached = this.meshCache.get(key);
         if (cached != null) {
            return cached;
         }
      }

      int selfVer = cache.versionOf(key);
      int vW = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx - 1, cz));
      int vE = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx + 1, cz));
      int vN = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx, cz - 1));
      int vS = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx, cz + 1));
      CachedMesh mesh = this.meshCache.get(key);
      if (mesh == null || !mesh.matches(selfVer, vW, vE, vN, vS)) {
         mesh = CachedMesh.build(cache, cx, cz, selfVer, vW, vE, vN, vS);
         this.meshCache.put(key, mesh);
      }

      return mesh;
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
      vc.addVertex(pose, ax, ay, az).setColor(r, g, b, a);
      vc.addVertex(pose, bx, by, bz).setColor(r, g, b, a);
      vc.addVertex(pose, cx, cy, cz).setColor(r, g, b, a);
      vc.addVertex(pose, dx, dy, dz).setColor(r, g, b, a);
   }

   @Override
   public void close() {
      super.close();
      this.meshCache.clear();
      this.overlayBaked = null;
      this.selectionBaked = null;
      if (this.perspBuffer != null) {
         this.perspBuffer.close();
         this.perspBuffer = null;
      }

      //? if >= 26.1 {
      if (this.orthoRestoreBuffer != null) {
         this.orthoRestoreBuffer.close();
         this.orthoRestoreBuffer = null;
      }
      //?}
   }

   
   private static final class CachedMesh {
      private final int selfVer;
      private final int vW;
      private final int vE;
      private final int vN;
      private final int vS;
      private final float[] px;
      private final float[] py;
      private final float[] pz;
      private final int[] rgb;

      private CachedMesh(int selfVer, int vW, int vE, int vN, int vS, float[] px, float[] py, float[] pz, int[] rgb) {
         this.selfVer = selfVer;
         this.vW = vW;
         this.vE = vE;
         this.vN = vN;
         this.vS = vS;
         this.px = px;
         this.py = py;
         this.pz = pz;
         this.rgb = rgb;
      }

      boolean matches(int selfVer, int vW, int vE, int vN, int vS) {
         return this.selfVer == selfVer && this.vW == vW && this.vE == vE && this.vN == vN && this.vS == vS;
      }

      void emit(VertexConsumer vc, Pose pose, int originX, int originZ) {
         for (int i = 0; i < this.rgb.length; i++) {
            int c = this.rgb[i];
            vc.addVertex(pose, this.px[i] - originX, this.py[i], this.pz[i] - originZ).setColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, 255);
         }
      }

      /**
       * Bakes the chunk with row-greedy merging: a run of cells with the same colour and height along X becomes
       * ONE top quad, and the north/south cliff walls merge over sub-runs of equal neighbour height (west/east
       * walls only exist at run ends, because inside a run the neighbour has the same height). Flat terrain
       * collapses from 256 top quads to ~16, which is what makes re-emitting the visible chunks during
       * pan/zoom/hover cheap. The merged geometry covers exactly the same surface with the same colours and
       * windings as the old per-cell emission, so the map looks identical.
       */
      static CachedMesh build(ZonePlannerMapColours cache, int cx, int cz, int selfVer, int vW, int vE, int vN, int vS) {
         FloatList xs = new FloatList();
         FloatList ys = new FloatList();
         FloatList zs = new FloatList();
         IntList cs = new IntList();
         long key = ZonePlannerChunkKeys.chunkKey(cx, cz);
         int[] cols = cache.coloursOf(key);
         int[] hts = cache.heightsOf(key);
         if (cols != null && hts != null) {
            int[] htsW = cache.heightsOf(ZonePlannerChunkKeys.chunkKey(cx - 1, cz));
            int[] htsE = cache.heightsOf(ZonePlannerChunkKeys.chunkKey(cx + 1, cz));
            int[] htsN = cache.heightsOf(ZonePlannerChunkKeys.chunkKey(cx, cz - 1));
            int[] htsS = cache.heightsOf(ZonePlannerChunkKeys.chunkKey(cx, cz + 1));
            int baseX = cx << 4;
            int baseZ = cz << 4;

            for (int lz = 0; lz < 16; lz++) {
               float z0 = baseZ + lz;
               float z1 = z0 + 1.0F;
               int row = lz * 16;
               int[] nRow = lz > 0 ? hts : htsN;
               int nOff = lz > 0 ? row - 16 : 240;
               int[] sRow = lz < 15 ? hts : htsS;
               int sOff = lz < 15 ? row + 16 : 0;
               int lx = 0;

               while (lx < 16) {
                  int colour = cols[row + lx];
                  if (colour == 0) {
                     lx++;
                     continue;
                  }

                  int h = hts[row + lx];
                  int start = lx;

                  do {
                     lx++;
                  } while (lx < 16 && cols[row + lx] == colour && hts[row + lx] == h);

                  int rgb = colour & 0xFFFFFF;
                  int shaded = darken(rgb);
                  float xA = baseX + start;
                  float xB = baseX + lx;
                  float yTop = h + 1.0F;
                  emitQuadBaked(xs, ys, zs, cs, xA, yTop, z1, xB, yTop, z1, xB, yTop, z0, xA, yTop, z0, rgb);

                  int nh = start > 0 ? hts[row + start - 1] : (htsW != null ? htsW[row + 15] : ZonePlannerMapColours.NO_HEIGHT);
                  if (nh != ZonePlannerMapColours.NO_HEIGHT && nh < h) {
                     float yBot = nh + 1.0F;
                     emitQuadBaked(xs, ys, zs, cs, xA, yBot, z1, xA, yTop, z1, xA, yTop, z0, xA, yBot, z0, shaded);
                  }

                  nh = lx < 16 ? hts[row + lx] : (htsE != null ? htsE[row] : ZonePlannerMapColours.NO_HEIGHT);
                  if (nh != ZonePlannerMapColours.NO_HEIGHT && nh < h) {
                     float yBot = nh + 1.0F;
                     emitQuadBaked(xs, ys, zs, cs, xB, yBot, z0, xB, yTop, z0, xB, yTop, z1, xB, yBot, z1, shaded);
                  }

                  emitWallRuns(xs, ys, zs, cs, nRow, nOff, start, lx, baseX, h, z0, true, shaded);
                  emitWallRuns(xs, ys, zs, cs, sRow, sOff, start, lx, baseX, h, z1, false, shaded);
               }
            }
         }

         return new CachedMesh(selfVer, vW, vE, vN, vS, xs.toArray(), ys.toArray(), zs.toArray(), cs.toArray());
      }

      /** Emits the north/south cliff wall of a run, merged over sub-runs of equal (lower) neighbour height. */
      private static void emitWallRuns(
         FloatList xs, FloatList ys, FloatList zs, IntList cs, int[] nbrRow, int nbrOff, int from, int to, int baseX,
         int h, float z, boolean north, int shaded
      ) {
         if (nbrRow == null) {
            return;
         }

         float yTop = h + 1.0F;
         int i = from;

         while (i < to) {
            int nh = nbrRow[nbrOff + i];
            if (nh == ZonePlannerMapColours.NO_HEIGHT || nh >= h) {
               i++;
               continue;
            }

            int segStart = i;

            do {
               i++;
            } while (i < to && nbrRow[nbrOff + i] == nh);

            float xA = baseX + segStart;
            float xB = baseX + i;
            float yBot = nh + 1.0F;
            if (north) {
               emitQuadBaked(xs, ys, zs, cs, xA, yBot, z, xA, yTop, z, xB, yTop, z, xB, yBot, z, shaded);
            } else {
               emitQuadBaked(xs, ys, zs, cs, xB, yBot, z, xB, yTop, z, xA, yTop, z, xA, yBot, z, shaded);
            }
         }
      }

      private static int darken(int rgb) {
         int r = (rgb >> 16 & 0xFF) * 7 / 10;
         int g = (rgb >> 8 & 0xFF) * 7 / 10;
         int b = (rgb & 0xFF) * 7 / 10;
         return r << 16 | g << 8 | b;
      }

      private static void emitQuadBaked(
         FloatList xs, FloatList ys, FloatList zs, IntList cs, float ax, float ay, float az, float bx, float by, float bz,
         float cx, float cy, float cz, float dx, float dy, float dz, int rgb
      ) {
         xs.add(ax);
         ys.add(ay);
         zs.add(az);
         cs.add(rgb);
         xs.add(bx);
         ys.add(by);
         zs.add(bz);
         cs.add(rgb);
         xs.add(cx);
         ys.add(cy);
         zs.add(cz);
         cs.add(rgb);
         xs.add(dx);
         ys.add(dy);
         zs.add(dz);
         cs.add(rgb);
      }
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
               .setColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, c >>> 24);
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

   private static final class FloatList {
      private float[] data = new float[256];
      private int size;

      void add(float v) {
         if (this.size == this.data.length) {
            float[] grown = new float[this.data.length * 2];
            System.arraycopy(this.data, 0, grown, 0, this.size);
            this.data = grown;
         }

         this.data[this.size++] = v;
      }

      float[] toArray() {
         float[] out = new float[this.size];
         System.arraycopy(this.data, 0, out, 0, this.size);
         return out;
      }
   }

   
   private static final class IntList {
      private int[] data = new int[256];
      private int size;

      void add(int v) {
         if (this.size == this.data.length) {
            int[] grown = new int[this.data.length * 2];
            System.arraycopy(this.data, 0, grown, 0, this.size);
            this.data = grown;
         }

         this.data[this.size++] = v;
      }

      int[] toArray() {
         int[] out = new int[this.size];
         System.arraycopy(this.data, 0, out, 0, this.size);
         return out;
      }
   }
}
