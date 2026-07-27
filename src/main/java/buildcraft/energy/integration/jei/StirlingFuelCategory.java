/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import buildcraft.api.mj.MjAPI;
import buildcraft.energy.BCEnergyItems;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.lib.integration.jei.JeiCategoryDraw;
import buildcraft.lib.misc.LocaleUtil;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class StirlingFuelCategory extends AbstractRecipeCategory<StirlingFuelJei> {
   private static final int CARD_W = 176, CARD_H = 48;
   private static final int IN_X = 8, IN_Y = 4, INFO_X = 30;

   public StirlingFuelCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.STIRLING_FUEL,
         Component.translatable("gui.jei.category.buildcraft.stirling_engine_fuel"),
         guiHelper.createDrawableItemLike(BCEnergyItems.ENGINE_STONE),
         CARD_W,
         CARD_H
      );
   }

   @Override
   public void setRecipe(IRecipeLayoutBuilder builder, StirlingFuelJei recipe, IFocusGroup focuses) {
      builder.addInputSlot(IN_X, IN_Y).addItemStacks(List.of(recipe.fuel()));
   }

   @Override
   public void draw(StirlingFuelJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      JeiCategoryDraw.text(graphics, LocaleUtil.localize("gui.jei.category.buildcraft.stirling_engine_fuel.rate", LocaleUtil.localizeMjFlow(MjAPI.MJ)), INFO_X, 5);
      JeiCategoryDraw.text(graphics, LocaleUtil.localize("gui.jei.category.buildcraft.stirling_engine_fuel.burn", recipe.burnTime()), INFO_X, 15);
   }
}
