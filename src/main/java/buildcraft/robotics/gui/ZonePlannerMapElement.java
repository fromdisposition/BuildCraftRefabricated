/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.gui;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlan;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;

public class ZonePlannerMapElement implements IInteractionElement {

    private static final int SCALE = 3;

    private static final int PAN_STEP = 4;
    private static final int UNLOADED_COLOUR = 0xFF_10_10_10;

    private final GuiZonePlanner gui;
    private final TileZonePlanner tile;

    private final int mapOffsetX, mapOffsetY, mapW, mapH;

    private final int barOffsetX, barOffsetY;
    private static final int SWATCH_W = 12, SWATCH_H = 8;

    private final int viewW, viewH;

    private DyeColor selectedColour = DyeColor.WHITE;

    private int centerX, centerZ;

    private int[] colourCache;
    private int cacheCenterX = Integer.MIN_VALUE, cacheCenterZ = Integer.MIN_VALUE;

    public ZonePlannerMapElement(GuiZonePlanner gui, TileZonePlanner tile,
            int mapOffsetX, int mapOffsetY, int mapW, int mapH, int barOffsetX, int barOffsetY) {
        this.gui = gui;
        this.tile = tile;
        this.mapOffsetX = mapOffsetX;
        this.mapOffsetY = mapOffsetY;
        this.viewW = mapW / SCALE;
        this.viewH = mapH / SCALE;
        this.mapW = viewW * SCALE;
        this.mapH = viewH * SCALE;
        this.barOffsetX = barOffsetX;
        this.barOffsetY = barOffsetY;
        if (tile != null) {
            BlockPos pos = tile.getBlockPos();
            this.centerX = pos.getX();
            this.centerZ = pos.getZ();
        }
    }

    public DyeColor getSelectedColour() {
        return selectedColour;
    }

    private int mapX() {
        return gui.getGuiLeftPos() + mapOffsetX;
    }

    private int mapY() {
        return gui.getGuiTopPos() + mapOffsetY;
    }

    private int barX() {
        return gui.getGuiLeftPos() + barOffsetX;
    }

    private int barY() {
        return gui.getGuiTopPos() + barOffsetY;
    }

    @Override
    public double getX() {
        return Math.min(mapX(), barX());
    }

    @Override
    public double getY() {
        return Math.min(mapY(), barY());
    }

    @Override
    public double getWidth() {
        int right = Math.max(mapX() + mapW, barX() + 16 * SWATCH_W);
        return right - getX();
    }

    @Override
    public double getHeight() {
        int bottom = Math.max(mapY() + mapH, barY() + SWATCH_H);
        return bottom - getY();
    }

    private int firstBlockX() {
        return centerX - viewW / 2;
    }

    private int firstBlockZ() {
        return centerZ - viewH / 2;
    }

    @Override
    public void drawBackground(float partialTicks) {
        BCGraphics g = GuiIcon.getGuiGraphics();
        if (g == null) {
            return;
        }
        ensureCache();

        int ox = mapX();
        int oy = mapY();
        g.enableScissor(ox, oy, ox + mapW, oy + mapH);

        if (colourCache != null) {
            for (int j = 0; j < viewH; j++) {
                for (int i = 0; i < viewW; i++) {
                    int colour = colourCache[j * viewW + i];
                    int sx = ox + i * SCALE;
                    int sy = oy + j * SCALE;
                    g.fill(sx, sy, sx + SCALE, sy + SCALE, colour);
                }
            }
        }

        if (tile != null) {
            int selected = selectedColour.getId();
            for (int layer = 0; layer < tile.layers.length; layer++) {
                ZonePlan plan = tile.layers[layer];
                if (plan == null) {
                    continue;
                }
                boolean isSelected = layer == selected;
                int rgb = DyeColor.byId(layer).getTextureDiffuseColor() & 0xFF_FF_FF;
                int argb = (isSelected ? 0xC0_00_00_00 : 0x55_00_00_00) | rgb;
                drawLayerCells(g, plan, ox, oy, argb);
            }
        }

        g.disableScissor();

        int planX = tile != null ? tile.getBlockPos().getX() : centerX;
        int planZ = tile != null ? tile.getBlockPos().getZ() : centerZ;
        if (inView(planX, planZ)) {
            int sx = ox + (planX - firstBlockX()) * SCALE;
            int sy = oy + (planZ - firstBlockZ()) * SCALE;
            g.fill(sx - 1, sy, sx + SCALE + 1, sy + SCALE, 0xFF_FF_FF_FF);
            g.fill(sx, sy - 1, sx + SCALE, sy + SCALE + 1, 0xFF_FF_FF_FF);
        }

        drawBorder(g, ox, oy, mapW, mapH, 0xFF_00_00_00);

        int bx = barX();
        int by = barY();
        for (int c = 0; c < 16; c++) {
            int rgb = DyeColor.byId(c).getTextureDiffuseColor() & 0xFF_FF_FF;
            int x = bx + c * SWATCH_W;
            g.fill(x, by, x + SWATCH_W - 1, by + SWATCH_H, 0xFF_00_00_00 | rgb);
            if (c == selectedColour.getId()) {
                drawBorder(g, x - 1, by - 1, SWATCH_W, SWATCH_H + 2, 0xFF_FF_FF_FF);
            }
        }
    }

    private void drawLayerCells(BCGraphics g, ZonePlan plan, int ox, int oy, int argb) {
        List<int[]> cells = plan.getAll();
        int bx0 = firstBlockX();
        int bz0 = firstBlockZ();
        for (int[] cell : cells) {
            int dx = cell[0] - bx0;
            int dz = cell[1] - bz0;
            if (dx < 0 || dz < 0 || dx >= viewW || dz >= viewH) {
                continue;
            }
            int sx = ox + dx * SCALE;
            int sy = oy + dz * SCALE;
            g.fill(sx, sy, sx + SCALE, sy + SCALE, argb);
        }
    }

    private static void drawBorder(BCGraphics g, int x, int y, int w, int h, int colour) {
        g.fill(x, y, x + w, y + 1, colour);
        g.fill(x, y + h - 1, x + w, y + h, colour);
        g.fill(x, y, x + 1, y + h, colour);
        g.fill(x + w - 1, y, x + w, y + h, colour);
    }

    private boolean inView(int worldX, int worldZ) {
        int dx = worldX - firstBlockX();
        int dz = worldZ - firstBlockZ();
        return dx >= 0 && dz >= 0 && dx < viewW && dz < viewH;
    }

    private void ensureCache() {
        if (colourCache != null && cacheCenterX == centerX && cacheCenterZ == centerZ) {
            return;
        }
        colourCache = new int[viewW * viewH];
        Level level = tile != null ? tile.getLevel() : null;
        int bx0 = firstBlockX();
        int bz0 = firstBlockZ();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int j = 0; j < viewH; j++) {
            for (int i = 0; i < viewW; i++) {
                int wx = bx0 + i;
                int wz = bz0 + j;
                colourCache[j * viewW + i] = sampleColumn(level, wx, wz, mpos);
            }
        }
        cacheCenterX = centerX;
        cacheCenterZ = centerZ;
    }

    private static int sampleColumn(Level level, int wx, int wz, BlockPos.MutableBlockPos mpos) {
        if (level == null) {
            return UNLOADED_COLOUR;
        }
        mpos.set(wx, level.getMinY(), wz);
        if (!level.hasChunkAt(mpos)) {
            return UNLOADED_COLOUR;
        }
        int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);
        int y = Math.max(level.getMinY(), topY - 1);
        mpos.set(wx, y, wz);
        BlockState state = level.getBlockState(mpos);
        int rgb;
        try {
            rgb = state.getMapColor(level, mpos).col;
        } catch (Throwable t) {
            rgb = 0x00_00_00;
        }
        if (rgb == 0) {
            return UNLOADED_COLOUR;
        }
        return 0xFF_00_00_00 | (rgb & 0xFF_FF_FF);
    }

    @Override
    public void onMouseClicked(int button) {
        double mx = gui.mainGui.mouse.getX();
        double my = gui.mainGui.mouse.getY();

        int bx = barX();
        int by = barY();
        if (mx >= bx && mx < bx + 16 * SWATCH_W && my >= by && my < by + SWATCH_H) {
            int c = (int) ((mx - bx) / SWATCH_W);
            if (c >= 0 && c < 16) {
                selectedColour = DyeColor.byId(c);
            }
            return;
        }
        paintAt(mx, my, button);
    }

    @Override
    public void onMouseDragged(int button, long ticksSinceClick) {
        paintAt(gui.mainGui.mouse.getX(), gui.mainGui.mouse.getY(), button);
    }

    private void paintAt(double mx, double my, int button) {
        int ox = mapX();
        int oy = mapY();
        if (mx < ox || my < oy || mx >= ox + mapW || my >= oy + mapH) {
            return;
        }
        if (tile == null) {
            return;
        }
        int i = (int) ((mx - ox) / SCALE);
        int j = (int) ((my - oy) / SCALE);
        int wx = firstBlockX() + i;
        int wz = firstBlockZ() + j;
        boolean set = button == 0;
        if (button != 0 && button != 1) {
            return;
        }
        int layer = selectedColour.getId();

        if (tile.layers[layer] == null) {
            tile.layers[layer] = new ZonePlan();
        }
        tile.layers[layer].set(wx, wz, set);
        gui.getMenu().sendPaint(layer, wx, wz, set);
    }

    @Override
    public boolean onKeyPress(char typedChar, int keyCode) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT:
                centerX -= PAN_STEP;
                return true;
            case GLFW.GLFW_KEY_RIGHT:
                centerX += PAN_STEP;
                return true;
            case GLFW.GLFW_KEY_UP:
                centerZ -= PAN_STEP;
                return true;
            case GLFW.GLFW_KEY_DOWN:
                centerZ += PAN_STEP;
                return true;
            default:
                return false;
        }
    }
}
