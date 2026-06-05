/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;

public class GuiFilteredBuffer extends GuiBC8<ContainerFilteredBuffer_BC8> {
    private static final Identifier TEXTURE =
            Identifier.parse("buildcrafttransport:textures/gui/filtered_buffer.png");
    private static final int SIZE_X = 176, SIZE_Y = 169;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_EMPTY_SLOT = new GuiIcon(
            Identifier.parse("buildcrafttransport:textures/gui/empty_filtered_buffer_slot.png"), 0, 0, 16, 16, 16);
    private static final GuiIcon ICON_NOTHING_SLOT = new GuiIcon(
            Identifier.parse("buildcrafttransport:textures/gui/nothing_filtered_buffer_slot.png"), 0, 0, 16, 16, 16);

    public GuiFilteredBuffer(ContainerFilteredBuffer_BC8 menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);

        int rootX = (int) mainGui.rootElement.getX();
        int rootY = (int) mainGui.rootElement.getY();

        for (int i = 0; i < 9; i++) {
            net.minecraft.world.item.ItemStack stackFilter = menu.tile.invFilter.getStackInSlot(i);
            net.minecraft.world.item.ItemStack stackMain = menu.tile.invMain.getStackInSlot(i);

            int x = rootX + 8 + i * 18;
            int filterY = rootY + 27;
            int mainY = rootY + 61;

            if (stackFilter.isEmpty()) {
                ICON_EMPTY_SLOT.drawAt(x, filterY);
            }

            if (stackMain.isEmpty()) {
                if (!stackFilter.isEmpty()) {
                    graphics.fakeItem(stackFilter, x, mainY);
                    graphics.fill(x, mainY, x + 16, mainY + 16, 0xB28B8B8B);
                } else {
                    ICON_NOTHING_SLOT.drawAt(x, mainY);
                }
            }
        }
    }

    @Override
    protected void initGuiElements() {
        if (menu.tile != null) {
            mainGui.shownElements.add(new buildcraft.lib.gui.ledger.LedgerOwnership(mainGui,
                () -> menu.tile != null ? menu.tile.getOwner() : null,
                true
            ));
        }

        mainGui.shownElements.add(new DummyHelpElement(
            new GuiRectangle(8, 27, 160, 16).offset(mainGui.rootElement),
            new ElementHelpInfo("buildcraft.help.filtered_buffer.filter.title", 0xFF_55_55_FF,
                "buildcraft.help.filtered_buffer.filter")
        ));

        mainGui.shownElements.add(new DummyHelpElement(
            new GuiRectangle(8, 61, 160, 16).offset(mainGui.rootElement),
            new ElementHelpInfo("buildcraft.help.filtered_buffer.main.title", 0xFF_55_FF_55,
                "buildcraft.help.filtered_buffer.main")
        ));
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();
        String titleStr = title.getString();
        int titleWidth = font.width(titleStr);
        int titleX = (imageWidth - titleWidth) / 2;
        graphics.text(font, titleStr, titleX, 10, 0xFF404040, false);
    }
}
