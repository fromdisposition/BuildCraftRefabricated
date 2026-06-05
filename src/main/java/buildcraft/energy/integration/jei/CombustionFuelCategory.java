/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.integration.jei.JeiFluids;

import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager.IDirtyFuel;

import buildcraft.energy.BCEnergyItems;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.misc.LocaleUtil;

public class CombustionFuelCategory extends AbstractRecipeCategory<IFuel> {
    private static final int WIDTH = 176, HEIGHT = 66;
    private static final int BUCKET = 1000;
    private static final int TEXT_COLOR = 0xFF404040;

    private static final int IN_X = 8, IN_Y = 4, TANK_W = 16, TANK_H = 40;
    private static final int RESIDUE_X = 32, RESIDUE_Y = 4;

    public CombustionFuelCategory(IGuiHelper guiHelper) {
        super(
                EngineFuelJeiTypes.COMBUSTION_FUEL,
                Component.translatable("gui.jei.category.buildcraft.combustion_engine_fuel"),
                guiHelper.createDrawableItemLike(BCEnergyItems.ENGINE_IRON),
                WIDTH, HEIGHT
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IFuel recipe, IFocusGroup focuses) {
        FluidStack fuel = recipe.getFluid();
        if (fuel != null && !fuel.isEmpty()) {
            IRecipeSlotBuilder fuelSlot = builder.addInputSlot(IN_X, IN_Y)
                    .setFluidRenderer(BUCKET, false, TANK_W, TANK_H);
            JeiFluids.addFluidStack(fuelSlot, fuel, BUCKET);
        }
        if (recipe instanceof IDirtyFuel dirty) {
            FluidStack residue = dirty.getResidue();
            if (residue != null && !residue.isEmpty()) {
                IRecipeSlotBuilder residueSlot = builder.addOutputSlot(RESIDUE_X, RESIDUE_Y)
                        .setFluidRenderer(residue.getAmount(), false, TANK_W, TANK_H);
                JeiFluids.addFluidStack(residueSlot, residue);
            }
        }
    }

    @Override

    public void draw(IFuel recipe, IRecipeSlotsView slots, net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {

        Font font = Minecraft.getInstance().font;
        BCGraphics g = new BCGraphics(graphics);

        g.text(font, LocaleUtil.localizeMjFlow(recipe.getPowerPerCycle()), IN_X, 48, TEXT_COLOR, false);
        String burn = Component.translatable(
                "gui.jei.category.buildcraft.combustion_engine_fuel.burn",
                recipe.getTotalBurningTime()).getString();
        g.text(font, burn, IN_X, 58, TEXT_COLOR, false);
    }
}
