/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.recipe.CombustionFuelRecipe;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.integration.jei.JeiCategoryDraw;
import buildcraft.lib.integration.jei.JeiFluids;
import buildcraft.lib.misc.LocaleUtil;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class CombustionFuelCategory extends AbstractRecipeCategory<CombustionFuelRecipe> {
   private static final int CARD_W = 176, CARD_H = 66;
   private static final int FUEL_X = 8, FUEL_Y = 4, TANK_W = 16, TANK_H = 40;
   private static final int RESIDUE_X = 32, RESIDUE_Y = 4;

   public CombustionFuelCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.COMBUSTION_FUEL,
         Component.translatable("gui.jei.category.buildcraft.combustion_engine_fuel"),
         guiHelper.createDrawableItemLike(BCEnergyItems.ENGINE_IRON),
         CARD_W,
         CARD_H
      );
   }

   public void setRecipe(IRecipeLayoutBuilder builder, CombustionFuelRecipe recipe, IFocusGroup focuses) {
      FluidStack fuel = new FluidStack(recipe.fluid(), 1000);
      if (!fuel.isEmpty()) {
         IRecipeSlotBuilder fuelSlot = builder.addInputSlot(FUEL_X, FUEL_Y).setFluidRenderer(1000L, false, TANK_W, TANK_H);
         JeiFluids.addFluidStack(fuelSlot, fuel, 1000L);
      }

      if (recipe.isDirty()) {
         FluidStack residue = recipe.getResiduePerBucket();
         if (!residue.isEmpty()) {
            IRecipeSlotBuilder residueSlot = builder.addOutputSlot(RESIDUE_X, RESIDUE_Y).setFluidRenderer(residue.getAmount(), false, TANK_W, TANK_H);
            JeiFluids.addFluidStack(residueSlot, residue);
         }
      }
   }

   public void draw(CombustionFuelRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      JeiCategoryDraw.text(graphics, LocaleUtil.localizeMjFlow(recipe.powerPerCycle()), FUEL_X, 48);
      JeiCategoryDraw.text(
         graphics, LocaleUtil.localize("gui.jei.category.buildcraft.combustion_engine_fuel.burn", recipe.totalBurningTime()), FUEL_X, 58
      );
   }
}
