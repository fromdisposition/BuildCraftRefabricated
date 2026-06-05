/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;

import buildcraft.factory.container.ContainerHeatExchange;
import buildcraft.factory.tile.TileHeatExchange.EnumProgressState;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionEnd;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionStart;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.GuiElementFluidTank;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;

public class GuiHeatExchange extends GuiBC8<ContainerHeatExchange> {
    private static final Identifier TEXTURE =
            Identifier.parse("buildcraftfactory:textures/gui/heat_exchanger.png");
    private static final int SIZE_X = 176, SIZE_Y = 171;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);

    private static final GuiIcon OVERLAY_VERTICAL = new GuiIcon(TEXTURE, 0, 171, 16, 38);
    private static final GuiIcon OVERLAY_HORIZONTAL = new GuiIcon(TEXTURE, 17, 171, 34, 17);

    private static final int TANK_END_IN_X = 44, TANK_END_IN_Y = 12;
    private static final int TANK_END_IN_W = 16, TANK_END_IN_H = 38;

    private static final int TANK_START_IN_X = 44, TANK_START_IN_Y = 64;
    private static final int TANK_START_IN_W = 34, TANK_START_IN_H = 17;

    private static final int TANK_END_OUT_X = 98, TANK_END_OUT_Y = 12;
    private static final int TANK_END_OUT_W = 34, TANK_END_OUT_H = 17;

    private static final int TANK_START_OUT_X = 116, TANK_START_OUT_Y = 43;
    private static final int TANK_START_OUT_W = 16, TANK_START_OUT_H = 38;

    private static final int WIPE_SRC_X = 176, WIPE_SRC_Y = 71;
    private static final int WIPE_W = 54, WIPE_H = 71;
    private static final int WIPE_DST_X = 61, WIPE_DST_Y = 11;

    public GuiHeatExchange(ContainerHeatExchange menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void initGuiElements() {
        ExchangeSectionStart start = menu.startSection();
        ExchangeSectionEnd end = menu.endSection();

        if (start != null) {
            mainGui.shownElements.add(new GuiElementFluidTank(
                mainGui,
                new GuiRectangle(TANK_START_IN_X, TANK_START_IN_Y, TANK_START_IN_W, TANK_START_IN_H)
                        .offset(mainGui.rootElement),
                start.tankInput,
                menu.widgetTankStartInput,
                OVERLAY_HORIZONTAL
            ));
            mainGui.shownElements.add(new GuiElementFluidTank(
                mainGui,
                new GuiRectangle(TANK_START_OUT_X, TANK_START_OUT_Y, TANK_START_OUT_W, TANK_START_OUT_H)
                        .offset(mainGui.rootElement),
                start.tankOutput,
                menu.widgetTankStartOutput,
                OVERLAY_VERTICAL
            ));

            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_START_IN_X, TANK_START_IN_Y, TANK_START_IN_W, TANK_START_IN_H)
                        .offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.heat_exchange.cold_in.title", 0xFF_55_AA_FF,
                    "buildcraft.help.heat_exchange.cold_in.desc")));
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_START_OUT_X, TANK_START_OUT_Y, TANK_START_OUT_W, TANK_START_OUT_H)
                        .offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.heat_exchange.cooled_out.title", 0xFF_AA_CC_FF,
                    "buildcraft.help.heat_exchange.cooled_out.desc")));
        }
        if (end != null) {
            mainGui.shownElements.add(new GuiElementFluidTank(
                mainGui,
                new GuiRectangle(TANK_END_IN_X, TANK_END_IN_Y, TANK_END_IN_W, TANK_END_IN_H)
                        .offset(mainGui.rootElement),
                end.tankInput,
                menu.widgetTankEndInput,
                OVERLAY_VERTICAL
            ));
            mainGui.shownElements.add(new GuiElementFluidTank(
                mainGui,
                new GuiRectangle(TANK_END_OUT_X, TANK_END_OUT_Y, TANK_END_OUT_W, TANK_END_OUT_H)
                        .offset(mainGui.rootElement),
                end.tankOutput,
                menu.widgetTankEndOutput,
                OVERLAY_HORIZONTAL
            ));

            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_END_IN_X, TANK_END_IN_Y, TANK_END_IN_W, TANK_END_IN_H)
                        .offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.heat_exchange.hot_in.title", 0xFF_FF_55_55,
                    "buildcraft.help.heat_exchange.hot_in.desc")));
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_END_OUT_X, TANK_END_OUT_Y, TANK_END_OUT_W, TANK_END_OUT_H)
                        .offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.heat_exchange.heated_out.title", 0xFF_FF_AA_55,
                    "buildcraft.help.heat_exchange.heated_out.desc")));
        }

        mainGui.shownElements.add(new DummyHelpElement(
            new GuiRectangle(73, 36, 30, 21).offset(mainGui.rootElement),
            new ElementHelpInfo("buildcraft.help.heat_exchange.progress.title", 0xFF_88_CC_88,
                "buildcraft.help.heat_exchange.progress.desc1",
                "buildcraft.help.heat_exchange.progress.desc2")));
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);
    }

    @Override
    protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
        BCGraphics graphics = GuiIcon.getGuiGraphics();

        drawCenterWipeOverlay(partialTick);

        ExchangeSectionStart start = menu.startSection();
        ExchangeSectionEnd end = menu.endSection();
        if (start != null) {
            renderTankTooltip(graphics, mouseX, mouseY, start.tankInput,
                    TANK_START_IN_X, TANK_START_IN_Y, TANK_START_IN_W, TANK_START_IN_H);
            renderTankTooltip(graphics, mouseX, mouseY, start.tankOutput,
                    TANK_START_OUT_X, TANK_START_OUT_Y, TANK_START_OUT_W, TANK_START_OUT_H);
        }
        if (end != null) {
            renderTankTooltip(graphics, mouseX, mouseY, end.tankInput,
                    TANK_END_IN_X, TANK_END_IN_Y, TANK_END_IN_W, TANK_END_IN_H);
            renderTankTooltip(graphics, mouseX, mouseY, end.tankOutput,
                    TANK_END_OUT_X, TANK_END_OUT_Y, TANK_END_OUT_W, TANK_END_OUT_H);
        }
    }

    private void drawCenterWipeOverlay(float partialTicks) {
        ExchangeSectionStart start = menu.startSection();
        if (start == null) return;
        EnumProgressState state = start.getProgressState();
        if (state == EnumProgressState.OFF) return;

        double progress = Math.max(0.0, Math.min(1.0, start.getProgress(partialTicks)));
        int leftOffset;
        int visibleW;
        if (state == EnumProgressState.PREPARING) {
            leftOffset = 0;
            visibleW = (int) Math.round(progress * WIPE_W);
        } else if (state == EnumProgressState.STOPPING) {
            leftOffset = (int) Math.round((1.0 - progress) * WIPE_W);
            visibleW = WIPE_W - leftOffset;
        } else {
            leftOffset = 0;
            visibleW = WIPE_W;
        }
        if (visibleW <= 0) return;

        int absX = leftPos + WIPE_DST_X + leftOffset;
        int absY = topPos + WIPE_DST_Y;
        new GuiIcon(TEXTURE, WIPE_SRC_X + leftOffset, WIPE_SRC_Y, visibleW, WIPE_H)
                .drawAt(absX, absY);
    }

    private void renderTankTooltip(BCGraphics graphics, int mouseX, int mouseY,
            FluidStacksResourceHandler tank, int relX, int relY, int w, int h) {
        if (tank == null) return;
        int absX = leftPos + relX;
        int absY = topPos + relY;
        if (mouseX >= absX && mouseX < absX + w && mouseY >= absY && mouseY < absY + h) {
            int amount = (int) tank.getAmountAsLong(0);
            int capacity = (int) tank.getCapacityAsLong(0, FluidResource.EMPTY);

            List<Component> lines = new ArrayList<>();
            if (amount > 0) {
                lines.add(tank.getResource(0).toStack(amount).getHoverName());
            }
            lines.add(Component.literal(amount + " / " + capacity + " mB")
                    .withStyle(ChatFormatting.GRAY));
            List<FormattedCharSequence> comps = new ArrayList<>();
            for (Component c : lines) {
                comps.add(c.getVisualOrderText());
            }
            graphics.setTooltipForNextFrame(font, comps, mouseX, mouseY);
        }
    }
}
