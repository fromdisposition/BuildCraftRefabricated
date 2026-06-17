/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.gui;

import buildcraft.core.BCCore;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.fabric.client.GuiGraphicsCompat;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.robotics.client.render.pip.ZoneMapPipRenderState;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.zone.ZonePlannerChunkKeys;
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

public class ZonePlannerMapElement implements IInteractionElement {
   private static final float PITCH_DEG = 90.0F;
   private static final int PAN_STEP = 4;
   private static final double MIN_DIST = 16.0;
   private static final double MAX_DIST = 180.0;
   private static final double ZOOM_SPEED = 6.0;
   private static final int RETRY_INTERVAL = 60;
   private static final int DEFAULT_HEIGHT = 64;
   private static final int MARGIN_CHUNKS = 1;
   private static final int MAX_CHUNK_SPAN = 48;
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
   private int selColourValue = 0xFFFFFFFF;
   private double smoothCamY = Double.NaN;
   private int cachedOverlayVersion = Integer.MIN_VALUE;
   private int cachedOverlayLayer = Integer.MIN_VALUE;
   private int[] cachedOverlayCells = new int[0];
   private int[] cachedOverlayColours;
   private int cachedOverlayColour;

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

   
   private int[] footprintChunkBounds(ZonePlannerMapColours cache) {
      double groundY = this.focusHeight(cache);
      double aboveGround = Math.max(8.0, this.camY(cache) - groundY);
      double halfZ = aboveGround * Math.tan(Math.toRadians(ZoneMapPipRenderState.FOV / 2.0));
      double halfX = halfZ * ((double)this.mapW / this.mapH);
      int minCX = Mth.floor((this.camX - halfX) / 16.0) - MARGIN_CHUNKS;
      int maxCX = Mth.floor((this.camX + halfX) / 16.0) + MARGIN_CHUNKS;
      int minCZ = Mth.floor((this.camZ - halfZ) / 16.0) - MARGIN_CHUNKS;
      int maxCZ = Mth.floor((this.camZ + halfZ) / 16.0) + MARGIN_CHUNKS;
      int cx = Mth.floor(this.camX / 16.0);
      int cz = Mth.floor(this.camZ / 16.0);
      if (maxCX - minCX > MAX_CHUNK_SPAN) {
         minCX = cx - MAX_CHUNK_SPAN / 2;
         maxCX = cx + MAX_CHUNK_SPAN / 2;
      }

      if (maxCZ - minCZ > MAX_CHUNK_SPAN) {
         minCZ = cz - MAX_CHUNK_SPAN / 2;
         maxCZ = cz + MAX_CHUNK_SPAN / 2;
      }

      return new int[]{minCX, minCZ, maxCX, maxCZ};
   }

   private int focusHeight(ZonePlannerMapColours cache) {
      if (cache != null) {
         int wx = Mth.floor(this.camX);
         int wz = Mth.floor(this.camZ);
         int h = cache.heightAt(ZonePlannerChunkKeys.chunkKey(wx >> 4, wz >> 4), wx, wz);
         if (h != ZonePlannerMapColours.NO_HEIGHT) {
            return h;
         }
      }

      return DEFAULT_HEIGHT;
   }

   private double camY(ZonePlannerMapColours cache) {
      double target = this.focusHeight(cache) + this.camDist;
      if (Double.isNaN(this.smoothCamY)) {
         this.smoothCamY = target;
      } else {
         this.smoothCamY += (target - this.smoothCamY) * 0.08;
      }
      return this.smoothCamY;
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
            this.ensureOverlay(menu);
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
      int[] bounds = this.footprintChunkBounds(cache);
      int[] overlayCells = withContent ? this.cachedOverlayCells : new int[0];
      int[] overlayColours = withContent ? this.cachedOverlayColours : null;
      int overlayColour = withContent ? this.cachedOverlayColour : 0;
      boolean hasSel = withContent && this.selecting;
      boolean hasHover = withContent && this.hasHover;
      return new ZoneMapPipRenderState(
         cache,
         originX,
         originZ,
         this.camX,
         this.camZ,
         this.camY(cache),
         PITCH_DEG,
         0.0F,
         bounds[0],
         bounds[1],
         bounds[2],
         bounds[3],
         overlayColour,
         overlayCells,
         overlayColours,
         hasSel,
         this.selStartBX,
         this.selStartBZ,
         this.selEndBX,
         this.selEndBZ,
         this.selColourValue,
         hasHover,
         this.hoverBlockX,
         this.hoverBlockZ,
         cache.globalVersion(),
         this.mapX(),
         this.mapY(),
         this.mapX() + this.mapW,
         this.mapY() + this.mapH,
         1.0F,
         g != null ? GuiGraphicsCompat.peekScissorStack(g.raw) : null
      );
   }

   
   private void ensureOverlay(ContainerZonePlanner menu) {
      int layer = this.activeLayer();
      int version = menu != null ? menu.clientLayerVersion : 0;
      if (version == this.cachedOverlayVersion && layer == this.cachedOverlayLayer) {
         return;
      }

      this.cachedOverlayVersion = version;
      this.cachedOverlayLayer = layer;
      if (this.tile == null) {
         this.cachedOverlayCells = new int[0];
         this.cachedOverlayColours = null;
         this.cachedOverlayColour = 0;
      } else if (layer >= 0 && layer < this.tile.layers.length) {
         this.cachedOverlayColour = 0xFF000000 | DyeColor.byId(layer).getTextureDiffuseColor() & 0xFFFFFF;
         this.cachedOverlayCells = this.collectLayerCells(layer);
         this.cachedOverlayColours = null;
      } else {
         this.cachedOverlayColour = 0;
         this.collectAllLayers();
      }
   }

   private int[] collectLayerCells(int layer) {
      ZonePlan plan = this.tile.layers[layer];
      if (plan == null) {
         return new int[0];
      }

      BlockPos tilePos = this.tile.getBlockPos();
      List<int[]> cells = plan.getAll();
      int[] arr = new int[cells.size() * 2];
      int n = 0;

      for (int[] cell : cells) {
         arr[n++] = cell[0] + tilePos.getX();
         arr[n++] = cell[1] + tilePos.getZ();
      }

      return arr;
   }

   private void collectAllLayers() {
      BlockPos tilePos = this.tile.getBlockPos();
      List<Integer> cellsOut = new ArrayList<>();
      List<Integer> coloursOut = new ArrayList<>();

      for (int layer = 0; layer < this.tile.layers.length; layer++) {
         ZonePlan plan = this.tile.layers[layer];
         if (plan != null) {
            int colour = 0xFF000000 | DyeColor.byId(layer).getTextureDiffuseColor() & 0xFFFFFF;

            for (int[] cell : plan.getAll()) {
               cellsOut.add(cell[0] + tilePos.getX());
               cellsOut.add(cell[1] + tilePos.getZ());
               coloursOut.add(colour);
            }
         }
      }

      int[] cells = new int[cellsOut.size()];

      for (int i = 0; i < cells.length; i++) {
         cells[i] = cellsOut.get(i);
      }

      int[] colours = new int[coloursOut.size()];

      for (int i = 0; i < colours.length; i++) {
         colours[i] = coloursOut.get(i);
      }

      this.cachedOverlayCells = cells;
      this.cachedOverlayColours = colours;
   }

   private void ensureVisibleChunks(ContainerZonePlanner menu, ZonePlannerMapColours cache) {
      if (menu != null && cache != null) {
         if (++this.retryCounter >= RETRY_INTERVAL) {
            this.retryCounter = 0;
            cache.retryMissing();
         }

         int[] bounds = this.footprintChunkBounds(cache);
         int cx0 = bounds[0];
         int cz0 = bounds[1];
         int cx1 = bounds[2];
         int cz1 = bounds[3];
         List<Long> missing = new ArrayList<>();

         for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
               long key = ZonePlannerChunkKeys.chunkKey(cx, cz);
               if (!cache.hasData(key) && !cache.isRequested(key)) {
                  cache.markRequested(key);
                  missing.add(key);
               }
            }
         }

         menu.requestChunks(missing);
      }
   }

   
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
         int h = cache.heightAt(ZonePlannerChunkKeys.chunkKey(bx >> 4, bz >> 4), bx, bz);
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
         if (layer >= 0) {
            if (this.tile != null && cache != null && (button == 0 || button == 1)) {
               int[] hit = this.pick(cache, mx, my);
               if (hit != null) {
                  this.selecting = true;
                  this.selStartBX = hit[0];
                  this.selStartBZ = hit[1];
                  this.selEndBX = hit[0];
                  this.selEndBZ = hit[1];
                  this.selColourValue = button == 0 ? 0xFF000000 | DyeColor.byId(layer).getTextureDiffuseColor() & 0xFFFFFF : 0xFFFF5555;
               }
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
