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
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
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

/**
 * Top-down map of the zone planner. Region textures are drawn as flat quads in a pose scaled to real screen pixels,
 * with a whole number of pixels per block, so texels, zone fills, borders and the hovered block all sit on the same
 * grid at every zoom level.
 */
public class ZonePlannerMapElement implements IInteractionElement {
   private static final int PAN_STEP = 4;
   /** Screen pixels per block; below one the map is drawn through a downscaling pose. */
   private static final double[] ZOOM_LEVELS = {0.25, 0.5, 1.0, 2.0, 3.0, 4.0, 6.0, 8.0, 12.0, 16.0, 24.0, 32.0};
   private static final double DEFAULT_GUI_PIXELS_PER_BLOCK = 1.5;
   private static final int RETRY_INTERVAL = 60;
   private static final int MARGIN_CHUNKS = 1;
   private static final int MAX_CHUNK_SPAN = 48;
   private static final int OVERLAY_ALPHA = 0x55;
   private static final int BORDER_ALPHA = 0xE0;
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
   private int zoomIndex = -1;
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
   private int[] overlayEdgesX = new int[0];
   private int[] overlayEdgesZ = new int[0];
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

   private static double guiScale() {
      return Minecraft.getInstance().getWindow().getGuiScale();
   }

   /** Zoomed all the way out, the loaded square of chunks around the planner exactly fills the map's height. */
   private double minZoom() {
      int loaded = 2 * Minecraft.getInstance().options.getEffectiveRenderDistance() + 1;
      return this.mapH * guiScale() / (Math.min(MAX_CHUNK_SPAN, loaded) * 16.0);
   }

   /** Screen pixels per block for every zoom step: the fit-to-loaded-area step, then the whole-pixel steps above it. */
   private double[] levels() {
      double min = this.minZoom();
      int count = 1;

      for (double level : ZOOM_LEVELS) {
         if (level > min * 1.05) {
            count++;
         }
      }

      double[] levels = new double[count];
      levels[0] = min;
      int n = 1;

      for (double level : ZOOM_LEVELS) {
         if (level > min * 1.05) {
            levels[n++] = level;
         }
      }

      return levels;
   }

   /** Screen pixels per block. */
   private double zoom() {
      double[] levels = this.levels();
      if (this.zoomIndex < 0) {
         double wanted = DEFAULT_GUI_PIXELS_PER_BLOCK * guiScale();
         this.zoomIndex = 0;

         for (int i = 1; i < levels.length; i++) {
            if (Math.abs(levels[i] - wanted) < Math.abs(levels[this.zoomIndex] - wanted)) {
               this.zoomIndex = i;
            }
         }
      }

      this.zoomIndex = Mth.clamp(this.zoomIndex, 0, levels.length - 1);
      return levels[this.zoomIndex];
   }

   /** GUI units per block. */
   private double guiPerBlock() {
      return this.zoom() / guiScale();
   }

   /** Drawing units per block: a whole number of screen pixels, or one unit under a downscaling pose. */
   private int unit() {
      return (int)Math.max(1.0, this.zoom());
   }

   /** GUI units per drawing unit. */
   private double poseScale() {
      return this.zoom() / (this.unit() * guiScale());
   }

   private long originX() {
      return Math.round(this.mapW / (2.0 * this.poseScale()) - this.camX * this.unit());
   }

   private long originZ() {
      return Math.round(this.mapH / (2.0 * this.poseScale()) - this.camZ * this.unit());
   }

   private int blockX(double mouseX) {
      return (int)Math.floorDiv(Math.round((mouseX - this.mapX()) / this.poseScale()) - this.originX(), (long)this.unit());
   }

   private int blockZ(double mouseY) {
      return (int)Math.floorDiv(Math.round((mouseY - this.mapY()) / this.poseScale()) - this.originZ(), (long)this.unit());
   }

   private int[] visibleChunkBounds() {
      double halfX = this.mapW / (2.0 * this.guiPerBlock());
      double halfZ = this.mapH / (2.0 * this.guiPerBlock());
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

   @Override
   public void drawBackground(float partialTicks) {
      BCGraphics g = GuiIcon.getGuiGraphics();
      if (g == null) {
         return;
      }

      ContainerZonePlanner menu = this.container();
      ZonePlannerMapColours cache = menu != null ? menu.mapColours : null;
      this.ensureVisibleChunks(menu, cache);
      if (cache == null) {
         return;
      }

      this.updateHover(cache);
      this.ensureOverlay(menu);
      int[] bounds = this.visibleChunkBounds();
      this.textures.update(cache, bounds[0], bounds[1], bounds[2], bounds[3]);
      int unit = this.unit();
      long ox = this.originX();
      long oz = this.originZ();
      int x0 = this.mapX();
      int y0 = this.mapY();
      g.enableScissor(x0, y0, x0 + this.mapW, y0 + this.mapH);
      g.pushPoseGui();
      g.translateGui(x0, y0);
      float poseScale = (float)this.poseScale();
      g.scaleGui(poseScale, poseScale);
      int regionUnits = ZoneMapTextures.REGION_BLOCKS * unit;
      int rx0 = Math.floorDiv(bounds[0] * 16, ZoneMapTextures.REGION_BLOCKS);
      int rx1 = Math.floorDiv(bounds[2] * 16, ZoneMapTextures.REGION_BLOCKS);
      int rz0 = Math.floorDiv(bounds[1] * 16, ZoneMapTextures.REGION_BLOCKS);
      int rz1 = Math.floorDiv(bounds[3] * 16, ZoneMapTextures.REGION_BLOCKS);

      for (int rx = rx0; rx <= rx1; rx++) {
         for (int rz = rz0; rz <= rz1; rz++) {
            Identifier texture = this.textures.textureOf(rx, rz);
            if (texture != null) {
               int sx = (int)(ox + (long)rx * regionUnits);
               int sy = (int)(oz + (long)rz * regionUnits);
               g.blit(
                  texture, sx, sy, 0.0F, 0.0F, regionUnits, regionUnits, ZoneMapTextures.REGION_BLOCKS, ZoneMapTextures.REGION_BLOCKS,
                  ZoneMapTextures.REGION_BLOCKS, ZoneMapTextures.REGION_BLOCKS
               );
            }
         }
      }

      for (int i = 0; i + 4 < this.overlayRuns.length; i += 5) {
         this.fillBlocks(g, ox, oz, this.overlayRuns[i], this.overlayRuns[i + 1], this.overlayRuns[i + 2], this.overlayRuns[i + 3], this.overlayRuns[i + 4]);
      }

      int border = Math.max(1, unit / 6);

      for (int i = 0; i + 4 < this.overlayEdgesX.length; i += 5) {
         int bx0 = this.overlayEdgesX[i];
         int bx1 = this.overlayEdgesX[i + 1];
         int bz = this.overlayEdgesX[i + 2];
         boolean top = this.overlayEdgesX[i + 3] != 0;
         long y = oz + (long)bz * unit - (top ? 0 : border);
         g.fill((int)(ox + (long)bx0 * unit), (int)y, (int)(ox + (long)bx1 * unit), (int)(y + border), this.overlayEdgesX[i + 4]);
      }

      for (int i = 0; i + 4 < this.overlayEdgesZ.length; i += 5) {
         int bz0 = this.overlayEdgesZ[i];
         int bz1 = this.overlayEdgesZ[i + 1];
         int bx = this.overlayEdgesZ[i + 2];
         boolean left = this.overlayEdgesZ[i + 3] != 0;
         long x = ox + (long)bx * unit - (left ? 0 : border);
         g.fill((int)x, (int)(oz + (long)bz0 * unit), (int)(x + border), (int)(oz + (long)bz1 * unit), this.overlayEdgesZ[i + 4]);
      }

      if (this.selecting) {
         int minX = Math.min(this.selStartBX, this.selEndBX);
         int maxX = Math.max(this.selStartBX, this.selEndBX) + 1;
         int minZ = Math.min(this.selStartBZ, this.selEndBZ);
         int maxZ = Math.max(this.selStartBZ, this.selEndBZ) + 1;
         this.fillBlocks(g, ox, oz, minX, minZ, maxX, maxZ, SELECTION_ALPHA << 24 | this.selColourValue & 0xFFFFFF);
         int edge = borderColour(this.selColourValue);
         int sx0 = (int)(ox + (long)minX * unit);
         int sx1 = (int)(ox + (long)maxX * unit);
         int sz0 = (int)(oz + (long)minZ * unit);
         int sz1 = (int)(oz + (long)maxZ * unit);
         g.fill(sx0, sz0, sx1, sz0 + border, edge);
         g.fill(sx0, sz1 - border, sx1, sz1, edge);
         g.fill(sx0, sz0, sx0 + border, sz1, edge);
         g.fill(sx1 - border, sz0, sx1, sz1, edge);
      }

      if (this.hasHover) {
         this.fillBlocks(g, ox, oz, this.hoverBlockX, this.hoverBlockZ, this.hoverBlockX + 1, this.hoverBlockZ + 1, HOVER_COLOUR);
      }

      g.popPoseGui();
      g.disableScissor();
   }

   private void fillBlocks(BCGraphics g, long ox, long oz, int bx0, int bz0, int bx1, int bz1, int argb) {
      int unit = this.unit();
      g.fill((int)(ox + (long)bx0 * unit), (int)(oz + (long)bz0 * unit), (int)(ox + (long)bx1 * unit), (int)(oz + (long)bz1 * unit), argb);
   }

   private static int borderColour(int argb) {
      int r = (argb >> 16 & 0xFF) * 55 / 100;
      int g = (argb >> 8 & 0xFF) * 55 / 100;
      int b = (argb & 0xFF) * 55 / 100;
      return BORDER_ALPHA << 24 | r << 16 | g << 8 | b;
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
      int[] cells;
      if (this.tile == null) {
         cells = new int[0];
      } else if (layer >= 0 && layer < this.tile.layers.length) {
         cells = this.collectLayers(layer, layer);
      } else {
         cells = this.collectLayers(0, this.tile.layers.length - 1);
      }

      this.overlayRuns = bakeRuns(cells);
      int[][] edges = bakeEdges(cells);
      this.overlayEdgesX = edges[0];
      this.overlayEdgesZ = edges[1];
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

   /**
    * Outline of every painted area in a darker shade of its colour: an edge is drawn where the neighbouring cell is
    * not painted in the same colour. Returns horizontal edges as (x0, x1, z, top, argb) and vertical edges as
    * (z0, z1, x, left, argb), each merged along its length.
    */
   private static int[][] bakeEdges(int[] cells) {
      Long2IntOpenHashMap byCell = new Long2IntOpenHashMap();

      for (int i = 0; i + 2 < cells.length; i += 3) {
         byCell.put(ZonePlannerChunkKeys.chunkKey(cells[i], cells[i + 1]), cells[i + 2]);
      }

      Map<Long, List<Integer>> horizontal = new HashMap<>();
      Map<Long, List<Integer>> vertical = new HashMap<>();

      for (int i = 0; i + 2 < cells.length; i += 3) {
         int x = cells[i];
         int z = cells[i + 1];
         int colour = cells[i + 2];
         if (byCell.get(ZonePlannerChunkKeys.chunkKey(x, z - 1)) != colour) {
            horizontal.computeIfAbsent(edgeKey(z, true, colour), k -> new ArrayList<>()).add(x);
         }

         if (byCell.get(ZonePlannerChunkKeys.chunkKey(x, z + 1)) != colour) {
            horizontal.computeIfAbsent(edgeKey(z + 1, false, colour), k -> new ArrayList<>()).add(x);
         }

         if (byCell.get(ZonePlannerChunkKeys.chunkKey(x - 1, z)) != colour) {
            vertical.computeIfAbsent(edgeKey(x, true, colour), k -> new ArrayList<>()).add(z);
         }

         if (byCell.get(ZonePlannerChunkKeys.chunkKey(x + 1, z)) != colour) {
            vertical.computeIfAbsent(edgeKey(x + 1, false, colour), k -> new ArrayList<>()).add(z);
         }
      }

      return new int[][]{mergeEdges(horizontal), mergeEdges(vertical)};
   }

   private static long edgeKey(int line, boolean leading, int colour) {
      return (long)line << 33 | (leading ? 1L << 32 : 0L) | colour & 0xFFFFFFFFL;
   }

   private static int[] mergeEdges(Map<Long, List<Integer>> edges) {
      IntArrayList out = new IntArrayList();

      for (Map.Entry<Long, List<Integer>> entry : edges.entrySet()) {
         long key = entry.getKey();
         int line = (int)(key >> 33);
         boolean leading = (key >> 32 & 1L) != 0;
         int colour = borderColour((int)key);
         List<Integer> along = entry.getValue();
         along.sort(null);
         int k = 0;

         while (k < along.size()) {
            int a0 = along.get(k);
            int a1 = a0 + 1;
            k++;

            while (k < along.size() && along.get(k) == a1) {
               a1++;
               k++;
            }

            out.add(a0);
            out.add(a1);
            out.add(line);
            out.add(leading ? 1 : 0);
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
         double guiPerBlock = this.guiPerBlock();
         this.camX = this.panStartCamX - (mx - this.panStartMouseX) / guiPerBlock;
         this.camZ = this.panStartCamZ - (my - this.panStartMouseY) / guiPerBlock;
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

      int next = Mth.clamp(this.zoomIndex + (amount > 0.0 ? 1 : -1), 0, this.levels().length - 1);
      if (next == this.zoomIndex) {
         return true;
      }

      double before = this.guiPerBlock();
      double worldX = this.camX + (mx - this.mapX() - this.mapW / 2.0) / before;
      double worldZ = this.camZ + (my - this.mapY() - this.mapH / 2.0) / before;
      this.zoomIndex = next;
      double after = this.guiPerBlock();
      this.camX = worldX - (mx - this.mapX() - this.mapW / 2.0) / after;
      this.camZ = worldZ - (my - this.mapY() - this.mapH / 2.0) / after;
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
