/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.pos.IGuiArea;

@SuppressWarnings("this-escape")
public abstract class GuiBC8<C extends ContainerBC_Neptune> extends AbstractContainerScreen<C> {

    public final BuildCraftGui mainGui;

    public int getGuiLeftPos() {
        return leftPos;
    }

    public int getGuiTopPos() {
        return topPos;
    }

    public int getGuiImageWidth() {
        return imageWidth;
    }

    public int getGuiImageHeight() {
        return imageHeight;
    }

    protected GuiBC8(C container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        IGuiArea rootArea = BuildCraftGui.createWindowedArea(this);
        this.mainGui = new BuildCraftGui(this, rootArea);
    }

    protected GuiBC8(C container, Inventory playerInventory, Component title, int xSize, int ySize) {

        super(container, playerInventory, title, xSize, ySize);

        IGuiArea rootArea = BuildCraftGui.createWindowedArea(this);
        this.mainGui = new BuildCraftGui(this, rootArea);
    }

    protected abstract void initGuiElements();

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    @Override
    protected void init() {
        super.init();

        java.util.Map<String, buildcraft.lib.gui.ledger.Ledger_Neptune> oldLedgers = new java.util.LinkedHashMap<>();
        for (IGuiElement elem : mainGui.shownElements) {
            if (elem instanceof buildcraft.lib.gui.ledger.Ledger_Neptune ledger) {
                oldLedgers.put(elem.getClass().getName(), ledger);
            }
        }

        IGuiArea rootArea = BuildCraftGui.createWindowedArea(this);
        mainGui.lowerLeftLedgerPos = rootArea.offset(0, 5);
        mainGui.lowerRightLedgerPos = rootArea.getPosition(1, -1).offset(0, 5);
        mainGui.shownElements.clear();
        initGuiElements();
        if (shouldAddHelpLedger()) {
            mainGui.shownElements.add(new LedgerHelp(mainGui, false));
        }

        if (!oldLedgers.isEmpty()) {
            for (IGuiElement elem : mainGui.shownElements) {
                if (elem instanceof buildcraft.lib.gui.ledger.Ledger_Neptune ledger) {
                    var oldLedger = oldLedgers.get(elem.getClass().getName());
                    if (oldLedger != null) {
                        ledger.copyAnimationStateFrom(oldLedger);
                    }
                }
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        mainGui.tick();
    }

    @Override

    public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        BCGraphics bcg = new BCGraphics(graphics);
        GuiIcon.setGuiGraphics(bcg);
        mainGui.drawBackgroundLayer(partialTicks, mouseX, mouseY, () -> {
            drawBackgroundTexture(bcg);
        });
        mainGui.drawElementBackgrounds();
    }

    @Override

    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {

        BCGraphics bcg = new BCGraphics(graphics);
        GuiIcon.setGuiGraphics(bcg);

        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        graphics.nextStratum();
        mainGui.drawDragLayer(bcg);
        mainGui.drawMenuOverlayLayer(bcg);
        drawTooltipLayer(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        int button = event.button();
        if (mainGui.onMouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        int button = event.button();
        mainGui.onMouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        int button = event.button();
        mainGui.onMouseDragged(mouseX, mouseY, button, 0);
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override

    protected void extractLabels(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

        BCGraphics bcg = new BCGraphics(graphics);
        GuiIcon.setGuiGraphics(bcg);
        mainGui.preDrawForeground();
        mainGui.drawElementForegrounds(null);
        mainGui.postDrawForeground();

        drawForegroundLayer();
    }

    protected void drawForegroundLayer() {
    }

    protected void drawBackgroundTexture(BCGraphics graphics) {

    }

    protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {

    }
}
