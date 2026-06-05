/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import java.util.List;

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

import buildcraft.energy.BCEnergyItems;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.misc.LocaleUtil;

public class CombustionCoolantCategory extends AbstractRecipeCategory<CombustionCoolantJei> {
    private static final int WIDTH = 176, HEIGHT = 58;
    private static final int BUCKET = 1000;
    private static final int TEXT_COLOR = 0xFF404040;

    private static final int IN_X = 8, IN_Y = 4, TANK_W = 16, TANK_H = 40;
    private static final int SOLID_Y = 4;
    private static final int OUT_X = 40, OUT_Y = 4;

    public CombustionCoolantCategory(IGuiHelper guiHelper) {
        super(
                EngineFuelJeiTypes.COMBUSTION_COOLANT,
                Component.translatable("gui.jei.category.buildcraft.combustion_engine_coolant"),
                guiHelper.createDrawableItemLike(BCEnergyItems.ENGINE_IRON),
                WIDTH, HEIGHT
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CombustionCoolantJei recipe, IFocusGroup focuses) {
        if (recipe.isSolid()) {
            builder.addInputSlot(IN_X, SOLID_Y).addItemStacks(List.of(recipe.item()));
            FluidStack water = recipe.fluid();
            if (water != null && !water.isEmpty()) {
                IRecipeSlotBuilder waterSlot = builder.addOutputSlot(OUT_X, OUT_Y)
                        .setFluidRenderer(water.getAmount(), false, TANK_W, TANK_H);
                JeiFluids.addFluidStack(waterSlot, water);
            }
        } else {
            FluidStack fluid = recipe.fluid();
            if (fluid != null && !fluid.isEmpty()) {
                IRecipeSlotBuilder fluidSlot = builder.addInputSlot(IN_X, IN_Y)
                        .setFluidRenderer(BUCKET, false, TANK_W, TANK_H);
                JeiFluids.addFluidStack(fluidSlot, fluid, BUCKET);
            }
        }
    }

    @Override

    public void draw(CombustionCoolantJei recipe, IRecipeSlotsView slots, net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {

        Font font = Minecraft.getInstance().font;
        BCGraphics g = new BCGraphics(graphics);
        String line;
        if (recipe.isSolid()) {
            line = Component.translatable(
                    "gui.jei.category.buildcraft.combustion_engine_coolant.melts",
                    recipe.fluid().getAmount()).getString();
        } else {
            line = Component.translatable(
                    "gui.jei.category.buildcraft.combustion_engine_coolant.cooling",
                    String.format("%.4f", recipe.coolingPerMb())).getString();
        }
        g.text(font, line, IN_X, 48, TEXT_COLOR, false);
    }
}
