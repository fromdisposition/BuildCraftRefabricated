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
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;

public class ZoneMapPipRenderer extends PictureInPictureRenderer<ZoneMapPipRenderState> {
   private static final int OVERLAY_ALPHA = 0x55;
   private static final int SELECTION_ALPHA = 0x99;
   private static final int HOVER_ALPHA = 0x99;
   private ProjectionMatrixBuffer perspBuffer;
   private ProjectionMatrixBuffer orthoRestoreBuffer;
   private final Projection orthoRestore = new Projection();
   private final Map<Long, CachedMesh> meshCache = new HashMap<>();
   private long lastStamp = Long.MIN_VALUE;

   public ZoneMapPipRenderer(BufferSource bufferSource) {
      super(bufferSource);
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
      return state.renderStamp() == this.lastStamp;
   }

   @Override
   protected void renderToTexture(ZoneMapPipRenderState state, PoseStack poseStack) {
      this.lastStamp = state.renderStamp();
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

   private void emitTerrain(ZoneMapPipRenderState state, Pose pose) {
      ZonePlannerMapColours cache = state.colours();
      if (cache != null) {
         VertexConsumer vc = this.bufferSource.getBuffer(BCLibRenderTypes.debugSolid());
         int originX = state.originX();
         int originZ = state.originZ();
         int cx0 = state.minChunkX();
         int cx1 = state.maxChunkX();
         int cz0 = state.minChunkZ();
         int cz1 = state.maxChunkZ();

         for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
               long key = ZonePlannerChunkKeys.chunkKey(cx, cz);
               if (cache.versionOf(key) != 0) {
                  CachedMesh mesh = this.meshFor(cache, cx, cz, key);
                  mesh.emit(vc, pose, originX, originZ);
               }
            }
         }

         this.emitOverlay(state, pose);
      }
   }

   
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

   
   private void emitOverlay(ZoneMapPipRenderState state, Pose pose) {
      ZonePlannerMapColours cache = state.colours();
      int originX = state.originX();
      int originZ = state.originZ();
      VertexConsumer vc = this.bufferSource.getBuffer(BCLibRenderTypes.debugFilled());
      int[] cells = state.overlayCells();
      int[] cellColours = state.overlayColours();
      int single = state.overlayColour();
      if (cells != null) {
         for (int i = 0; i + 1 < cells.length; i += 2) {
            int colour = cellColours != null && i / 2 < cellColours.length ? cellColours[i / 2] : single;
            if ((colour >>> 24) != 0) {
               this.emitOverlayCell(vc, pose, cache, cells[i], cells[i + 1], originX, originZ, colour, OVERLAY_ALPHA);
            }
         }
      }

      if (state.hasSelection()) {
         int sc = state.selColour();
         int minX = Math.min(state.selX0(), state.selX1());
         int maxX = Math.max(state.selX0(), state.selX1());
         int minZ = Math.min(state.selZ0(), state.selZ1());
         int maxZ = Math.max(state.selZ0(), state.selZ1());

         for (int wx = minX; wx <= maxX; wx++) {
            for (int wz = minZ; wz <= maxZ; wz++) {
               this.emitOverlayCell(vc, pose, cache, wx, wz, originX, originZ, sc, SELECTION_ALPHA);
            }
         }
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

   private void emitOverlayCell(VertexConsumer vc, Pose pose, ZonePlannerMapColours cache, int wx, int wz, int originX, int originZ, int colour, int alpha) {
      int h = cache.heightAt(ZonePlannerChunkKeys.chunkKey(wx >> 4, wz >> 4), wx, wz);
      if (h != ZonePlannerMapColours.NO_HEIGHT) {
         float x0 = wx - originX;
         float z0 = wz - originZ;
         emitFilledCuboid(vc, pose, x0, z0, x0 + 1.0F, z0 + 1.0F, h + 1.02F, h + 2.0F, colour >> 16 & 0xFF, colour >> 8 & 0xFF, colour & 0xFF, alpha);
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
      if (this.perspBuffer != null) {
         this.perspBuffer.close();
         this.perspBuffer = null;
      }

      if (this.orthoRestoreBuffer != null) {
         this.orthoRestoreBuffer.close();
         this.orthoRestoreBuffer = null;
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
            vc.addVertex(pose, this.px[i] - originX, this.py[i], this.pz[i] - originZ).setColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, 255);
         }
      }

      static CachedMesh build(ZonePlannerMapColours cache, int cx, int cz, int selfVer, int vW, int vE, int vN, int vS) {
         FloatList xs = new FloatList();
         FloatList ys = new FloatList();
         FloatList zs = new FloatList();
         IntList cs = new IntList();

         for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
               int wx = (cx << 4) + lx;
               int wz = (cz << 4) + lz;
               int colour = cache.colourAt(ZonePlannerChunkKeys.chunkKey(cx, cz), wx, wz);
               if (colour != 0) {
                  int h = cache.heightAt(ZonePlannerChunkKeys.chunkKey(cx, cz), wx, wz);
                  int rgb = colour & 0xFFFFFF;
                  int shaded = darken(rgb);
                  float x0 = wx;
                  float z0 = wz;
                  float yTop = h + 1.0F;
                  emitTopBaked(xs, ys, zs, cs, x0, z0, x0 + 1.0F, z0 + 1.0F, yTop, rgb);
                  emitSideBaked(cache, xs, ys, zs, cs, wx, wz, -1, 0, h, x0, z0, x0 + 1.0F, z0 + 1.0F, shaded);
                  emitSideBaked(cache, xs, ys, zs, cs, wx, wz, 1, 0, h, x0, z0, x0 + 1.0F, z0 + 1.0F, shaded);
                  emitSideBaked(cache, xs, ys, zs, cs, wx, wz, 0, -1, h, x0, z0, x0 + 1.0F, z0 + 1.0F, shaded);
                  emitSideBaked(cache, xs, ys, zs, cs, wx, wz, 0, 1, h, x0, z0, x0 + 1.0F, z0 + 1.0F, shaded);
               }
            }
         }

         return new CachedMesh(selfVer, vW, vE, vN, vS, xs.toArray(), ys.toArray(), zs.toArray(), cs.toArray());
      }

      private static int darken(int rgb) {
         int r = (rgb >> 16 & 0xFF) * 7 / 10;
         int g = (rgb >> 8 & 0xFF) * 7 / 10;
         int b = (rgb & 0xFF) * 7 / 10;
         return r << 16 | g << 8 | b;
      }

      private static void emitSideBaked(
         ZonePlannerMapColours cache, FloatList xs, FloatList ys, FloatList zs, IntList cs, int wx, int wz, int dx, int dz,
         int h, float x0, float z0, float x1, float z1, int rgb
      ) {
         int nx = wx + dx;
         int nz = wz + dz;
         int nh = cache.heightAt(ZonePlannerChunkKeys.chunkKey(nx >> 4, nz >> 4), nx, nz);
         if (nh != ZonePlannerMapColours.NO_HEIGHT && nh < h) {
            float yTop = h + 1.0F;
            float yBot = nh + 1.0F;
            if (dx < 0) {
               emitQuadBaked(xs, ys, zs, cs, x0, yBot, z1, x0, yTop, z1, x0, yTop, z0, x0, yBot, z0, rgb);
            } else if (dx > 0) {
               emitQuadBaked(xs, ys, zs, cs, x1, yBot, z0, x1, yTop, z0, x1, yTop, z1, x1, yBot, z1, rgb);
            } else if (dz < 0) {
               emitQuadBaked(xs, ys, zs, cs, x0, yBot, z0, x0, yTop, z0, x1, yTop, z0, x1, yBot, z0, rgb);
            } else {
               emitQuadBaked(xs, ys, zs, cs, x1, yBot, z1, x1, yTop, z1, x0, yTop, z1, x0, yBot, z1, rgb);
            }
         }
      }

      private static void emitTopBaked(
         FloatList xs, FloatList ys, FloatList zs, IntList cs, float x0, float z0, float x1, float z1, float y, int rgb
      ) {
         emitQuadBaked(xs, ys, zs, cs, x0, y, z1, x1, y, z1, x1, y, z0, x0, y, z0, rgb);
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
