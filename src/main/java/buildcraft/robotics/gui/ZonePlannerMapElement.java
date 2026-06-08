package buildcraft.robotics.gui;

import buildcraft.core.BCCore;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ZonePlannerMapElement implements IInteractionElement {
   private static final int PAN_STEP = 4;
   private static final int MIN_SCALE = 1;
   private static final int MAX_SCALE = 8;
   private static final int UNLOADED_COLOUR = -15724528;
   private static final int RETRY_INTERVAL = 60;
   private final GuiZonePlanner gui;
   private final TileZonePlanner tile;
   private final int mapOffsetX;
   private final int mapOffsetY;
   private final int boundsW;
   private final int boundsH;
   private int scale = 3;
   private int mapW;
   private int mapH;
   private int viewW;
   private int viewH;
   private int centerX;
   private int centerZ;
   private int retryCounter;
   private boolean panning;
   private double panStartMouseX;
   private double panStartMouseY;
   private int panStartCenterX;
   private int panStartCenterZ;
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
      this.boundsW = mapW;
      this.boundsH = mapH;
      this.recomputeView();
      if (tile != null) {
         BlockPos pos = tile.getBlockPos();
         this.centerX = pos.getX();
         this.centerZ = pos.getZ();
      }
   }

   private void recomputeView() {
      this.viewW = this.boundsW / this.scale;
      this.viewH = this.boundsH / this.scale;
      this.mapW = this.viewW * this.scale;
      this.mapH = this.viewH * this.scale;
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
      return (chunkX & 0xFFFFFFFFL) | ((long)chunkZ << 32);
   }

   private int firstBlockX() {
      return this.centerX - this.viewW / 2;
   }

   private int firstBlockZ() {
      return this.centerZ - this.viewH / 2;
   }

   @Override
   public void drawBackground(float partialTicks) {
      BCGraphics g = GuiIcon.getGuiGraphics();
      if (g != null) {
         ContainerZonePlanner menu = this.container();
         ZonePlannerMapColours cache = menu != null ? menu.mapColours : null;
         this.ensureVisibleChunks(menu, cache);
         int ox = this.mapX();
         int oy = this.mapY();
         int bx0 = this.firstBlockX();
         int bz0 = this.firstBlockZ();
         g.enableScissor(ox, oy, ox + this.mapW, oy + this.mapH);

         for (int j = 0; j < this.viewH; j++) {
            for (int i = 0; i < this.viewW; i++) {
               int wx = bx0 + i;
               int wz = bz0 + j;
               int colour = UNLOADED_COLOUR;
               if (cache != null) {
                  long key = chunkKey(wx >> 4, wz >> 4);
                  int cached = cache.colourAt(key, wx, wz);
                  if (cached != 0) {
                     colour = cached;
                  }
               }

               int sx = ox + i * this.scale;
               int sy = oy + j * this.scale;
               g.fill(sx, sy, sx + this.scale, sy + this.scale, colour);
            }
         }

         if (this.tile != null) {
            int selected = this.activeLayer();
            if (selected >= 0 && selected < this.tile.layers.length) {
               ZonePlan plan = this.tile.layers[selected];
               if (plan != null) {
                  int rgb = DyeColor.byId(selected).getTextureDiffuseColor() & 16777215;
                  int argb = 0x55000000 | rgb;
                  this.drawLayerCells(g, plan, ox, oy, argb);
               }
            }

            if (this.selecting) {
               this.drawSelection(g, ox, oy);
            }
         }

         g.disableScissor();
         this.updateHover(menu);
         int planX = this.tile != null ? this.tile.getBlockPos().getX() : this.centerX;
         int planZ = this.tile != null ? this.tile.getBlockPos().getZ() : this.centerZ;
         if (this.inView(planX, planZ)) {
            int sx = ox + (planX - bx0) * this.scale;
            int sy = oy + (planZ - bz0) * this.scale;
            g.fill(sx - 1, sy, sx + this.scale + 1, sy + this.scale, -1);
            g.fill(sx, sy - 1, sx + this.scale, sy + this.scale + 1, -1);
         }

         drawBorder(g, ox, oy, this.mapW, this.mapH, -16777216);
         this.drawHover(g, ox, oy);
      }
   }

   private void drawLayerCells(BCGraphics g, ZonePlan plan, int ox, int oy, int argb) {
      List<int[]> cells = plan.getAll();
      int bx0 = this.firstBlockX();
      int bz0 = this.firstBlockZ();
      BlockPos tilePos = this.tile.getBlockPos();

      for (int[] cell : cells) {
         int dx = cell[0] + tilePos.getX() - bx0;
         int dz = cell[1] + tilePos.getZ() - bz0;
         if (dx >= 0 && dz >= 0 && dx < this.viewW && dz < this.viewH) {
            int sx = ox + dx * this.scale;
            int sy = oy + dz * this.scale;
            g.fill(sx, sy, sx + this.scale, sy + this.scale, argb);
         }
      }
   }

   private void drawSelection(BCGraphics g, int ox, int oy) {
      int bx0 = this.firstBlockX();
      int bz0 = this.firstBlockZ();
      int minX = Math.min(this.selStartBX, this.selEndBX);
      int maxX = Math.max(this.selStartBX, this.selEndBX);
      int minZ = Math.min(this.selStartBZ, this.selEndBZ);
      int maxZ = Math.max(this.selStartBZ, this.selEndBZ);
      int sx = ox + (minX - bx0) * this.scale;
      int sy = oy + (minZ - bz0) * this.scale;
      int ex = ox + (maxX - bx0 + 1) * this.scale;
      int ey = oy + (maxZ - bz0 + 1) * this.scale;
      int colour = -1;
      g.fill(sx, sy, ex, sy + 1, colour);
      g.fill(sx, ey - 1, ex, ey, colour);
      g.fill(sx, sy, sx + 1, ey, colour);
      g.fill(ex - 1, sy, ex, ey, colour);
   }

   private void drawHover(BCGraphics g, int ox, int oy) {
      if (this.hasHover) {
         String text = "X:" + this.hoverBlockX + " Z:" + this.hoverBlockZ + (this.hoverBlockY != Integer.MIN_VALUE ? " Y:" + this.hoverBlockY : "");
         Minecraft mc = Minecraft.getInstance();
         int tw = mc.font.width(text);
         int tx = ox + 2;
         int ty = oy + this.mapH - mc.font.lineHeight - 2;
         g.fill(tx - 1, ty - 1, tx + tw + 1, ty + mc.font.lineHeight, -1610612736);
         g.text(mc.font, text, tx, ty, -1);
      }
   }

   private static void drawBorder(BCGraphics g, int x, int y, int w, int h, int colour) {
      g.fill(x, y, x + w, y + 1, colour);
      g.fill(x, y + h - 1, x + w, y + h, colour);
      g.fill(x, y, x + 1, y + h, colour);
      g.fill(x + w - 1, y, x + w, y + h, colour);
   }

   private boolean inView(int worldX, int worldZ) {
      int dx = worldX - this.firstBlockX();
      int dz = worldZ - this.firstBlockZ();
      return dx >= 0 && dz >= 0 && dx < this.viewW && dz < this.viewH;
   }

   private void ensureVisibleChunks(ContainerZonePlanner menu, ZonePlannerMapColours cache) {
      if (menu != null && cache != null) {
         if (++this.retryCounter >= RETRY_INTERVAL) {
            this.retryCounter = 0;
            cache.retryMissing();
         }

         int bx0 = this.firstBlockX();
         int bz0 = this.firstBlockZ();
         int cx0 = bx0 >> 4;
         int cx1 = (bx0 + this.viewW - 1) >> 4;
         int cz0 = bz0 >> 4;
         int cz1 = (bz0 + this.viewH - 1) >> 4;
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

   private int blockXAt(double mx) {
      return this.firstBlockX() + (int)Math.floor((mx - this.mapX()) / this.scale);
   }

   private int blockZAt(double my) {
      return this.firstBlockZ() + (int)Math.floor((my - this.mapY()) / this.scale);
   }

   private boolean inBounds(double mx, double my) {
      int ox = this.mapX();
      int oy = this.mapY();
      return mx >= ox && my >= oy && mx < ox + this.mapW && my < oy + this.mapH;
   }

   @Override
   public void onMouseClicked(int button) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      if (this.inBounds(mx, my)) {
         int layer = this.activeLayer();
         if (this.tile != null && layer >= 0 && (button == 0 || button == 1)) {
            this.selecting = true;
            this.selStartBX = this.blockXAt(mx);
            this.selStartBZ = this.blockZAt(my);
            this.selEndBX = this.selStartBX;
            this.selEndBZ = this.selStartBZ;
         } else {
            this.panning = true;
            this.panStartMouseX = mx;
            this.panStartMouseY = my;
            this.panStartCenterX = this.centerX;
            this.panStartCenterZ = this.centerZ;
         }
      }
   }

   @Override
   public void onMouseDragged(int button, long ticksSinceClick) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      if (this.selecting) {
         this.selEndBX = this.blockXAt(mx);
         this.selEndBZ = this.blockZAt(my);
      } else if (this.panning) {
         this.centerX = this.panStartCenterX - (int)((mx - this.panStartMouseX) / this.scale);
         this.centerZ = this.panStartCenterZ - (int)((my - this.panStartMouseY) / this.scale);
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

      int oldScale = this.scale;
      int newScale = amount > 0.0 ? this.scale + 1 : this.scale - 1;
      newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));
      if (newScale == oldScale) {
         return true;
      }

      int focusX = this.blockXAt(mx);
      int focusZ = this.blockZAt(my);
      this.scale = newScale;
      this.recomputeView();
      this.centerX = focusX;
      this.centerZ = focusZ;
      return true;
   }

   @Override
   public boolean onKeyPress(char typedChar, int keyCode) {
      switch (keyCode) {
         case 262:
            this.centerX += PAN_STEP;
            return true;
         case 263:
            this.centerX -= PAN_STEP;
            return true;
         case 264:
            this.centerZ += PAN_STEP;
            return true;
         case 265:
            this.centerZ -= PAN_STEP;
            return true;
         default:
            return false;
      }
   }

   private void updateHover(ContainerZonePlanner menu) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      if (this.inBounds(mx, my)) {
         this.hasHover = true;
         this.hoverBlockX = this.blockXAt(mx);
         this.hoverBlockZ = this.blockZAt(my);
         if (menu != null) {
            long key = chunkKey(this.hoverBlockX >> 4, this.hoverBlockZ >> 4);
            this.hoverBlockY = menu.mapColours.heightAt(key, this.hoverBlockX, this.hoverBlockZ);
         } else {
            this.hoverBlockY = Integer.MIN_VALUE;
         }
      } else {
         this.hasHover = false;
      }
   }
}
