/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import java.util.ArrayList;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;

import buildcraft.silicon.EnumAssemblyRecipeState;
import buildcraft.silicon.container.ContainerAssemblyTable;

public class GuiAssemblyTable extends GuiBC8<ContainerAssemblyTable> {
    private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/assembly_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 220;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_SAVED = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 16, 16);
    private static final GuiIcon ICON_SAVED_ENOUGH = new GuiIcon(TEXTURE_BASE, SIZE_X, 16, 16, 16);
    private static final GuiIcon ICON_SAVED_ENOUGH_ACTIVE = new GuiIcon(TEXTURE_BASE, SIZE_X, 32, 16, 16);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 48, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(86, 36, 4, 70);

    private static final int INPUT_X = 8, INPUT_Y = 36, INPUT_W = 3 * 18 - 2, INPUT_H = 4 * 18 - 2;
    private static final int RECIPES_X = 116, RECIPES_Y = 36, RECIPES_W = 3 * 18 - 2, RECIPES_H = 4 * 18 - 2;

    public GuiAssemblyTable(ContainerAssemblyTable container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void initGuiElements() {

        if (menu.tile != null) {
            mainGui.shownElements.add(new LedgerOwnership(mainGui,
                    () -> menu.tile != null ? menu.tile.getOwner() : null, true));
        }
        mainGui.shownElements.add(new LedgerTablePower(mainGui, menu.tile, true));

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(INPUT_X, INPUT_Y, INPUT_W, INPUT_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.assembly_table.input.title", 0xFF_FF_CC_88,
                        "buildcraft.help.assembly_table.input.desc")));
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(RECIPES_X, RECIPES_Y, RECIPES_W, RECIPES_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.assembly_table.recipes.title", 0xFF_88_CC_88,
                        "buildcraft.help.assembly_table.recipes.desc1",
                        "buildcraft.help.assembly_table.recipes.desc2")));
        mainGui.shownElements.add(new DummyHelpElement(
                RECT_PROGRESS.offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.assembly_table.power.title", 0xFF_DD_AA_FF,
                        "buildcraft.help.assembly_table.power.desc")));
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);

        long target = menu.tile.getTarget();
        if (target != 0) {
            double v = (double) menu.tile.power / target;
            ICON_PROGRESS.drawCutInside(
                new GuiRectangle(
                    RECT_PROGRESS.x,
                    (int) (RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1 - v, 0)),
                    RECT_PROGRESS.width,
                    (int) Math.ceil(RECT_PROGRESS.height * Math.min(v, 1))
                ).offset(mainGui.rootElement)
            );
        }
        for (int i = 0; i < menu.tile.recipesStates.size(); i++) {
            EnumAssemblyRecipeState state = new ArrayList<>(menu.tile.recipesStates.values()).get(i);
            IGuiArea area = getRecipeArea(i);
            if (state == EnumAssemblyRecipeState.SAVED) {
                ICON_SAVED.drawAt(area);
            }
            if (state == EnumAssemblyRecipeState.PAUSED) {
                ICON_SAVED.drawAt(area);
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                ICON_SAVED_ENOUGH.drawAt(area);
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
                ICON_SAVED_ENOUGH_ACTIVE.drawAt(area);
            }
        }
    }

    private IGuiArea getRecipeArea(int index) {
        int posX = index % 3;
        int posY = index / 3;
        return new GuiRectangle(16, 16).offset(mainGui.rootElement)
            .offset(116 + posX * 18, 36 + posY * 18);
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();
        String title = I18n.get("block.buildcraftsilicon.assembly_table");
        graphics.text(font, title, (imageWidth - font.width(title)) / 2, 15, 0xFF404040, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();
            for (int i = 0; i < menu.tile.recipesStates.size(); i++) {
                IGuiArea area = getRecipeArea(i);
                if (area.contains(mouseX, mouseY)) {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
