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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraft.factory.BCFactoryItems;
import buildcraft.lib.integration.jei.FluidContainerAliases;
import buildcraft.lib.integration.jei.JeiFluids;

public class DistillerCategory extends AbstractRecipeCategory<IDistillationRecipe> {

    private static final int TEX_U = 3, TEX_V = 4;
    private static final int TEX_W = 170, TEX_H = 74;

    private static final int WIDTH = TEX_W;

    private static final int HEIGHT = TEX_H + 12;

    private static final int TANK_IN_X = 44 - TEX_U, TANK_IN_Y = 23 - TEX_V, TANK_IN_W = 16, TANK_IN_H = 38;
    private static final int TANK_GAS_X = 98 - TEX_U, TANK_GAS_Y = 10 - TEX_V, TANK_GAS_W = 34, TANK_GAS_H = 17;
    private static final int TANK_LIQ_X = 98 - TEX_U, TANK_LIQ_Y = 54 - TEX_V, TANK_LIQ_W = 34, TANK_LIQ_H = 17;

    private static final int POWER_X = 4, POWER_Y = TEX_H + 2;
    private static final int POWER_COLOR = 0xFF404040;

    private final IDrawable background;

    public DistillerCategory(IGuiHelper guiHelper) {
        super(
                DistillerRecipeTypes.DISTILLER,
                Component.translatable("gui.jei.category.buildcraft.distiller"),
                guiHelper.createDrawableItemLike(BCFactoryItems.DISTILLER),
                WIDTH, HEIGHT
        );
        this.background = guiHelper.createDrawable(
                Identifier.parse("buildcraftfactory:textures/gui/distiller.png"),
                TEX_U, TEX_V, TEX_W, TEX_H);
    }

    @Override

    public void draw(IDistillationRecipe recipe, IRecipeSlotsView slots, net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {

        background.draw(graphics);

        double mj = recipe.powerRequired() / (double) MjAPI.MJ;
        String powerStr = Component.translatable(
                "gui.jei.category.buildcraft.distiller.power",
                String.format("%.1f", mj),
                "MJ").getString();
        Font font = Minecraft.getInstance().font;
        new buildcraft.lib.gui.BCGraphics(graphics).text(font, powerStr, POWER_X, POWER_Y, POWER_COLOR, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IDistillationRecipe recipe, IFocusGroup focuses) {
        FluidStack in = recipe.in();
        if (!in.isEmpty()) {
            IRecipeSlotBuilder inSlot = builder.addInputSlot(TANK_IN_X, TANK_IN_Y)
                    .setFluidRenderer(in.getAmount(), false, TANK_IN_W, TANK_IN_H);
            JeiFluids.addFluidStack(inSlot, in);
            FluidContainerAliases.addAliases(builder, in, RecipeIngredientRole.INPUT);
        }
        FluidStack outGas = recipe.outGas();
        if (outGas != null && !outGas.isEmpty()) {
            IRecipeSlotBuilder gasSlot = builder.addOutputSlot(TANK_GAS_X, TANK_GAS_Y)
                    .setFluidRenderer(outGas.getAmount(), false, TANK_GAS_W, TANK_GAS_H);
            JeiFluids.addFluidStack(gasSlot, outGas);
            FluidContainerAliases.addAliases(builder, outGas, RecipeIngredientRole.OUTPUT);
        }
        FluidStack outLiquid = recipe.outLiquid();
        if (outLiquid != null && !outLiquid.isEmpty()) {
            IRecipeSlotBuilder liqSlot = builder.addOutputSlot(TANK_LIQ_X, TANK_LIQ_Y)
                    .setFluidRenderer(outLiquid.getAmount(), false, TANK_LIQ_W, TANK_LIQ_H);
            JeiFluids.addFluidStack(liqSlot, outLiquid);
            FluidContainerAliases.addAliases(builder, outLiquid, RecipeIngredientRole.OUTPUT);
        }
    }
}

