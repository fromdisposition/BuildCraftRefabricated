/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.factory.container.ContainerChute;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;

public class GuiChute extends GuiBC8<ContainerChute> {
    private static final Identifier TEXTURE =
            Identifier.parse("buildcraftfactory:textures/gui/chute.png");
    private static final int SIZE_X = 176, SIZE_Y = 153;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);

    private static final int SLOTS_X = 62, SLOTS_Y = 18, SLOTS_W = 52, SLOTS_H = 34;

    public GuiChute(ContainerChute menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);
    }

    @Override
    protected void initGuiElements() {
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(SLOTS_X, SLOTS_Y, SLOTS_W, SLOTS_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.chute.slots.title", 0xFF_88_CC_88,
                        "buildcraft.help.chute.slots.desc1",
                        "buildcraft.help.chute.slots.desc2")));
    }

}
