/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.button.BCButton;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;

public class GuiDiamondWoodPipe extends GuiBC8<ContainerDiamondWoodPipe> {
    private static final Identifier TEXTURE =
            Identifier.parse("buildcrafttransport:textures/gui/pipe_emerald.png");
    private static final Identifier TEXTURE_BUTTON =
            Identifier.parse("buildcrafttransport:textures/gui/pipe_emerald_button.png");
    private static final int SIZE_X = 175, SIZE_Y = 161;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_ROUND_ROBIN_INDEX = new GuiIcon(TEXTURE, 176, 0, 20, 20);
    private static final GuiIcon ICON_ROUND_ROBIN_NONE = new GuiIcon(TEXTURE, 176, 20, 20, 20);

    private static final int FILTER_X = 8, FILTER_Y = 18, FILTER_W = 9 * 18 - 2, FILTER_H = 16;

    private static final int MODE_X = 7, MODE_Y = 41, MODE_W = 18 * 3, MODE_H = 18;

    private FilterButton whiteListButton;
    private FilterButton blackListButton;
    private FilterButton roundRobinButton;

    public GuiDiamondWoodPipe(ContainerDiamondWoodPipe menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);

        if (menu.behaviour.pipe.getFlow() instanceof buildcraft.api.transport.pipe.IFlowItems) {
            if (menu.behaviour.filterMode == FilterMode.ROUND_ROBIN) {
                GuiIcon icon = menu.behaviour.filterValid ? ICON_ROUND_ROBIN_INDEX : ICON_ROUND_ROBIN_NONE;
                int xOffset = menu.behaviour.filterValid ? 18 * menu.behaviour.currentFilter : 0;
                icon.drawAt(mainGui.rootElement.getX() + 6 + xOffset, mainGui.rootElement.getY() + 16);
            }
        }
    }

    @Override
    protected void initGuiElements() {
        int bx = leftPos + 7;
        int by = topPos + 41;

        whiteListButton = new FilterButton(bx, by, FilterMode.WHITE_LIST, 19, 19, "tip.PipeItemsEmerald.whitelist");
        blackListButton = new FilterButton(bx + 18, by, FilterMode.BLACK_LIST, 37, 19, "tip.PipeItemsEmerald.blacklist");

        addRenderableWidget(whiteListButton);
        addRenderableWidget(blackListButton);

        if (menu.behaviour.pipe.getFlow() instanceof buildcraft.api.transport.pipe.IFlowItems) {
            roundRobinButton = new FilterButton(bx + 36, by, FilterMode.ROUND_ROBIN, 55, 19, "tip.PipeItemsEmerald.roundrobin");
            addRenderableWidget(roundRobinButton);
        }

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(FILTER_X, FILTER_Y, FILTER_W, FILTER_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.diamond_wood_pipe.filter.title", 0xFF_88_CC_FF,
                        "buildcraft.help.diamond_wood_pipe.filter.desc1",
                        "buildcraft.help.diamond_wood_pipe.filter.desc2")));
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(MODE_X, MODE_Y, MODE_W, MODE_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.diamond_wood_pipe.mode.title", 0xFF_FF_CC_88,
                        "buildcraft.help.diamond_wood_pipe.mode.desc")));
    }

    private void setFilterMode(FilterMode mode) {
        menu.behaviour.filterMode = mode;
        menu.sendNewFilterMode(mode);
    }

    private class FilterButton extends BCButton {
        private final FilterMode mode;
        private final int iconU, iconV;

        public FilterButton(int x, int y, FilterMode mode, int u, int v, String tooltipKey) {
            super(x, y, 18, 18, Component.empty());
            this.mode = mode;
            this.iconU = u;
            this.iconV = v;
            this.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        }

        @Override
        public void onPress(InputWithModifiers modifiers) {
            setFilterMode(mode);
        }

        @Override
        protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int state;
            boolean selected = menu.behaviour.filterMode == mode;
            if (!this.active) {
                state = 0;
            } else if (isHovered()) {
                state = selected ? 4 : 2;
            } else {
                state = selected ? 3 : 1;
            }

            int baseU = state * 18;
            int baseV = 0;

            GuiIcon bgIcon = new GuiIcon(TEXTURE_BUTTON, baseU, baseV, width, height);
            bgIcon.drawAt(getX(), getY());

            GuiIcon fgIcon = new GuiIcon(TEXTURE_BUTTON, iconU, iconV, 16, 16);
            fgIcon.drawAt(getX() + 1, getY() + 1);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
