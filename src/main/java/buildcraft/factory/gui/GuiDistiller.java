/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.factory.container.ContainerDistiller;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.GuiElementFluidTank;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;

public class GuiDistiller extends GuiBC8<ContainerDistiller> {
    private static final Identifier TEXTURE =
            Identifier.parse("buildcraftfactory:textures/gui/distiller.png");
    private static final int SIZE_X = 176, SIZE_Y = 161;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);

    private static final GuiIcon OVERLAY_VERTICAL = new GuiIcon(TEXTURE, 0, 161, 16, 38);
    private static final GuiIcon OVERLAY_HORIZONTAL = new GuiIcon(TEXTURE, 17, 161, 34, 17);

    private static final int CENTER_DST_X = 61, CENTER_DST_Y = 12;
    private static final int CENTER_W = 36, CENTER_H = 57;
    private static final GuiIcon OVERLAY_STUCK = new GuiIcon(TEXTURE, 176, 0, CENTER_W, CENTER_H);
    private static final GuiIcon OVERLAY_RUNNING = new GuiIcon(TEXTURE, 212, 0, CENTER_W, CENTER_H);

    private static final int TANK_IN_X = 44, TANK_IN_Y = 23;
    private static final int TANK_IN_W = 16, TANK_IN_H = 38;

    private static final int TANK_GAS_X = 98, TANK_GAS_Y = 10;
    private static final int TANK_GAS_W = 34, TANK_GAS_H = 17;

    private static final int TANK_LIQ_X = 98, TANK_LIQ_Y = 54;
    private static final int TANK_LIQ_W = 34, TANK_LIQ_H = 17;

    public GuiDistiller(ContainerDistiller menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void initGuiElements() {
        if (menu.tile != null) {
            mainGui.shownElements.add(new LedgerOwnership(
                mainGui,
                () -> menu.tile != null ? menu.tile.getOwner() : null,
                true
            ));
            mainGui.shownElements.add(new GuiElementFluidTank(
                mainGui,
                new GuiRectangle(TANK_IN_X, TANK_IN_Y, TANK_IN_W, TANK_IN_H).offset(mainGui.rootElement),
                menu.tile.getTankIn(),
                menu.widgetTankIn,
                OVERLAY_VERTICAL
            ));
            mainGui.shownElements.add(new GuiElementFluidTank(
                mainGui,
                new GuiRectangle(TANK_GAS_X, TANK_GAS_Y, TANK_GAS_W, TANK_GAS_H).offset(mainGui.rootElement),
                menu.tile.getTankGasOut(),
                menu.widgetTankGasOut,
                OVERLAY_HORIZONTAL
            ));
            mainGui.shownElements.add(new GuiElementFluidTank(
                mainGui,
                new GuiRectangle(TANK_LIQ_X, TANK_LIQ_Y, TANK_LIQ_W, TANK_LIQ_H).offset(mainGui.rootElement),
                menu.tile.getTankLiquidOut(),
                menu.widgetTankLiquidOut,
                OVERLAY_HORIZONTAL
            ));

            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_IN_X, TANK_IN_Y, TANK_IN_W, TANK_IN_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.distiller.input.title", 0xFF_FF_CC_88,
                    "buildcraft.help.distiller.input.desc")));
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_GAS_X, TANK_GAS_Y, TANK_GAS_W, TANK_GAS_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.distiller.gas_out.title", 0xFF_AA_DD_FF,
                    "buildcraft.help.distiller.gas_out.desc")));
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_LIQ_X, TANK_LIQ_Y, TANK_LIQ_W, TANK_LIQ_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.distiller.liquid_out.title", 0xFF_AA_33_AA,
                    "buildcraft.help.distiller.liquid_out.desc")));
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(CENTER_DST_X, CENTER_DST_Y, CENTER_W, CENTER_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.distiller.process.title", 0xFF_88_CC_88,
                    "buildcraft.help.distiller.process.desc1",
                    "buildcraft.help.distiller.process.desc2")));
        }
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();
        String titleStr = title.getString();
        graphics.text(font, titleStr, 8, 6, 0xFF404040, false);
        graphics.text(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0xFF404040, false);
    }

    @Override
    protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
        BCGraphics graphics = GuiIcon.getGuiGraphics();
        drawCenterStateOverlay();
        renderTankTooltip(graphics, mouseX, mouseY, menu.tile != null ? menu.tile.getTankIn() : null,
                TANK_IN_X, TANK_IN_Y, TANK_IN_W, TANK_IN_H);
        renderTankTooltip(graphics, mouseX, mouseY, menu.tile != null ? menu.tile.getTankGasOut() : null,
                TANK_GAS_X, TANK_GAS_Y, TANK_GAS_W, TANK_GAS_H);
        renderTankTooltip(graphics, mouseX, mouseY, menu.tile != null ? menu.tile.getTankLiquidOut() : null,
                TANK_LIQ_X, TANK_LIQ_Y, TANK_LIQ_W, TANK_LIQ_H);
    }

    private void drawCenterStateOverlay() {
        if (menu.tile == null) return;
        GuiIcon overlay = null;
        if (menu.tile.isActive()) {
            overlay = OVERLAY_RUNNING;
        } else if (menu.tile.isStuck()) {
            overlay = OVERLAY_STUCK;
        }
        if (overlay != null) {
            overlay.drawAt(leftPos + CENTER_DST_X, topPos + CENTER_DST_Y);
        }
    }

    private void renderTankTooltip(BCGraphics graphics, int mouseX, int mouseY,
            buildcraft.lib.transfer.fluid.FluidStacksResourceHandler tank, int relX, int relY, int w, int h) {
        if (tank == null) return;
        int absX = leftPos + relX;
        int absY = topPos + relY;
        if (mouseX >= absX && mouseX < absX + w && mouseY >= absY && mouseY < absY + h) {
            int amount = (int) tank.getAmountAsLong(0);
            int capacity = (int) tank.getCapacityAsLong(0, buildcraft.lib.transfer.fluid.FluidResource.EMPTY);

            List<Component> lines = new ArrayList<>();
            if (amount > 0) {
                lines.add(tank.getResource(0).toStack(amount).getHoverName());
            }
            lines.add(Component.literal(amount + " / " + capacity + " mB")
                    .withStyle(ChatFormatting.GRAY));
            java.util.List<net.minecraft.util.FormattedCharSequence> comps = new java.util.ArrayList<>();
            for (net.minecraft.network.chat.Component c : lines) {
                comps.add(c.getVisualOrderText());
            }
            graphics.setTooltipForNextFrame(font, comps, mouseX, mouseY);
        }
    }
}
