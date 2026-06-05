/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.recipes.IRefineryRecipeManager.ICoolableRecipe;
import buildcraft.api.recipes.IRefineryRecipeManager.IHeatableRecipe;
import buildcraft.factory.BCFactoryItems;
import buildcraft.lib.integration.jei.FluidContainerAliases;
import buildcraft.lib.integration.jei.JeiFluids;

public class HeatExchangerCategory extends AbstractRecipeCategory<HeatExchangerRecipePair> {

    private static final int TEX_U = 3, TEX_V = 4;
    private static final int TEX_W = 170, TEX_H = 84;

    private static final int WIDTH = TEX_W;
    private static final int HEIGHT = TEX_H;

    private static final int START_IN_X = 44 - TEX_U, START_IN_Y = 64 - TEX_V, START_IN_W = 34, START_IN_H = 17;
    private static final int START_OUT_X = 116 - TEX_U, START_OUT_Y = 43 - TEX_V, START_OUT_W = 16, START_OUT_H = 38;
    private static final int END_IN_X = 44 - TEX_U, END_IN_Y = 12 - TEX_V, END_IN_W = 16, END_IN_H = 38;
    private static final int END_OUT_X = 98 - TEX_U, END_OUT_Y = 12 - TEX_V, END_OUT_W = 34, END_OUT_H = 17;

    private final IDrawable background;

    public HeatExchangerCategory(IGuiHelper guiHelper) {
        super(
                HeatExchangerRecipeTypes.PAIR,
                Component.translatable("gui.jei.category.buildcraft.heat_exchanger"),
                guiHelper.createDrawableItemLike(BCFactoryItems.HEAT_EXCHANGE),
                WIDTH, HEIGHT
        );
        this.background = guiHelper.createDrawable(
                Identifier.parse("buildcraftfactory:textures/gui/heat_exchanger.png"),
                TEX_U, TEX_V, TEX_W, TEX_H);
    }

    @Override

    public void draw(HeatExchangerRecipePair pair, IRecipeSlotsView slots, net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {

        background.draw(graphics);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HeatExchangerRecipePair pair, IFocusGroup focuses) {
        IHeatableRecipe heatable = pair.heatable();
        ICoolableRecipe coolable = pair.coolable();

        FluidStack hIn = heatable.in();
        if (!hIn.isEmpty()) {
            IRecipeSlotBuilder hInSlot = builder.addInputSlot(START_IN_X, START_IN_Y)
                    .setFluidRenderer(hIn.getAmount(), false, START_IN_W, START_IN_H);
            JeiFluids.addFluidStack(hInSlot, hIn);
            FluidContainerAliases.addAliases(builder, hIn, RecipeIngredientRole.INPUT);
        }
        FluidStack cIn = coolable.in();
        if (!cIn.isEmpty()) {
            IRecipeSlotBuilder cInSlot = builder.addInputSlot(END_IN_X, END_IN_Y)
                    .setFluidRenderer(cIn.getAmount(), false, END_IN_W, END_IN_H);
            JeiFluids.addFluidStack(cInSlot, cIn);
            FluidContainerAliases.addAliases(builder, cIn, RecipeIngredientRole.INPUT);
        }

        FluidStack hOut = heatable.out();
        if (hOut != null && !hOut.isEmpty()) {
            IRecipeSlotBuilder hOutSlot = builder.addOutputSlot(END_OUT_X, END_OUT_Y)
                    .setFluidRenderer(hOut.getAmount(), false, END_OUT_W, END_OUT_H);
            JeiFluids.addFluidStack(hOutSlot, hOut);
            FluidContainerAliases.addAliases(builder, hOut, RecipeIngredientRole.OUTPUT);
        }
        FluidStack cOut = coolable.out();
        if (cOut != null && !cOut.isEmpty()) {
            IRecipeSlotBuilder cOutSlot = builder.addOutputSlot(START_OUT_X, START_OUT_Y)
                    .setFluidRenderer(cOut.getAmount(), false, START_OUT_W, START_OUT_H);
            JeiFluids.addFluidStack(cOutSlot, cOut);
            FluidContainerAliases.addAliases(builder, cOut, RecipeIngredientRole.OUTPUT);
        }
    }
}

