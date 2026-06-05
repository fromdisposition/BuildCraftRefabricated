/* Copyright (c) 2017 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy.client.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;

@SuppressWarnings("deprecation")
public class GuiEngineIron_BC8 extends GuiBC8<ContainerEngineIron_BC8> {
    private static final Identifier TEXTURE = Identifier.parse("buildcraftenergy:textures/gui/combustion_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 177;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE, 176, 0, 16, 60);

    private static final int TANK_WIDTH = 16, TANK_HEIGHT = 60;
    private static final int TANK_FUEL_X = 26, TANK_FUEL_Y = 18;
    private static final int TANK_COOLANT_X = 80, TANK_COOLANT_Y = 18;
    private static final int TANK_RESIDUE_X = 134, TANK_RESIDUE_Y = 18;

    public GuiEngineIron_BC8(ContainerEngineIron_BC8 menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
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
                menu::isSyncedBurning,
                true
            ));

            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_FUEL_X, TANK_FUEL_Y, TANK_WIDTH, TANK_HEIGHT).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.tank.title.tankFuel", 0xFF_FF_33_33,
                    "buildcraft.help.tank.generic", "buildcraft.help.tank.fuel")
            ));
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_COOLANT_X, TANK_COOLANT_Y, TANK_WIDTH, TANK_HEIGHT).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.tank.title.tankCoolant", 0xFF_55_55_FF,
                    "buildcraft.help.tank.generic", "buildcraft.help.tank.coolant")
            ));
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TANK_RESIDUE_X, TANK_RESIDUE_Y, TANK_WIDTH, TANK_HEIGHT).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.tank.title.tankResidue", 0xFF_AA_33_AA,
                    "buildcraft.help.tank.generic", "buildcraft.help.tank.residue")
            ));
        }
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);

        drawFluidTank(graphics, TANK_FUEL_X, TANK_FUEL_Y,
            menu.getSyncedFuelFluid(), menu.getSyncedFuelAmount(), TileEngineIron_BC8.MAX_FLUID);
        drawFluidTank(graphics, TANK_COOLANT_X, TANK_COOLANT_Y,
            menu.getSyncedCoolantFluid(), menu.getSyncedCoolantAmount(), TileEngineIron_BC8.MAX_FLUID);
        drawFluidTank(graphics, TANK_RESIDUE_X, TANK_RESIDUE_Y,
            menu.getSyncedResidueFluid(), menu.getSyncedResidueAmount(), TileEngineIron_BC8.MAX_FLUID);
    }

    private void drawFluidTank(BCGraphics graphics, int x, int y, Fluid fluid, int amount, int maxAmount) {
        int drawX = (int) mainGui.rootElement.getX() + x;
        int drawY = (int) mainGui.rootElement.getY() + y;

        if (amount > 0 && maxAmount > 0 && fluid != null && fluid != Fluids.EMPTY) {
            int fillHeight = (int) ((float) amount / maxAmount * TANK_HEIGHT);
            if (fillHeight > 0) {
                drawFluidTexture(graphics, drawX, drawY + (TANK_HEIGHT - fillHeight),
                    TANK_WIDTH, fillHeight, fluid);
            }
        }

        ICON_TANK_OVERLAY.drawAt(drawX, drawY);
    }

    private void drawFluidTexture(BCGraphics graphics, int x, int y, int width, int height, Fluid fluid) {
        buildcraft.lib.fluids.FluidStack stack = new buildcraft.lib.fluids.FluidStack(fluid, 1);
        Identifier stillTexture = buildcraft.lib.misc.FluidUtilBC.getFluidTexture(stack);
        if (stillTexture == null) {
            stillTexture = Identifier.withDefaultNamespace("block/water_still");
        }

        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
            .getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(stillTexture);

        int tintColor = buildcraft.lib.misc.FluidUtilBC.getFluidColor(stack);

        buildcraft.lib.client.fluid.BcFluidGuiDrawer.drawTiled(
                graphics, x, y, width, height, sprite, tintColor);
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();
        String str = LocaleUtil.localize("tile.engineIron.name");
        int strWidth = font.width(str);
        int titleX = (imageWidth - strWidth) / 2;
        graphics.text(font, str, titleX, 6, 0xFF404040, false);
        graphics.text(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0xFF404040, false);
    }

    @Override
    protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
        BCGraphics graphics = GuiIcon.getGuiGraphics();

        renderTankTooltip(graphics, mouseX, mouseY, TANK_FUEL_X, TANK_FUEL_Y,
            menu.getSyncedFuelFluid(), menu.getSyncedFuelAmount(), TileEngineIron_BC8.MAX_FLUID);
        renderTankTooltip(graphics, mouseX, mouseY, TANK_COOLANT_X, TANK_COOLANT_Y,
            menu.getSyncedCoolantFluid(), menu.getSyncedCoolantAmount(), TileEngineIron_BC8.MAX_FLUID);
        renderTankTooltip(graphics, mouseX, mouseY, TANK_RESIDUE_X, TANK_RESIDUE_Y,
            menu.getSyncedResidueFluid(), menu.getSyncedResidueAmount(), TileEngineIron_BC8.MAX_FLUID);
    }

    private void renderTankTooltip(BCGraphics graphics, int mouseX, int mouseY,
                                    int tankX, int tankY, Fluid fluid, int amount, int maxAmount) {
        int absX = leftPos + tankX;
        int absY = topPos + tankY;
        if (mouseX >= absX && mouseX < absX + TANK_WIDTH
            && mouseY >= absY && mouseY < absY + TANK_HEIGHT) {

            List<Component> lines = new java.util.ArrayList<>();
            if (fluid != null && fluid != Fluids.EMPTY && amount > 0) {
                lines.add(new buildcraft.lib.fluids.FluidStack(fluid, amount).getHoverName());
            }
            lines.add(Component.literal(amount + " / " + maxAmount + " mB")
                .withStyle(net.minecraft.ChatFormatting.GRAY));

            java.util.List<net.minecraft.util.FormattedCharSequence> comps = new java.util.ArrayList<>();
            for (Component c : lines) {
                comps.add(c.getVisualOrderText());
            }
            graphics.setTooltipForNextFrame(font, comps, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (isTankClicked(mouseX, mouseY, TANK_FUEL_X, TANK_FUEL_Y)) {
            menu.widgetFuel.sendClick();
            return true;
        }
        if (isTankClicked(mouseX, mouseY, TANK_COOLANT_X, TANK_COOLANT_Y)) {
            menu.widgetCoolant.sendClick();
            return true;
        }
        if (isTankClicked(mouseX, mouseY, TANK_RESIDUE_X, TANK_RESIDUE_Y)) {
            menu.widgetResidue.sendClick();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean isTankClicked(double mouseX, double mouseY, int tankX, int tankY) {
        int absX = leftPos + tankX;
        int absY = topPos + tankY;
        return mouseX >= absX && mouseX < absX + TANK_WIDTH
            && mouseY >= absY && mouseY < absY + TANK_HEIGHT;
    }
}
