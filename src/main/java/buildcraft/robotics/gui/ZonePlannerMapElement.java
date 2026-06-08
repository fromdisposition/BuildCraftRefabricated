package buildcraft.robotics.gui;

import buildcraft.core.BCCore;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.fabric.client.GuiGraphicsCompat;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.robotics.client.render.pip.ZoneMapPipRenderState;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/** Interactive 3D top-down perspective terrain map (BC 8.0 parity), rendered via the GUI Picture-in-Picture pipeline. */
public class ZonePlannerMapElement implements IInteractionElement {
   private static final float PITCH_DEG = 90.0F;
   private static final int PAN_STEP = 4;
   private static final double MIN_DIST = 16.0;
   private static final double MAX_DIST = 384.0;
   private static final double ZOOM_SPEED = 6.0;
   private static final int RETRY_INTERVAL = 60;
   private static final int DEFAULT_HEIGHT = 64;
   private final GuiZonePlanner gui;
   private final TileZonePlanner tile;
   private final int mapOffsetX;
   private final int mapOffsetY;
   private final int mapW;
   private final int mapH;
   private double camX;
   private double camZ;
   private double camDist = 80.0;
   private double zoomSpeed;
   private int retryCounter;
   private boolean panning;
   private double panStartMouseX;
   private double panStartMouseY;
   private double panStartCamX;
   private double panStartCamZ;
   private boolean selecting;
   private int selStartBX;
   private int selStartBZ;
   private int selEndBX;
   private int selEndBZ;
   private boolean hasHover;
   private int hoverBlockX;
   private int hoverBlockZ;
   private int hoverBlockY;

   public ZonePlannerMapElement(GuiZonePlanner gui, TileZonePlanner tile, int mapOffsetX, int mapOffsetY, int mapW, int mapH) {
      this.gui = gui;
      this.tile = tile;
      this.mapOffsetX = mapOffsetX;
      this.mapOffsetY = mapOffsetY;
      this.mapW = mapW;
      this.mapH = mapH;
      if (tile != null) {
         BlockPos pos = tile.getBlockPos();
         this.camX = pos.getX() + 0.5;
         this.camZ = pos.getZ() + 0.5;
      }
   }

   private ContainerZonePlanner container() {
      return this.gui.getMenu() instanceof ContainerZonePlanner c ? c : null;
   }

   private int activeLayer() {
      ItemStack carried = this.gui.getMenu().getCarried();
      if (!carried.isEmpty() && carried.getItem() instanceof ItemPaintbrush_BC8) {
         DyeColor colour = (DyeColor)carried.get(BCCore.BRUSH_COLOR);
         if (colour != null) {
            return colour.getId();
         }
      }

      return -1;
   }

   private int mapX() {
      return this.gui.getGuiLeftPos() + this.mapOffsetX;
   }

   private int mapY() {
      return this.gui.getGuiTopPos() + this.mapOffsetY;
   }

   @Override
   public double getX() {
      return this.mapX();
   }

   @Override
   public double getY() {
      return this.mapY();
   }

   @Override
   public double getWidth() {
      return this.mapW;
   }

   @Override
   public double getHeight() {
      return this.mapH;
   }

   private static long chunkKey(int chunkX, int chunkZ) {
      return (chunkX & 0xFFFFFFFFL) | (long)chunkZ << 32;
   }

   private int viewRadius() {
      return Mth.clamp((int)(this.camDist * 0.8) + 8, 24, 96);
   }

   private int focusHeight(ZonePlannerMapColours cache) {
      if (cache != null) {
         int wx = Mth.floor(this.camX);
         int wz = Mth.floor(this.camZ);
         int h = cache.heightAt(chunkKey(wx >> 4, wz >> 4), wx, wz);
         if (h != ZonePlannerMapColours.NO_HEIGHT) {
            return h;
         }
      }

      return DEFAULT_HEIGHT;
   }

   private double camY(ZonePlannerMapColours cache) {
      return this.focusHeight(cache) + this.camDist;
   }

   @Override
   public void drawBackground(float partialTicks) {
      BCGraphics g = GuiIcon.getGuiGraphics();
      if (g != null) {
         ContainerZonePlanner menu = this.container();
         ZonePlannerMapColours cache = menu != null ? menu.mapColours : null;
         this.applyZoomDamping();
         this.ensureVisibleChunks(menu, cache);
         if (cache != null) {
            this.updateHover(cache);
            ZoneMapPipRenderState state = this.buildState(g, cache, true);
            GuiGraphicsCompat.submitPictureInPictureRenderState(g.raw, state);
         }
      }
   }

   @Override
   public void drawForeground(float partialTicks) {
      BCGraphics g = GuiIcon.getGuiGraphics();
      if (g != null && this.hasHover) {
         String text = "X:" + this.hoverBlockX + " Z:" + this.hoverBlockZ + (this.hoverBlockY != ZonePlannerMapColours.NO_HEIGHT ? " Y:" + this.hoverBlockY : "");
         Minecraft mc = Minecraft.getInstance();
         int tw = mc.font.width(text);
         int tx = this.mapX() + 2;
         int ty = this.mapY() + this.mapH - mc.font.lineHeight - 2;
         g.fill(tx - 1, ty - 1, tx + tw + 1, ty + mc.font.lineHeight, -1610612736);
         g.text(mc.font, text, tx, ty, -1);
      }
   }

   private void applyZoomDamping() {
      if (this.zoomSpeed != 0.0) {
         this.camDist = Mth.clamp(this.camDist + this.zoomSpeed, MIN_DIST, MAX_DIST);
         this.zoomSpeed *= 0.7;
         if (Math.abs(this.zoomSpeed) < 0.05) {
            this.zoomSpeed = 0.0;
         }
      }
   }

   private ZoneMapPipRenderState buildState(BCGraphics g, ZonePlannerMapColours cache, boolean withContent) {
      int originX = Mth.floor(this.camX);
      int originZ = Mth.floor(this.camZ);
      int radius = this.viewRadius();
      int layer = this.activeLayer();
      int overlayColour = 0;
      int[] overlayCells = new int[0];
      if (withContent && this.tile != null && layer >= 0 && layer < this.tile.layers.length) {
         overlayColour = 0xFF000000 | DyeColor.byId(layer).getTextureDiffuseColor() & 0xFFFFFF;
         overlayCells = this.collectLayerCells(layer, originX, originZ, radius);
      }

      boolean hasSel = withContent && this.selecting;
      return new ZoneMapPipRenderState(
         cache,
         originX,
         originZ,
         this.camX,
         this.camZ,
         this.camY(cache),
         PITCH_DEG,
         0.0F,
         radius,
         overlayColour,
         overlayCells,
         hasSel,
         this.selStartBX,
         this.selStartBZ,
         this.selEndBX,
         this.selEndBZ,
         0xFFFFFFFF,
         cache.globalVersion(),
         this.mapX(),
         this.mapY(),
         this.mapX() + this.mapW,
         this.mapY() + this.mapH,
         1.0F,
         g != null ? GuiGraphicsCompat.peekScissorStack(g.raw) : null
      );
   }

   private int[] collectLayerCells(int layer, int originX, int originZ, int radius) {
      ZonePlan plan = this.tile.layers[layer];
      if (plan == null) {
         return new int[0];
      }

      BlockPos tilePos = this.tile.getBlockPos();
      List<int[]> cells = plan.getAll();
      List<Integer> out = new ArrayList<>();

      for (int[] cell : cells) {
         int wx = cell[0] + tilePos.getX();
         int wz = cell[1] + tilePos.getZ();
         if (Math.abs(wx - originX) <= radius && Math.abs(wz - originZ) <= radius) {
            out.add(wx);
            out.add(wz);
         }
      }

      int[] arr = new int[out.size()];

      for (int i = 0; i < arr.length; i++) {
         arr[i] = out.get(i);
      }

      return arr;
   }

   private void ensureVisibleChunks(ContainerZonePlanner menu, ZonePlannerMapColours cache) {
      if (menu != null && cache != null) {
         if (++this.retryCounter >= RETRY_INTERVAL) {
            this.retryCounter = 0;
            cache.retryMissing();
         }

         int originX = Mth.floor(this.camX);
         int originZ = Mth.floor(this.camZ);
         int radius = this.viewRadius();
         int cx0 = originX - radius >> 4;
         int cx1 = originX + radius >> 4;
         int cz0 = originZ - radius >> 4;
         int cz1 = originZ + radius >> 4;
         List<Long> missing = new ArrayList<>();

         for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
               long key = chunkKey(cx, cz);
               if (!cache.hasData(key) && !cache.isRequested(key)) {
                  cache.markRequested(key);
                  missing.add(key);
               }
            }
         }

         menu.requestChunks(missing);
      }
   }

   /** Ray-pick the terrain under a GUI cursor. Returns {worldX, worldZ, worldY} or null. */
   private int[] pick(ZonePlannerMapColours cache, double mouseX, double mouseY) {
      ZoneMapPipRenderState state = this.buildState(null, cache, false);
      double[] ray = state.unprojectRay(mouseX, mouseY);
      double nx = ray[0] + state.originX();
      double ny = ray[1];
      double nz = ray[2] + state.originZ();
      double fx = ray[3] + state.originX();
      double fy = ray[4];
      double fz = ray[5] + state.originZ();
      double dx = fx - nx;
      double dy = fy - ny;
      double dz = fz - nz;
      double horiz = Math.sqrt(dx * dx + dz * dz);
      int steps = Mth.clamp((int)(horiz * 4.0), 16, 4000);

      for (int i = 0; i <= steps; i++) {
         double t = (double)i / steps;
         double wx = nx + dx * t;
         double wy = ny + dy * t;
         double wz = nz + dz * t;
         int bx = Mth.floor(wx);
         int bz = Mth.floor(wz);
         int h = cache.heightAt(chunkKey(bx >> 4, bz >> 4), bx, bz);
         if (h != ZonePlannerMapColours.NO_HEIGHT && wy <= h + 1) {
            return new int[]{bx, bz, h};
         }
      }

      return null;
   }

   @Override
   public void onMouseClicked(int button) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      if (this.inBounds(mx, my)) {
         ContainerZonePlanner menu = this.container();
         ZonePlannerMapColours cache = menu != null ? menu.mapColours : null;
         int layer = this.activeLayer();
         if (this.tile != null && cache != null && layer >= 0 && (button == 0 || button == 1)) {
            int[] hit = this.pick(cache, mx, my);
            if (hit != null) {
               this.selecting = true;
               this.selStartBX = hit[0];
               this.selStartBZ = hit[1];
               this.selEndBX = hit[0];
               this.selEndBZ = hit[1];
            }
         } else {
            this.panning = true;
            this.panStartMouseX = mx;
            this.panStartMouseY = my;
            this.panStartCamX = this.camX;
            this.panStartCamZ = this.camZ;
         }
      }
   }

   @Override
   public void onMouseDragged(int button, long ticksSinceClick) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      if (this.selecting) {
         ContainerZonePlanner menu = this.container();
         ZonePlannerMapColours cache = menu != null ? menu.mapColours : null;
         if (cache != null) {
            int[] hit = this.pick(cache, mx, my);
            if (hit != null) {
               this.selEndBX = hit[0];
               this.selEndBZ = hit[1];
            }
         }
      } else if (this.panning) {
         double wpp = this.worldPerPixel();
         this.camX = this.panStartCamX - (mx - this.panStartMouseX) * wpp;
         this.camZ = this.panStartCamZ - (my - this.panStartMouseY) * wpp;
      }
   }

   private double worldPerPixel() {
      return 2.0 * this.camDist * Math.tan(Math.toRadians(ZoneMapPipRenderState.FOV / 2.0)) / this.mapH;
   }

   @Override
   public void onMouseReleased(int button) {
      if (this.selecting && this.tile != null) {
         int layer = this.activeLayer();
         if (layer >= 0) {
            boolean set = button == 0;
            BlockPos tilePos = this.tile.getBlockPos();
            int rx0 = this.selStartBX - tilePos.getX();
            int rz0 = this.selStartBZ - tilePos.getZ();
            int rx1 = this.selEndBX - tilePos.getX();
            int rz1 = this.selEndBZ - tilePos.getZ();
            if (this.tile.layers[layer] == null) {
               this.tile.layers[layer] = new ZonePlan();
            }

            int minX = Math.min(rx0, rx1);
            int maxX = Math.max(rx0, rx1);
            int minZ = Math.min(rz0, rz1);
            int maxZ = Math.max(rz0, rz1);

            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  this.tile.layers[layer].set(x, z, set);
               }
            }

            ContainerZonePlanner menu = this.container();
            if (menu != null) {
               menu.sendPaintRect(layer, rx0, rz0, rx1, rz1, set);
            }
         }
      }

      this.selecting = false;
      this.panning = false;
   }

   @Override
   public boolean onMouseScroll(double amount) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      if (!this.inBounds(mx, my)) {
         return false;
      }

      this.zoomSpeed -= amount * ZOOM_SPEED;
      return true;
   }

   @Override
   public boolean onKeyPress(char typedChar, int keyCode) {
      switch (keyCode) {
         case 262:
            this.camX += PAN_STEP;
            return true;
         case 263:
            this.camX -= PAN_STEP;
            return true;
         case 264:
            this.camZ += PAN_STEP;
            return true;
         case 265:
            this.camZ -= PAN_STEP;
            return true;
         default:
            return false;
      }
   }

   private boolean inBounds(double mx, double my) {
      int ox = this.mapX();
      int oy = this.mapY();
      return mx >= ox && my >= oy && mx < ox + this.mapW && my < oy + this.mapH;
   }

   private void updateHover(ZonePlannerMapColours cache) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      if (this.inBounds(mx, my)) {
         int[] hit = this.pick(cache, mx, my);
         if (hit != null) {
            this.hasHover = true;
            this.hoverBlockX = hit[0];
            this.hoverBlockZ = hit[1];
            this.hoverBlockY = hit[2];
            return;
         }
      }

      this.hasHover = false;
   }
}
