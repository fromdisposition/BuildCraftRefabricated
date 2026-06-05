/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import java.util.List;

import net.minecraft.client.Minecraft;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;

@SuppressWarnings("deprecation")
public class GuiElementFluidTank implements IInteractionElement {

    private final BuildCraftGui gui;
    private final IGuiArea area;
    private final ResourceHandler<FluidResource> tank;
    private final WidgetFluidTank widget;
    private final GuiIcon overlay;

    public GuiElementFluidTank(BuildCraftGui gui, IGuiArea area,
            ResourceHandler<FluidResource> tank,
            WidgetFluidTank widget,
            GuiIcon overlay) {
        this.gui = gui;
        this.area = area;
        this.tank = tank;
        this.widget = widget;
        this.overlay = overlay;
    }

    public ResourceHandler<FluidResource> getTank() {
        return tank;
    }

    @Override
    public double getX() { return area.getX(); }

    @Override
    public double getY() { return area.getY(); }

    @Override
    public double getWidth() { return area.getWidth(); }

    @Override
    public double getHeight() { return area.getHeight(); }

    @Override
    public void drawBackground(float partialTicks) {
        if (tank == null) return;

        buildcraft.lib.transfer.fluid.FluidResource fluid = tank.getResource(0);
        long capacity = tank.getCapacityAsLong(0, buildcraft.lib.transfer.fluid.FluidResource.EMPTY);
        long amount = tank.getAmountAsLong(0);
        if (!fluid.isEmpty() && capacity > 0 && amount > 0) {
            BCGraphics graphics = GuiIcon.getGuiGraphics();
            if (graphics != null) {
                drawFluid(graphics, fluid.toStack((int) amount), (int) amount, (int) capacity);
            }
        }

        if (overlay != null) {
            overlay.drawAt(area);
        }
    }

    private void drawFluid(BCGraphics graphics, FluidStack fluid, int amount, int capacity) {
        Identifier stillTexture = buildcraft.lib.misc.FluidUtilBC.getFluidTexture(fluid);
        if (stillTexture == null) return;

        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(stillTexture);

        int tintColor = buildcraft.lib.misc.FluidUtilBC.getFluidColor(fluid);

        int x = (int) area.getX();
        int y = (int) area.getY();
        int w = (int) area.getWidth();
        int h = (int) area.getHeight();

        int fillHeight = (int) ((float) amount / capacity * h);
        if (fillHeight <= 0 && amount > 0) fillHeight = 1;

        int fillY = y + h - fillHeight;

        buildcraft.lib.client.fluid.BcFluidGuiDrawer.drawTiled(
                graphics, x, fillY, w, fillHeight, sprite, tintColor);
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (tank == null) return;
        if (!contains(gui.mouse.getX(), gui.mouse.getY())) return;

        buildcraft.lib.transfer.fluid.FluidResource fluid = tank.getResource(0);
        long capacity = tank.getCapacityAsLong(0, buildcraft.lib.transfer.fluid.FluidResource.EMPTY);
        long amount = tank.getAmountAsLong(0);

        String name = fluid.isEmpty() || amount == 0 ? "Empty" : fluid.toStack(1).getHoverName().getString();
        tooltips.add(new ToolTip(
            name,
            net.minecraft.ChatFormatting.GRAY + (amount + " / " + capacity + " mB")
        ));
    }

    @Override
    public void onMouseClicked(int button) {
        if (widget != null && contains(gui.mouse.getX(), gui.mouse.getY())) {
            widget.sendClick();
        }
    }
}
