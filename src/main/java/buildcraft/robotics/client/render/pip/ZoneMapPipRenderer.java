/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render.pip;

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
import javax.annotation.Nullable;
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

public class ZoneMapPipRenderer extends ZoneMapPipRendererBase<ZoneMapPipRenderState> {
   @SuppressWarnings("unchecked")
   private final it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<CachedMesh>[] meshCache = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap[ZoneMapPipRenderState.LOD_LEVELS];
   private long lastStamp = Long.MIN_VALUE;
   // Terrain version the cached chunk meshes were last validated against: chunk data can only change by
   // bumping the cache's global version (already part of the render stamp), so while it is unchanged every
   // cached mesh is valid and meshFor can skip the five per-chunk neighbour-version lookups per frame.
   private int lastMeshValidationVersion = Integer.MIN_VALUE;
   private boolean revalidateMeshes = true;

   //? if >= 26.2 {
   public ZoneMapPipRenderer() {
      super();
      this.initMeshCache();
   }
   //?} else {
   /*public ZoneMapPipRenderer(BufferSource bufferSource) {
      super(bufferSource);
      this.initMeshCache();
   }
   *///?}

   private void initMeshCache() {
      for (int lod = 0; lod < this.meshCache.length; lod++) {
         this.meshCache[lod] = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>();
      }
   }

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
      return state.terrainRenderStamp() == this.lastStamp;
   }

   @Override
   protected ZoneMapPipRenderState camera(ZoneMapPipRenderState state) {
      return state;
   }

   @Override
   protected void beforeRender(ZoneMapPipRenderState state) {
      this.lastStamp = state.terrainRenderStamp();
      this.revalidateMeshes = state.terrainVersion() != this.lastMeshValidationVersion;
      this.lastMeshValidationVersion = state.terrainVersion();
      this.evictFarMeshes(state);
   }

   @Override
   protected void emit(ZoneMapPipRenderState state, PoseStack poseStack, GeometrySink sink) {
      ZonePlannerMapColours cache = state.colours();
      if (cache == null) {
         return;
      }

      int originX = state.originX();
      int originZ = state.originZ();
      int cx0 = state.minChunkX();
      int cx1 = state.maxChunkX();
      int cz0 = state.minChunkZ();
      int cz1 = state.maxChunkZ();
      int lod = state.lod();
      sink.submit(poseStack, BCLibRenderTypes.zoneMap(false), (pose, vc) -> {
         for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
               long key = ZonePlannerChunkKeys.chunkKey(cx, cz);
               if (cache.versionOf(key) != 0) {
                  CachedMesh mesh = this.meshFor(cache, cx, cz, key, lod);
                  mesh.emit(vc, pose, originX, originZ);
               }
            }
         }
      });
   }

   private void evictFarMeshes(ZoneMapPipRenderState state) {
      for (it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<CachedMesh> meshes : this.meshCache) {
         if (meshes.size() <= 1024) {
            continue;
         }

         int pad = 2;
         int minX = state.minChunkX() - pad;
         int maxX = state.maxChunkX() + pad;
         int minZ = state.minChunkZ() - pad;
         int maxZ = state.maxChunkZ() + pad;
         meshes.keySet().removeIf(key -> {
            int cx = (int)(key & 0xFFFFFFFFL);
            int cz = (int)(key >> 32);
            return cx < minX || cx > maxX || cz < minZ || cz > maxZ;
         });
      }
   }

   
   private CachedMesh meshFor(ZonePlannerMapColours cache, int cx, int cz, long key, int lod) {
      it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<CachedMesh> meshes = this.meshCache[lod];
      // Fast path (see lastMeshValidationVersion): no chunk data changed since the last texture render, so a
      // cached mesh is valid as-is — one primitive map get instead of five neighbour-version lookups.
      if (!this.revalidateMeshes) {
         CachedMesh cached = meshes.get(key);
         if (cached != null) {
            return cached;
         }
      }

      int selfVer = cache.versionOf(key);
      int vW = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx - 1, cz));
      int vE = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx + 1, cz));
      int vN = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx, cz - 1));
      int vS = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx, cz + 1));
      CachedMesh mesh = meshes.get(key);
      if (mesh == null || !mesh.matches(selfVer, vW, vE, vN, vS)) {
         mesh = CachedMesh.build(cache, cx, cz, lod, selfVer, vW, vE, vN, vS);
         meshes.put(key, mesh);
      }

      return mesh;
   }


   @Override
   public void close() {
      super.close();

      for (it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<CachedMesh> meshes : this.meshCache) {
         meshes.clear();
      }
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
            vc.addVertex(pose, this.px[i] - originX, this.py[i], this.pz[i] - originZ)
               .setColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, 255)
               .setUv(0.0F, 0.0F)
               .setLight(LightCoordsUtil.FULL_BRIGHT)
               .setNormal(0.0F, 1.0F, 0.0F);
         }
      }

      /**
       * Bakes the chunk at a level of detail: cells of {@code 1 << lod} blocks (the tallest column of a cell wins, so
       * cliffs keep their silhouette) merged row-greedily, a run of cells with the same colour and height along X
       * becomes ONE top quad, and the north/south cliff walls merge over sub-runs of equal neighbour height
       * (west/east walls only exist at run ends). Flat terrain collapses to a handful of quads per chunk.
       */
      static CachedMesh build(ZonePlannerMapColours cache, int cx, int cz, int lod, int selfVer, int vW, int vE, int vN, int vS) {
         FloatList xs = new FloatList();
         FloatList ys = new FloatList();
         FloatList zs = new FloatList();
         IntList cs = new IntList();
         int[][] self = cells(cache, ZonePlannerChunkKeys.chunkKey(cx, cz), lod);
         if (self != null) {
            int n = 16 >> lod;
            int size = 1 << lod;
            int[] cols = self[0];
            int[] hts = self[1];
            int[] htsW = heights(cache, ZonePlannerChunkKeys.chunkKey(cx - 1, cz), lod);
            int[] htsE = heights(cache, ZonePlannerChunkKeys.chunkKey(cx + 1, cz), lod);
            int[] htsN = heights(cache, ZonePlannerChunkKeys.chunkKey(cx, cz - 1), lod);
            int[] htsS = heights(cache, ZonePlannerChunkKeys.chunkKey(cx, cz + 1), lod);
            int baseX = cx << 4;
            int baseZ = cz << 4;

            for (int lz = 0; lz < n; lz++) {
               float z0 = baseZ + lz * size;
               float z1 = z0 + size;
               int row = lz * n;
               int[] nRow = lz > 0 ? hts : htsN;
               int nOff = lz > 0 ? row - n : (n - 1) * n;
               int[] sRow = lz < n - 1 ? hts : htsS;
               int sOff = lz < n - 1 ? row + n : 0;
               int lx = 0;

               while (lx < n) {
                  int colour = cols[row + lx];
                  if (colour == 0) {
                     lx++;
                     continue;
                  }

                  int h = hts[row + lx];
                  int start = lx;

                  do {
                     lx++;
                  } while (lx < n && cols[row + lx] == colour && hts[row + lx] == h);

                  int rgb = colour & 0xFFFFFF;
                  int shaded = darken(rgb);
                  float xA = baseX + start * size;
                  float xB = baseX + lx * size;
                  float yTop = h + 1.0F;
                  emitQuadBaked(xs, ys, zs, cs, xA, yTop, z1, xB, yTop, z1, xB, yTop, z0, xA, yTop, z0, rgb);
                  int nh = start > 0 ? hts[row + start - 1] : (htsW != null ? htsW[row + n - 1] : ZonePlannerMapColours.NO_HEIGHT);
                  if (nh != ZonePlannerMapColours.NO_HEIGHT && nh < h) {
                     float yBot = nh + 1.0F;
                     emitQuadBaked(xs, ys, zs, cs, xA, yBot, z1, xA, yTop, z1, xA, yTop, z0, xA, yBot, z0, shaded);
                  }

                  nh = lx < n ? hts[row + lx] : (htsE != null ? htsE[row] : ZonePlannerMapColours.NO_HEIGHT);
                  if (nh != ZonePlannerMapColours.NO_HEIGHT && nh < h) {
                     float yBot = nh + 1.0F;
                     emitQuadBaked(xs, ys, zs, cs, xB, yBot, z0, xB, yTop, z0, xB, yTop, z1, xB, yBot, z1, shaded);
                  }

                  emitWallRuns(xs, ys, zs, cs, nRow, nOff, start, lx, baseX, size, h, z0, true, shaded);
                  emitWallRuns(xs, ys, zs, cs, sRow, sOff, start, lx, baseX, size, h, z1, false, shaded);
               }
            }
         }

         return new CachedMesh(selfVer, vW, vE, vN, vS, xs.toArray(), ys.toArray(), zs.toArray(), cs.toArray());
      }

      @Nullable
      private static int[] heights(ZonePlannerMapColours cache, long key, int lod) {
         int[][] c = cells(cache, key, lod);
         return c == null ? null : c[1];
      }

      /** Colours and heights of a chunk as {@code (16 >> lod)²} cells; the tallest column of each cell is kept. */
      @Nullable
      private static int[][] cells(ZonePlannerMapColours cache, long key, int lod) {
         int[] cols = cache.coloursOf(key);
         int[] hts = cache.heightsOf(key);
         if (cols == null || hts == null) {
            return null;
         }

         if (lod == 0) {
            return new int[][]{cols, hts};
         }

         int n = 16 >> lod;
         int size = 1 << lod;
         int[] outCols = new int[n * n];
         int[] outHts = new int[n * n];

         for (int cz = 0; cz < n; cz++) {
            for (int cx = 0; cx < n; cx++) {
               int bestH = ZonePlannerMapColours.NO_HEIGHT;
               int bestC = 0;

               for (int dz = 0; dz < size; dz++) {
                  int rowIdx = (cz * size + dz) * 16 + cx * size;

                  for (int dx = 0; dx < size; dx++) {
                     int c = cols[rowIdx + dx];
                     int h = hts[rowIdx + dx];
                     if (c != 0 && h != ZonePlannerMapColours.NO_HEIGHT && h > bestH) {
                        bestH = h;
                        bestC = c;
                     }
                  }
               }

               outCols[cz * n + cx] = bestC;
               outHts[cz * n + cx] = bestH;
            }
         }

         return new int[][]{outCols, outHts};
      }

      /** Emits the north/south cliff wall of a run, merged over sub-runs of equal (lower) neighbour height. */
      private static void emitWallRuns(
         FloatList xs, FloatList ys, FloatList zs, IntList cs, int[] nbrRow, int nbrOff, int from, int to, int baseX, int size,
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

            float xA = baseX + segStart * size;
            float xB = baseX + i * size;
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
}
