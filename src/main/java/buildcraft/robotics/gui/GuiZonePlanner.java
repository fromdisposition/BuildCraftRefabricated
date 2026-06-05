/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.gui;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;

public class GuiZonePlanner extends GuiBC8<ContainerZonePlanner> {
    private static final int SIZE_X = 256, SIZE_Y = 228;

    private static final int MAP_X = 17, MAP_Y = 17, MAP_W = 213, MAP_H = 117;

    private static final int BAR_X = 17, BAR_Y = MAP_Y + MAP_H + 3;

    private static final int PANEL_BG = 0xFF_C6_C6_C6;
    private static final int PANEL_BORDER = 0xFF_37_37_37;
    private static final int SLOT_BG = 0xFF_8B_8B_8B;
    private static final int SLOT_BORDER = 0xFF_37_37_37;

    private boolean requestedLayers = false;

    public GuiZonePlanner(ContainerZonePlanner menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void init() {
        super.init();
        if (!requestedLayers) {
            getMenu().requestLayers();
            requestedLayers = true;
        }
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        int x = getGuiLeftPos();
        int y = getGuiTopPos();

        graphics.fill(x, y, x + SIZE_X, y + SIZE_Y, PANEL_BG);
        graphics.fill(x, y, x + SIZE_X, y + 1, PANEL_BORDER);
        graphics.fill(x, y + SIZE_Y - 1, x + SIZE_X, y + SIZE_Y, PANEL_BORDER);
        graphics.fill(x, y, x + 1, y + SIZE_Y, PANEL_BORDER);
        graphics.fill(x + SIZE_X - 1, y, x + SIZE_X, y + SIZE_Y, PANEL_BORDER);

        for (Slot slot : getMenu().slots) {
            int sx = x + slot.x;
            int sy = y + slot.y;
            graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER);
            graphics.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {

        if (mainGui.onKeyTyped((char) 0, event.key())) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void initGuiElements() {
        TileZonePlanner tile = getMenu().tile;
        mainGui.shownElements.add(new ZonePlannerMapElement(
                this, tile, MAP_X, MAP_Y, MAP_W, MAP_H, BAR_X, BAR_Y));

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(MAP_X, MAP_Y, MAP_W, MAP_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.zone_planner.map.title", 0xFF_88_CC_88,
                        "buildcraft.help.zone_planner.map.desc1",
                        "buildcraft.help.zone_planner.map.desc2")));
    }
}
