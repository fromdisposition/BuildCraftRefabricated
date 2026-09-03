/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.gui;

import buildcraft.core.BCCore;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import buildcraft.robotics.zone.ZonePlannerChunkKeys;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/** Top-down map of the zone planner: region textures panned and zoomed as flat quads, zones and selection as fills. */
public class ZonePlannerMapElement implements IInteractionElement {
   private static final int PAN_STEP = 4;
   private static final double MIN_SCALE = 0.25;
   private static final double MAX_SCALE = 8.0;
   private static final double DEFAULT_SCALE = 1.5;
   private static final double ZOOM_STEP = 1.25;
   private static final int RETRY_INTERVAL = 60;
   private static final int MARGIN_CHUNKS = 1;
   private static final int MAX_CHUNK_SPAN = 48;
   private static final int OVERLAY_ALPHA = 0x55;
   private static final int SELECTION_ALPHA = 0x99;
   private static final int HOVER_COLOUR = 0x80FFFFFF;
   private final GuiZonePlanner gui;
   private final TileZonePlanner tile;
   private final int mapOffsetX;
   private final int mapOffsetY;
   private final int mapW;
   private final int mapH;
   private final ZoneMapTextures textures = new ZoneMapTextures();
   private double camX;
   private double camZ;
   private double scale = DEFAULT_SCALE;
   private double targetScale = DEFAULT_SCALE;
   private boolean zoomAnchored;
   private double zoomAnchorMouseX;
   private double zoomAnchorMouseY;
   private double zoomAnchorWorldX;
   private double zoomAnchorWorldZ;
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
   private int cachedOverlayVersion = Integer.MIN_VALUE;
   private int cachedOverlayLayer = Integer.MIN_VALUE;
   private int[] overlayRuns = new int[0];
   private int[] lastScanBounds;

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

   public void close() {
      this.textures.close();
   }

   private ContainerZonePlanner container() {
      return this.gui.getMenu() instanceof ContainerZonePlanner c ? c : null;
   }

   private int activeLayer() {
      ItemStack carried = this.gui.getMenu().getCarried();
      if (!carried.isEmpty() && carried.getItem() instanceof ItemPaintbrush_BC8) {
         DyeColor colour = carried.get(BCCore.BRUSH_COLOR);
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

   private double centreX() {
      return this.mapX() + this.mapW / 2.0;
   }

   private double centreY() {
      return this.mapY() + this.mapH / 2.0;
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

   private int[] visibleChunkBounds() {
      double halfX = this.mapW / (2.0 * this.scale);
      double halfZ = this.mapH / (2.0 * this.scale);
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

   private void advanceZoom() {
      if (this.scale == this.targetScale) {
         return;
      }

      double next = this.scale + (this.targetScale - this.scale) * 0.3;
      if (Math.abs(this.targetScale - next) < 0.001 * this.targetScale) {
         next = this.targetScale;
      }

      if (this.zoomAnchored) {
         this.camX = this.zoomAnchorWorldX - (this.zoomAnchorMouseX - this.centreX()) / next;
         this.camZ = this.zoomAnchorWorldZ - (this.zoomAnchorMouseY - this.centreY()) / next;
      }

      this.scale = next;
      if (this.scale == this.targetScale) {
         this.zoomAnchored = false;
      }
   }

   @Override
   public void drawBackground(float partialTicks) {
      BCGraphics g = GuiIcon.getGuiGraphics();
      if (g == null) {
         return;
      }

      ContainerZonePlanner menu = this.container();
      ZonePlannerMapColours cache = menu != null ? menu.mapColours : null;
      this.advanceZoom();
      this.ensureVisibleChunks(menu, cache);
      if (cache == null) {
         return;
      }

      this.updateHover(cache);
      this.ensureOverlay(menu);
      int[] bounds = this.visibleChunkBounds();
      this.textures.update(cache, bounds[0], bounds[1], bounds[2], bounds[3]);
      int x0 = this.mapX();
      int y0 = this.mapY();
      g.enableScissor(x0, y0, x0 + this.mapW, y0 + this.mapH);
      double originX = this.centreX() - this.camX * this.scale;
      double originY = this.centreY() - this.camZ * this.scale;
      int baseX = Mth.floor(originX);
      int baseY = Mth.floor(originY);
      g.pushPoseGui();
      g.translateGui((float)(originX - baseX), (float)(originY - baseY));
      int rx0 = Math.floorDiv(bounds[0] * 16, ZoneMapTextures.REGION_BLOCKS);
      int rx1 = Math.floorDiv(bounds[2] * 16, ZoneMapTextures.REGION_BLOCKS);
      int rz0 = Math.floorDiv(bounds[1] * 16, ZoneMapTextures.REGION_BLOCKS);
      int rz1 = Math.floorDiv(bounds[3] * 16, ZoneMapTextures.REGION_BLOCKS);

      for (int rx = rx0; rx <= rx1; rx++) {
         for (int rz = rz0; rz <= rz1; rz++) {
            Identifier texture = this.textures.textureOf(rx, rz);
            if (texture != null) {
               int sx0 = baseX + this.toScreen(rx * ZoneMapTextures.REGION_BLOCKS);
               int sx1 = baseX + this.toScreen((rx + 1) * ZoneMapTextures.REGION_BLOCKS);
               int sy0 = baseY + this.toScreen(rz * ZoneMapTextures.REGION_BLOCKS);
               int sy1 = baseY + this.toScreen((rz + 1) * ZoneMapTextures.REGION_BLOCKS);
               g.blit(
                  texture, sx0, sy0, 0.0F, 0.0F, sx1 - sx0, sy1 - sy0, ZoneMapTextures.REGION_BLOCKS, ZoneMapTextures.REGION_BLOCKS,
                  ZoneMapTextures.REGION_BLOCKS, ZoneMapTextures.REGION_BLOCKS
               );
            }
         }
      }

      for (int i = 0; i + 4 < this.overlayRuns.length; i += 5) {
         this.fillBlocks(g, baseX, baseY, this.overlayRuns[i], this.overlayRuns[i + 1], this.overlayRuns[i + 2], this.overlayRuns[i + 3], this.overlayRuns[i + 4]);
      }

      if (this.selecting) {
         int minX = Math.min(this.selStartBX, this.selEndBX);
         int maxX = Math.max(this.selStartBX, this.selEndBX);
         int minZ = Math.min(this.selStartBZ, this.selEndBZ);
         int maxZ = Math.max(this.selStartBZ, this.selEndBZ);
         this.fillBlocks(g, baseX, baseY, minX, minZ, maxX + 1, maxZ + 1, SELECTION_ALPHA << 24 | this.selColourValue & 0xFFFFFF);
      }

      if (this.hasHover) {
         this.fillBlocks(g, baseX, baseY, this.hoverBlockX, this.hoverBlockZ, this.hoverBlockX + 1, this.hoverBlockZ + 1, HOVER_COLOUR);
      }

      g.popPoseGui();
      g.disableScissor();
   }

   private int toScreen(double world) {
      return Mth.floor(world * this.scale);
   }

   private void fillBlocks(BCGraphics g, int baseX, int baseY, int bx0, int bz0, int bx1, int bz1, int argb) {
      g.fill(baseX + this.toScreen(bx0), baseY + this.toScreen(bz0), baseX + this.toScreen(bx1), baseY + this.toScreen(bz1), argb);
   }

   @Override
   public void drawForeground(float partialTicks) {
      BCGraphics g = GuiIcon.getGuiGraphics();
      if (g != null && this.hasHover) {
         String text = this.hoverBlockY != ZonePlannerMapColours.NO_HEIGHT
            ? LocaleUtil.localize("gui.buildcraft.zone_planner.coords_y", this.hoverBlockX, this.hoverBlockZ, this.hoverBlockY)
            : LocaleUtil.localize("gui.buildcraft.zone_planner.coords", this.hoverBlockX, this.hoverBlockZ);
         Minecraft mc = Minecraft.getInstance();
         int tw = mc.font.width(text);
         int tx = this.mapX() + 2;
         int ty = this.mapY() + this.mapH - mc.font.lineHeight - 2;
         g.fill(tx - 1, ty - 1, tx + tw + 1, ty + mc.font.lineHeight, -1610612736);
         g.text(mc.font, text, tx, ty, -1);
      }
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
         this.overlayRuns = new int[0];
      } else if (layer >= 0 && layer < this.tile.layers.length) {
         this.overlayRuns = bakeRuns(this.collectLayers(layer, layer));
      } else {
         this.overlayRuns = bakeRuns(this.collectLayers(0, this.tile.layers.length - 1));
      }
   }

   /** Painted cells of the layers as (x, z, argb) triples in world coordinates. */
   private int[] collectLayers(int first, int last) {
      BlockPos tilePos = this.tile.getBlockPos();
      IntArrayList out = new IntArrayList();

      for (int layer = first; layer <= last; layer++) {
         ZonePlan plan = this.tile.layers[layer];
         if (plan != null) {
            int colour = OVERLAY_ALPHA << 24 | DyeColor.byId(layer).getTextureDiffuseColor() & 0xFFFFFF;

            for (int[] cell : plan.getAll()) {
               out.add(cell[0] + tilePos.getX());
               out.add(cell[1] + tilePos.getZ());
               out.add(colour);
            }
         }
      }

      return out.toIntArray();
   }

   /** Merges painted cells row by row into (x0, z, x1, z + 1, argb) runs so a large zone is a few fills, not thousands. */
   private static int[] bakeRuns(int[] cells) {
      Map<Integer, List<long[]>> rows = new HashMap<>();

      for (int i = 0; i + 2 < cells.length; i += 3) {
         rows.computeIfAbsent(cells[i + 1], k -> new ArrayList<>()).add(new long[]{cells[i], cells[i + 2]});
      }

      IntArrayList out = new IntArrayList();

      for (Map.Entry<Integer, List<long[]>> row : rows.entrySet()) {
         int z = row.getKey();
         List<long[]> sorted = row.getValue();
         sorted.sort((a, b) -> Long.compare(a[0], b[0]));
         int k = 0;

         while (k < sorted.size()) {
            int x0 = (int)sorted.get(k)[0];
            int colour = (int)sorted.get(k)[1];
            int x1 = x0 + 1;
            k++;

            while (k < sorted.size() && sorted.get(k)[0] == x1 && (int)sorted.get(k)[1] == colour) {
               x1++;
               k++;
            }

            out.add(x0);
            out.add(z);
            out.add(x1);
            out.add(z + 1);
            out.add(colour);
         }
      }

      return out.toIntArray();
   }

   private void ensureVisibleChunks(ContainerZonePlanner menu, ZonePlannerMapColours cache) {
      if (menu != null && cache != null) {
         boolean retry = ++this.retryCounter >= RETRY_INTERVAL;
         if (retry) {
            this.retryCounter = 0;
            cache.retryMissing();
         }

         int[] bounds = this.visibleChunkBounds();
         if (!retry && this.lastScanBounds != null && Arrays.equals(bounds, this.lastScanBounds)) {
            return;
         }

         this.lastScanBounds = bounds;
         List<Long> missing = null;

         for (int cx = bounds[0]; cx <= bounds[2]; cx++) {
            for (int cz = bounds[1]; cz <= bounds[3]; cz++) {
               long key = ZonePlannerChunkKeys.chunkKey(cx, cz);
               if (!cache.hasData(key) && !cache.isRequested(key)) {
                  cache.markRequested(key);
                  if (missing == null) {
                     missing = new ArrayList<>();
                  }

                  missing.add(key);
               }
            }
         }

         menu.requestChunks(missing);
      }
   }

   private int blockX(double mouseX) {
      return Mth.floor(this.camX + (mouseX - this.centreX()) / this.scale);
   }

   private int blockZ(double mouseY) {
      return Mth.floor(this.camZ + (mouseY - this.centreY()) / this.scale);
   }

   @Override
   public void onMouseClicked(int button) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      if (this.inBounds(mx, my)) {
         int layer = this.activeLayer();
         if (layer >= 0) {
            if (this.tile != null && (button == 0 || button == 1)) {
               this.selecting = true;
               this.selStartBX = this.blockX(mx);
               this.selStartBZ = this.blockZ(my);
               this.selEndBX = this.selStartBX;
               this.selEndBZ = this.selStartBZ;
               this.selColourValue = button == 0 ? 0xFF000000 | DyeColor.byId(layer).getTextureDiffuseColor() & 0xFFFFFF : 0xFFFF5555;
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
         this.selEndBX = this.blockX(mx);
         this.selEndBZ = this.blockZ(my);
      } else if (this.panning) {
         this.camX = this.panStartCamX - (mx - this.panStartMouseX) / this.scale;
         this.camZ = this.panStartCamZ - (my - this.panStartMouseY) / this.scale;
      }
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

      this.targetScale = Mth.clamp(this.targetScale * Math.pow(ZOOM_STEP, amount), MIN_SCALE, MAX_SCALE);
      this.zoomAnchorMouseX = mx;
      this.zoomAnchorMouseY = my;
      this.zoomAnchorWorldX = this.camX + (mx - this.centreX()) / this.scale;
      this.zoomAnchorWorldZ = this.camZ + (my - this.centreY()) / this.scale;
      this.zoomAnchored = true;
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
      this.hasHover = this.inBounds(mx, my);
      if (this.hasHover) {
         this.hoverBlockX = this.blockX(mx);
         this.hoverBlockZ = this.blockZ(my);
         this.hoverBlockY = cache.heightAt(ZonePlannerChunkKeys.chunkKey(this.hoverBlockX >> 4, this.hoverBlockZ >> 4), this.hoverBlockX, this.hoverBlockZ);
      }
   }
}
