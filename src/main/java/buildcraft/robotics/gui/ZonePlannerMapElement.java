package buildcraft.robotics.gui;

import buildcraft.core.BCCore;
import buildcraft.core.item.ItemPaintbrush_BC8;
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
import net.minecraft.world.item.ItemStack;
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
   private final int viewW;
   private final int viewH;
   private int centerX;
   private int centerZ;
   private int[] colourCache;
   private int cacheCenterX = Integer.MIN_VALUE;
   private int cacheCenterZ = Integer.MIN_VALUE;

   public ZonePlannerMapElement(GuiZonePlanner gui, TileZonePlanner tile, int mapOffsetX, int mapOffsetY, int mapW, int mapH) {
      this.gui = gui;
      this.tile = tile;
      this.mapOffsetX = mapOffsetX;
      this.mapOffsetY = mapOffsetY;
      this.viewW = mapW / 3;
      this.viewH = mapH / 3;
      this.mapW = this.viewW * 3;
      this.mapH = this.viewH * 3;
      if (tile != null) {
         BlockPos pos = tile.getBlockPos();
         this.centerX = pos.getX();
         this.centerZ = pos.getZ();
      }
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
            int selected = this.activeLayer();

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

      if (rgb == 0) {
         return -15724528;
      }

      return 0xFF000000 | shadeByHeight(rgb & 16777215, level, topY);
   }

   private static int shadeByHeight(int rgb, Level level, int topY) {
      int range = level.getHeight();
      double norm = range <= 0 ? 0.5 : (topY - level.getMinY()) / (double)range;
      norm = Math.max(0.0, Math.min(1.0, norm));
      double shade = 0.6 + 0.4 * norm;
      int r = (int)Math.min(255.0, ((rgb >> 16) & 0xFF) * shade);
      int g = (int)Math.min(255.0, ((rgb >> 8) & 0xFF) * shade);
      int b = (int)Math.min(255.0, (rgb & 0xFF) * shade);
      return r << 16 | g << 8 | b;
   }

   @Override
   public void onMouseClicked(int button) {
      double mx = this.gui.mainGui.mouse.getX();
      double my = this.gui.mainGui.mouse.getY();
      this.paintAt(mx, my, button);
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
            int layer = this.activeLayer();
            if (layer >= 0 && (button == 0 || button == 1)) {
               int i = (int)((mx - ox) / 3.0);
               int j = (int)((my - oy) / 3.0);
               int wx = this.firstBlockX() + i;
               int wz = this.firstBlockZ() + j;
               boolean set = button == 0;
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
