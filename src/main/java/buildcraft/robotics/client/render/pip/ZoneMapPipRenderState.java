package buildcraft.robotics.client.render.pip;

import buildcraft.robotics.zone.ZonePlannerMapColours;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Render-state for the 3D Zone Planner terrain map. Submitted from {@code ZonePlannerMapElement} into the GUI
 * Picture-in-Picture pipeline; drawn by {@link ZoneMapPipRenderer}. All world XZ coordinates are kept relative to
 * {@code originX/originZ} (floor of the camera centre) for float precision; Y stays absolute.
 */
public record ZoneMapPipRenderState(
   ZonePlannerMapColours colours,
   int originX,
   int originZ,
   double camX,
   double camZ,
   double camY,
   float pitchDeg,
   float yawDeg,
   int viewRadius,
   int overlayColour,
   int[] overlayCells,
   @Nullable int[] overlayColours,
   boolean hasSelection,
   int selX0,
   int selZ0,
   int selX1,
   int selZ1,
   int selColour,
   boolean hasHover,
   int hoverX,
   int hoverZ,
   int terrainVersion,
   int x0,
   int y0,
   int x1,
   int y1,
   float scale,
   @Nullable ScreenRectangle scissorArea,
   @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
   public static final float FOV = 70.0F;
   public static final float NEAR = 1.0F;
   public static final float FAR = 10000.0F;

   public ZoneMapPipRenderState(
      ZonePlannerMapColours colours,
      int originX,
      int originZ,
      double camX,
      double camZ,
      double camY,
      float pitchDeg,
      float yawDeg,
      int viewRadius,
      int overlayColour,
      int[] overlayCells,
      @Nullable int[] overlayColours,
      boolean hasSelection,
      int selX0,
      int selZ0,
      int selX1,
      int selZ1,
      int selColour,
      boolean hasHover,
      int hoverX,
      int hoverZ,
      int terrainVersion,
      int x0,
      int y0,
      int x1,
      int y1,
      float scale,
      @Nullable ScreenRectangle scissorArea
   ) {
      this(
         colours, originX, originZ, camX, camZ, camY, pitchDeg, yawDeg, viewRadius, overlayColour, overlayCells,
         overlayColours, hasSelection, selX0, selZ0, selX1, selZ1, selColour, hasHover, hoverX, hoverZ, terrainVersion,
         x0, y0, x1, y1, scale, scissorArea,
         PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea)
      );
   }

   /** Camera/view matrix in the origin-relative frame (vertices are emitted relative to originX/originZ). */
   public Matrix4f viewMatrix() {
      Matrix4f m = new Matrix4f();
      m.rotateX((float)Math.toRadians(this.pitchDeg));
      if (this.yawDeg != 0.0F) {
         m.rotateY((float)Math.toRadians(this.yawDeg));
      }

      m.translate((float)(this.originX - this.camX), (float)(-this.camY), (float)(this.originZ - this.camZ));
      return m;
   }

   public Matrix4f projMatrix() {
      float aspect = (float)(this.x1 - this.x0) / (float)(this.y1 - this.y0);
      boolean zeroToOne = RenderSystem.getDevice().isZZeroToOne();
      return new Matrix4f().setPerspective((float)Math.toRadians((double)FOV), aspect, NEAR, FAR, zeroToOne);
   }

   /**
    * Unprojects a GUI-space cursor into a world-space ray (origin-relative). Returns {@code {nearX,nearY,nearZ, farX,farY,farZ}}.
    */
   public double[] unprojectRay(double mouseX, double mouseY) {
      double ndcX = 2.0 * (mouseX - this.x0) / (this.x1 - this.x0) - 1.0;
      double ndcY = 1.0 - 2.0 * (mouseY - this.y0) / (this.y1 - this.y0);
      boolean zeroToOne = RenderSystem.getDevice().isZZeroToOne();
      float nearZ = zeroToOne ? 0.0F : -1.0F;
      Matrix4f inv = new Matrix4f(this.projMatrix()).mul(this.viewMatrix()).invert();
      Vector4f near = inv.transform(new Vector4f((float)ndcX, (float)ndcY, nearZ, 1.0F));
      Vector4f far = inv.transform(new Vector4f((float)ndcX, (float)ndcY, 1.0F, 1.0F));
      near.div(near.w());
      far.div(far.w());
      return new double[]{near.x(), near.y(), near.z(), far.x(), far.y(), far.z()};
   }

   /**
    * Change-stamp; an identical stamp across frames lets the renderer reuse the offscreen texture. It folds the full
    * painted-overlay content (not just its length) so that any paint/erase/import is reflected immediately instead of
    * waiting for a pan or zoom.
    */
   public long renderStamp() {
      long h = 1125899906842597L;
      h = 31L * h + Double.doubleToLongBits(this.camX);
      h = 31L * h + Double.doubleToLongBits(this.camZ);
      h = 31L * h + Double.doubleToLongBits(this.camY);
      h = 31L * h + Float.floatToIntBits(this.pitchDeg);
      h = 31L * h + Float.floatToIntBits(this.yawDeg);
      h = 31L * h + this.terrainVersion;
      h = 31L * h + this.overlayColour;
      if (this.overlayCells != null) {
         for (int c : this.overlayCells) {
            h = 31L * h + c;
         }
      }

      if (this.overlayColours != null) {
         for (int c : this.overlayColours) {
            h = 31L * h + c;
         }
      }

      h = 31L * h + (this.hasSelection ? 1 : 0);
      h = 31L * h + this.selX0;
      h = 31L * h + this.selZ0;
      h = 31L * h + this.selX1;
      h = 31L * h + this.selZ1;
      h = 31L * h + this.selColour;
      h = 31L * h + (this.hasHover ? 1 : 0);
      h = 31L * h + this.hoverX;
      h = 31L * h + this.hoverZ;
      return h;
   }
}
