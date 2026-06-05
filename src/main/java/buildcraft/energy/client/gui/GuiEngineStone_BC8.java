/* Copyright (c) 2017 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy.client.gui;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;

public class GuiEngineStone_BC8 extends GuiBC8<ContainerEngineStone_BC8> {
    private static final Identifier TEXTURE = Identifier.parse("buildcraftenergy:textures/gui/steam_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);

    private final IGuiArea flameRect;

    public GuiEngineStone_BC8(ContainerEngineStone_BC8 menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
        this.flameRect = new GuiRectangle(81, 25, 14, 14).offset(mainGui.rootElement);
    }

    @Override
    protected void initGuiElements() {
        if (menu.engine != null) {

            mainGui.shownElements.add(new LedgerOwnership(mainGui,
                () -> menu.engine != null ? menu.engine.getOwner() : null,
                true
            ));

            mainGui.shownElements.add(new LedgerEngine(mainGui,
                menu::getSyncedCurrentOutput,
                menu::getSyncedPower,
                menu::getSyncedHeat,
                menu::getSyncedPowerStage,
                menu::isSyncedBurningEngine,
                true
            ));

            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(82, 26, 12, 12).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.stone_engine.flame.title", 0xFF_FF_FF_1F,
                    "buildcraft.help.stone_engine.flame")
            ));
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(80, 41, 16, 16).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.stone_engine.fuel.title", 0xFF_AA_33_33,
                    "buildcraft.help.stone_engine.fuel")
            ));
        }
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);

        if (menu.isBurning()) {
            float progress = menu.getBurnProgress();
            int flameHeight = (int) Math.ceil(progress * 14);
            graphics.blit(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                (int) flameRect.getX(),
                (int) (flameRect.getY() + flameRect.getHeight() - flameHeight),
                176f,
                (float) (14 - flameHeight),
                14,
                flameHeight + 2,
                256, 256
            );
        }
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();
        String str = LocaleUtil.localize("tile.engineStone.name");
        int strWidth = font.width(str);
        int titleX = (imageWidth - strWidth) / 2;
        graphics.text(font, str, titleX, 6, 0xFF404040, false);
        graphics.text(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0xFF404040, false);
    }
}
