package buildcraft.robotics.client.render.pip;

import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;

/** Draws the Zone Planner terrain as a 3D top-down perspective height-field into an offscreen texture. */
public class ZoneMapPipRenderer extends PictureInPictureRenderer<ZoneMapPipRenderState> {
   private ProjectionMatrixBuffer perspBuffer;
   private ProjectionMatrixBuffer orthoRestoreBuffer;
   private final Projection orthoRestore = new Projection();
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
         int radius = state.viewRadius();
         int cx0 = (originX - radius) >> 4;
         int cx1 = (originX + radius) >> 4;
         int cz0 = (originZ - radius) >> 4;
         int cz1 = (originZ + radius) >> 4;

         for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
               long key = chunkKey(cx, cz);
               if (cache.versionOf(key) != 0) {
                  for (int lz = 0; lz < 16; lz++) {
                     for (int lx = 0; lx < 16; lx++) {
                        int wx = (cx << 4) + lx;
                        int wz = (cz << 4) + lz;
                        int colour = cache.colourAt(key, wx, wz);
                        if (colour != 0) {
                           int h = cache.heightAt(key, wx, wz);
                           this.emitColumn(vc, pose, cache, wx, wz, originX, originZ, h, colour);
                        }
                     }
                  }
               }
            }
         }

         this.emitOverlay(state, pose, vc);
      }
   }

   private void emitColumn(
      VertexConsumer vc, Pose pose, ZonePlannerMapColours cache, int wx, int wz, int originX, int originZ, int h, int colour
   ) {
      float x0 = wx - originX;
      float z0 = wz - originZ;
      float x1 = x0 + 1.0F;
      float z1 = z0 + 1.0F;
      int r = colour >> 16 & 0xFF;
      int g = colour >> 8 & 0xFF;
      int b = colour & 0xFF;
      emitTop(vc, pose, x0, z0, x1, z1, h, r, g, b, 255);
      int sr = r * 7 / 10;
      int sg = g * 7 / 10;
      int sb = b * 7 / 10;
      this.emitSide(vc, pose, cache, wx, wz, -1, 0, originX, originZ, h, x0, z0, x1, z1, sr, sg, sb);
      this.emitSide(vc, pose, cache, wx, wz, 1, 0, originX, originZ, h, x0, z0, x1, z1, sr, sg, sb);
      this.emitSide(vc, pose, cache, wx, wz, 0, -1, originX, originZ, h, x0, z0, x1, z1, sr, sg, sb);
      this.emitSide(vc, pose, cache, wx, wz, 0, 1, originX, originZ, h, x0, z0, x1, z1, sr, sg, sb);
   }

   private void emitSide(
      VertexConsumer vc, Pose pose, ZonePlannerMapColours cache, int wx, int wz, int dx, int dz, int originX, int originZ,
      int h, float x0, float z0, float x1, float z1, int r, int g, int b
   ) {
      int nx = wx + dx;
      int nz = wz + dz;
      long nKey = chunkKey(nx >> 4, nz >> 4);
      int nh = cache.heightAt(nKey, nx, nz);
      if (nh != ZonePlannerMapColours.NO_HEIGHT && nh < h) {
         float yTop = h;
         float yBot = nh;
         if (dx < 0) {
            emitQuad(vc, pose, x0, yBot, z1, x0, yTop, z1, x0, yTop, z0, x0, yBot, z0, r, g, b);
         } else if (dx > 0) {
            emitQuad(vc, pose, x1, yBot, z0, x1, yTop, z0, x1, yTop, z1, x1, yBot, z1, r, g, b);
         } else if (dz < 0) {
            emitQuad(vc, pose, x0, yBot, z0, x0, yTop, z0, x1, yTop, z0, x1, yBot, z0, r, g, b);
         } else {
            emitQuad(vc, pose, x1, yBot, z1, x1, yTop, z1, x0, yTop, z1, x0, yBot, z1, r, g, b);
         }
      }
   }

   private void emitOverlay(ZoneMapPipRenderState state, Pose pose, VertexConsumer vc) {
      int originX = state.originX();
      int originZ = state.originZ();
      int[] cells = state.overlayCells();
      int colour = state.overlayColour();
      if (cells != null && (colour >>> 24) != 0) {
         int r = colour >> 16 & 0xFF;
         int g = colour >> 8 & 0xFF;
         int b = colour & 0xFF;

         for (int i = 0; i + 1 < cells.length; i += 2) {
            int wx = cells[i];
            int wz = cells[i + 1];
            int h = state.colours().heightAt(chunkKey(wx >> 4, wz >> 4), wx, wz);
            if (h != ZonePlannerMapColours.NO_HEIGHT) {
               float x0 = wx - originX;
               float z0 = wz - originZ;
               emitTop(vc, pose, x0, z0, x0 + 1.0F, z0 + 1.0F, h + 0.05F, r, g, b, 255);
            }
         }
      }

      if (state.hasSelection()) {
         int sc = state.selColour();
         int r = sc >> 16 & 0xFF;
         int g = sc >> 8 & 0xFF;
         int b = sc & 0xFF;
         int minX = Math.min(state.selX0(), state.selX1());
         int maxX = Math.max(state.selX0(), state.selX1());
         int minZ = Math.min(state.selZ0(), state.selZ1());
         int maxZ = Math.max(state.selZ0(), state.selZ1());

         for (int wx = minX; wx <= maxX; wx++) {
            for (int wz = minZ; wz <= maxZ; wz++) {
               int h = state.colours().heightAt(chunkKey(wx >> 4, wz >> 4), wx, wz);
               if (h != ZonePlannerMapColours.NO_HEIGHT) {
                  float x0 = wx - originX;
                  float z0 = wz - originZ;
                  emitTop(vc, pose, x0, z0, x0 + 1.0F, z0 + 1.0F, h + 0.1F, r, g, b, 255);
               }
            }
         }
      }
   }

   private static void emitTop(VertexConsumer vc, Pose pose, float x0, float z0, float x1, float z1, float y, int r, int g, int b, int a) {
      emitQuad(vc, pose, x0, y, z1, x1, y, z1, x1, y, z0, x0, y, z0, r, g, b, a);
   }

   private static void emitQuad(
      VertexConsumer vc, Pose pose, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz,
      float dx, float dy, float dz, int r, int g, int b
   ) {
      emitQuad(vc, pose, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, r, g, b, 255);
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

   private static long chunkKey(int chunkX, int chunkZ) {
      return (chunkX & 0xFFFFFFFFL) | (long)chunkZ << 32;
   }

   @Override
   public void close() {
      super.close();
      if (this.perspBuffer != null) {
         this.perspBuffer.close();
         this.perspBuffer = null;
      }

      if (this.orthoRestoreBuffer != null) {
         this.orthoRestoreBuffer.close();
         this.orthoRestoreBuffer = null;
      }
   }
}
