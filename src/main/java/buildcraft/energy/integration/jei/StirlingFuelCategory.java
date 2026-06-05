/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import buildcraft.api.mj.MjAPI;

import buildcraft.energy.BCEnergyItems;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.misc.LocaleUtil;

public class StirlingFuelCategory extends AbstractRecipeCategory<StirlingFuelJei> {
    private static final int WIDTH = 176, HEIGHT = 48;
    private static final int TEXT_COLOR = 0xFF404040;

    private static final int IN_X = 8, IN_Y = 4;

    public StirlingFuelCategory(IGuiHelper guiHelper) {
        super(
                EngineFuelJeiTypes.STIRLING_FUEL,
                Component.translatable("gui.jei.category.buildcraft.stirling_engine_fuel"),
                guiHelper.createDrawableItemLike(BCEnergyItems.ENGINE_STONE),
                WIDTH, HEIGHT
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StirlingFuelJei recipe, IFocusGroup focuses) {
        builder.addInputSlot(IN_X, IN_Y).addItemStacks(List.of(recipe.fuel()));
    }

    @Override

    public void draw(StirlingFuelJei recipe, IRecipeSlotsView slots, net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {

        Font font = Minecraft.getInstance().font;
        BCGraphics g = new BCGraphics(graphics);

        String rate = Component.translatable(
                "gui.jei.category.buildcraft.stirling_engine_fuel.rate",
                LocaleUtil.localizeMjFlow(MjAPI.MJ)).getString();
        String burn = Component.translatable(
                "gui.jei.category.buildcraft.stirling_engine_fuel.burn",
                recipe.burnTime()).getString();
        g.text(font, rate, 30, IN_Y + 1, TEXT_COLOR, false);
        g.text(font, burn, 30, IN_Y + 11, TEXT_COLOR, false);
    }
}
