/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.gui;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.transport.container.ContainerDiamondPipe;

public class GuiDiamondPipe extends GuiBC8<ContainerDiamondPipe> {
    private static final Identifier TEXTURE =
            Identifier.parse("buildcrafttransport:textures/gui/filter.png");
    private static final Identifier TEXTURE_CB =
            Identifier.parse("buildcrafttransport:textures/gui/filter_cb.png");
    private static final int SIZE_X = 175, SIZE_Y = 225;
    private static final GuiIcon ICON_GUI    = new GuiIcon(TEXTURE,    0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_GUI_CB = new GuiIcon(TEXTURE_CB, 0, 0, SIZE_X, SIZE_Y);

    private static final int FILTER_X = 8, FILTER_Y = 18;
    private static final int FILTER_W = 9 * 18 - 2, FILTER_H = 6 * 18 - 2;

    public GuiDiamondPipe(ContainerDiamondPipe menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        GuiIcon icon = buildcraft.lib.client.ColorBlindUtil.isActive() ? ICON_GUI_CB : ICON_GUI;
        icon.drawAt(mainGui.rootElement);
    }

    @Override
    protected void initGuiElements() {
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(FILTER_X, FILTER_Y, FILTER_W, FILTER_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.diamond_pipe.filter.title", 0xFF_88_CC_FF,
                        "buildcraft.help.diamond_pipe.filter.desc1",
                        "buildcraft.help.diamond_pipe.filter.desc2")));
    }

}
