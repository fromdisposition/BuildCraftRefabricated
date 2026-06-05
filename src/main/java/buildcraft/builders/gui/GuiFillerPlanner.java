/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.gui;

import java.util.List;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.statement.GuiElementStatement;
import buildcraft.lib.gui.statement.GuiElementStatementDrag;
import buildcraft.lib.gui.statement.GuiElementStatementParam;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.builders.container.ContainerFillerPlanner;

public class GuiFillerPlanner extends GuiBC8<ContainerFillerPlanner> {
    private static final Identifier TEXTURE = Identifier.parse("buildcraftbuilders:textures/gui/filler_planner.png");

    public GuiFillerPlanner(ContainerFillerPlanner container, Inventory playerInv, Component title) {

        super(container, playerInv, Component.translatable("item.buildcraftbuilders.filler_planner"), 176, 81);
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        return false;
    }

    @Override
    protected void initGuiElements() {
        mainGui.shownElements.add(new GuiElementStatementDrag(mainGui));
        mainGui.shownElements.add(new GuiElementStatementSource<>(mainGui, true, menu.possiblePatternsContext));

        IGuiArea patternArea = new GuiRectangle(12, 32, 32, 32).offset(mainGui.rootElement);
        mainGui.shownElements.add(new GuiElementStatement<>(mainGui, patternArea, menu.getPatternStatementClient(),
                menu.possiblePatternsContext, true) {
            @Override
            public void drawBackground(float partialTicks) {
                IFillerPattern statement = this.get();
                double x = getX();
                double y = getY();
                if (statement != null) {
                    buildcraft.api.core.render.ISprite sprite = statement.getSprite();
                    if (sprite != null) {
                        GuiIcon.drawAt(sprite, x, y, 32);
                    }
                } else {
                    GuiElementStatement.ICON_SLOT_NOT_SET.drawAt(x, y);
                }
            }
        });

        buildcraft.api.statements.IStatementContainer fakeContainer = new buildcraft.api.statements.IStatementContainer() {
            @Override public net.minecraft.world.level.block.entity.BlockEntity getTile() { return null; }
            @Override public net.minecraft.world.level.block.entity.BlockEntity getNeighbourTile(net.minecraft.core.Direction side) { return null; }
        };
        for (int i = 0; i < 4; i++) {
            IGuiArea paramArea = new GuiRectangle(53 + 18 * i, 39, 18, 18).offset(mainGui.rootElement);
            mainGui.shownElements.add(new GuiElementStatementParam(mainGui, paramArea, fakeContainer,
                    menu.getPatternStatementClient(), i, true));
        }

        IGuiArea invertArea = new GuiRectangle(152, 40, 16, 16).offset(mainGui.rootElement);
        mainGui.shownElements.add(new GuiElementSimple(mainGui, invertArea) {
            @Override
            public void addToolTips(List<ToolTip> tooltips) {
                if (contains(mainGui.mouse)) {
                    String key = menu.isInverted() ? "tip.filler.invert.on" : "tip.filler.invert.off";
                    tooltips.add(new ToolTip(LocaleUtil.localize(key)));
                }
            }

            @Override
            public void addHelpElements(List<HelpPosition> elements) {
                elements.add(new ElementHelpInfo("buildcraft.help.filler.invert.title", 0xFFCCAA88,
                        "buildcraft.help.filler.invert.desc").target(this));
            }
        });
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        int mx = (int) this.mainGui.mouse.getX() - leftPos;
        int my = (int) this.mainGui.mouse.getY() - topPos;

        boolean invertHover = mx >= 152 && mx < 168 && my >= 40 && my < 56;

        int invertU = menu.isInverted() ? 240 : 224;
        int invertV = invertHover ? 16 : 0;
        new GuiIcon(TEXTURE, invertU, invertV, 16, 16).drawAt(leftPos + 152, topPos + 40);
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();

        String titleStr = Component.translatable("item.buildcraftbuilders.filler_planner").getString();
        graphics.text(font, titleStr, (imageWidth - font.width(titleStr)) / 2, 10, 0xFF404040, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int mx = (int) event.x() - leftPos;
            int my = (int) event.y() - topPos;
            if (mx >= 152 && mx < 168 && my >= 40 && my < 56) {
                menu.sendMessage(ContainerFillerPlanner.NET_INVERT, (buf) -> {});
                if (this.minecraft.player != null) {
                    this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
