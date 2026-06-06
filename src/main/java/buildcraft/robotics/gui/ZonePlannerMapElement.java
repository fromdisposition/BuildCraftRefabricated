package buildcraft.robotics.gui;

import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class ZonePlannerMapElement implements IInteractionElement {
   private static final int SCALE = 3;
   private static final int PAN_STEP = 4;
   private static final int UNLOADED_COLOUR = -15724528;
   private final GuiZonePlanner gui;
   private final TileZonePlanner tile;
   private final int mapOffsetX;
   private final int mapOffsetY;
   private final int mapW;
   private final int mapH;
   private final int barOffsetX;
   private final int barOffsetY;
   private static final int SWATCH_W = 12;
   private static final int SWATCH_H = 8;
   private final int viewW;
   private final int viewH;
   private DyeColor selectedColour = DyeColor.WHITE;
   private int centerX;
   private int centerZ;
   private int[] colourCache;
   private int cacheCenterX = Integer.MIN_VALUE;
   private int cacheCenterZ = Integer.MIN_VALUE;

   public ZonePlannerMapElement(GuiZonePlanner gui, TileZonePlanner tile, int mapOffsetX, int mapOffsetY, int mapW, int mapH, int barOffsetX, int barOffsetY) {
      this.gui = gui;
      this.tile = tile;
      this.mapOffsetX = mapOffsetX;
      this.mapOffsetY = mapOffsetY;
      this.viewW = mapW / 3;
      this.viewH = mapH / 3;
      this.mapW = this.viewW * 3;
      this.mapH = this.viewH * 3;
      this.barOffsetX = barOffsetX;
      this.barOffsetY = barOffsetY;
      if (tile != null) {
         BlockPos pos = tile.getBlockPos();
         this.centerX = pos.getX();
         this.centerZ = pos.getZ();
      }
   }

   public DyeColor getSelectedColour() {
      return this.selectedColour;
   }

   private int mapX() {
      return this.gui.getGuiLeftPos() + this.mapOffsetX;
   }

   private int mapY() {
      return this.gui.getGuiTopPos() + this.mapOffsetY;
   }

   private int barX() {
      return this.gui.getGuiLeftPos() + this.barOffsetX;
   }

   private int barY() {
      return this.gui.getGuiTopPos() + this.barOffsetY;
   }

   @Override
   public double getX() {
      return Math.min(this.mapX(), this.barX());
   }

   @Override
   public double getY() {
      return Math.min(this.mapY(), this.barY());
   }

   @Override
   public double getWidth() {
      int right = Math.max(this.mapX() + this.mapW, this.barX() + 192);
      return right - this.getX();
   }

   @Override
   public double getHeight() {
      int bottom = Math.max(this.mapY() + this.mapH, this.barY() + 8);
      return bottom - this.getY();
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
         this.ensureCache();
         int ox = this.mapX();
         int oy = this.mapY();
         g.enableScissor(ox, oy, ox + this.mapW, oy + this.mapH);
         if (this.colourCache != null) {
            for (int j = 0; j < this.viewH; j++) {
               for (int i = 0; i < this.viewW; i++) {
                  int colour = this.colourCache[j * this.viewW + i];
                  int sx = ox + i * 3;
                  int sy = oy + j * 3;
                  g.fill(sx, sy, sx + 3, sy + 3, colour);
               }
            }
         }

         if (this.tile != null) {
            int selected = this.selectedColour.getId();

            for (int layer = 0; layer < this.tile.layers.length; layer++) {
               ZonePlan plan = this.tile.layers[layer];
               if (plan != null) {
                  boolean isSelected = layer == selected;
                  int rgb = DyeColor.byId(layer).getTextureDiffuseColor() & 16777215;
                  int argb = (isSelected ? -1073741824 : 1426063360) | rgb;
                  this.drawLayerCells(g, plan, ox, oy, argb);
               }
            }
         }

         g.disableScissor();
         int planX = this.tile != null ? this.tile.getBlockPos().getX() : this.centerX;
         int planZ = this.tile != null ? this.tile.getBlockPos().getZ() : this.centerZ;
         if (this.inView(planX, planZ)) {
            int sx = ox + (planX - this.firstBlockX()) * 3;
            int sy = oy + (planZ - this.firstBlockZ()) * 3;
            g.fill(sx - 1, sy, sx + 3 + 1, sy + 3, -1);
            g.fill(sx, sy - 1, sx + 3, sy + 3 + 1, -1);
         }

         drawBorder(g, ox, oy, this.mapW, this.mapH, -16777216);
         int bx = this.barX();
         int by = this.barY();

         for (int c = 0; c < 16; c++) {
            int rgb = DyeColor.byId(c).getTextureDiffuseColor() & 16777215;
            int x = bx + c * 12;
            g.fill(x, by, x + 12 - 1, by + 8, 0xFF000000 | rgb);
            if (c == this.selectedColour.getId()) {
               drawBorder(g, x - 1, by - 1, 12, 10, -1);
            }
         }
      }
   }

   private void drawLayerCells(BCGraphics g, ZonePlan plan, int ox, int oy, int argb) {
      List<int[]> cells = plan.getAll();
      int bx0 = this.firstBlockX();
      int bz0 = this.firstBlockZ();

      for (int[] cell : cells) {
         int dx = cell[0] - bx0;
         int dz = cell[1] - bz0;
         if (dx >= 0 && dz >= 0 && dx < this.viewW && dz < this.viewH) {
            int sx = ox + dx * 3;
            int sy = oy + dz * 3;
            g.fill(sx, sy, sx + 3, sy + 3, argb);
         }
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

   private void ensureCache() {
      if (this.colourCache == null || this.cacheCenterX != this.centerX || this.cacheCenterZ != this.centerZ) {
         this.colourCache = new int[this.viewW * this.viewH];
         Level level = this.tile != null ? this.tile.getLevel() : null;
         int bx0 = this.firstBlockX();
         int bz0 = this.firstBlockZ();
         MutableBlockPos mpos = new MutableBlockPos();

         for (int j = 0; j < this.viewH; j++) {
            for (int i = 0; i < this.viewW; i++) {
               int wx = bx0 + i;
               int wz = bz0 + j;
               this.colourCache[j * this.viewW + i] = sampleColumn(level, wx, wz, mpos);
            }
         }

         this.cacheCenterX = this.centerX;
         this.cacheCenterZ = this.centerZ;
      }
   }

   private static int sampleColumn(Level level, int wx, int wz, MutableBlockPos mpos) {
      if (level == null) {
         return -15724528;
      }

      mpos.set(wx, level.getMinY(), wz);
      if (!Mc26Compat.isChunkLoaded(level, mpos)) {
         return -15724528;
      }

      int topY = level.getHeight(Types.WORLD_SURFACE, wx, wz);
      int y = Math.max(level.getMinY(), topY - 1);
      mpos.set(wx, y, wz);
      BlockState state = level.getBlockState(mpos);

      int rgb;
      try {
         rgb = state.getMapColor(level, mpos).col;
      } catch (Throwable t) {
         rgb = 0;
      }

      return rgb == 0 ? -15724528 : 0xFF000000 | rgb & 16777215;
   }

   @Override
   public void onMouseClicked(int button) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      int bx = this.barX();
      int by = this.barY();
      if (mx >= bx && mx < bx + 192 && my >= by && my < by + 8) {
         int c = (int)((mx - bx) / 12.0);
         if (c >= 0 && c < 16) {
            this.selectedColour = DyeColor.byId(c);
         }
      } else {
         this.paintAt(mx, my, button);
      }
   }

   @Override
   public void onMouseDragged(int button, long ticksSinceClick) {
      this.paintAt(this.gui.mainGui.mouse.getX(), this.gui.mainGui.mouse.getY(), button);
   }

   private void paintAt(double mx, double my, int button) {
      int ox = this.mapX();
      int oy = this.mapY();
      if (!(mx < ox) && !(my < oy) && !(mx >= ox + this.mapW) && !(my >= oy + this.mapH)) {
         if (this.tile != null) {
            int i = (int)((mx - ox) / 3.0);
            int j = (int)((my - oy) / 3.0);
            int wx = this.firstBlockX() + i;
            int wz = this.firstBlockZ() + j;
            boolean set = button == 0;
            if (button == 0 || button == 1) {
               int layer = this.selectedColour.getId();
               if (this.tile.layers[layer] == null) {
                  this.tile.layers[layer] = new ZonePlan();
               }

               this.tile.layers[layer].set(wx, wz, set);
               ((ContainerZonePlanner)this.gui.getMenu()).sendPaint(layer, wx, wz, set);
            }
         }
      }
   }

   @Override
   public boolean onKeyPress(char typedChar, int keyCode) {
      switch (keyCode) {
         case 262:
            this.centerX += 4;
            return true;
         case 263:
            this.centerX -= 4;
            return true;
         case 264:
            this.centerZ += 4;
            return true;
         case 265:
            this.centerZ -= 4;
            return true;
         default:
            return false;
      }
   }
}
