/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.integration.jei;

import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.factory.BCFactoryItems;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.integration.jei.FluidContainerAliases;
import buildcraft.lib.integration.jei.JeiFluids;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class HeatExchangerCategory extends AbstractRecipeCategory<HeatExchangerRecipePair> {
   private static final int TEX_U = 3;
   private static final int TEX_V = 4;
   private static final int TEX_W = 170;
   private static final int TEX_H = 84;
   private static final int WIDTH = 170;
   private static final int HEIGHT = 84;
   private static final int START_IN_X = 41;
   private static final int START_IN_Y = 60;
   private static final int START_IN_W = 34;
   private static final int START_IN_H = 17;
   private static final int START_OUT_X = 113;
   private static final int START_OUT_Y = 39;
   private static final int START_OUT_W = 16;
   private static final int START_OUT_H = 38;
   private static final int END_IN_X = 41;
   private static final int END_IN_Y = 8;
   private static final int END_IN_W = 16;
   private static final int END_IN_H = 38;
   private static final int END_OUT_X = 95;
   private static final int END_OUT_Y = 8;
   private static final int END_OUT_W = 34;
   private static final int END_OUT_H = 17;
   private final IDrawable background;

   public HeatExchangerCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.HEAT_EXCHANGER,
         Component.translatable("gui.jei.category.buildcraft.heat_exchanger"),
         guiHelper.createDrawableItemLike(BCFactoryItems.HEAT_EXCHANGE),
         170,
         84
      );
      this.background = guiHelper.createDrawable(Identifier.parse("buildcraftfactory:textures/gui/heat_exchanger.png"), 3, 4, 170, 84);
   }

   public void draw(HeatExchangerRecipePair pair, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, HeatExchangerRecipePair pair, IFocusGroup focuses) {
      IRefineryRecipeManager.IHeatableRecipe heatable = pair.heatable();
      IRefineryRecipeManager.ICoolableRecipe coolable = pair.coolable();
      FluidStack hIn = heatable.in();
      if (!hIn.isEmpty()) {
         IRecipeSlotBuilder hInSlot = builder.addInputSlot(41, 60).setFluidRenderer(hIn.getAmount(), false, 34, 17);
         JeiFluids.addFluidStack(hInSlot, hIn);
         FluidContainerAliases.addAliases(builder, hIn, RecipeIngredientRole.INPUT);
      }

      FluidStack cIn = coolable.in();
      if (!cIn.isEmpty()) {
         IRecipeSlotBuilder cInSlot = builder.addInputSlot(41, 8).setFluidRenderer(cIn.getAmount(), false, 16, 38);
         JeiFluids.addFluidStack(cInSlot, cIn);
         FluidContainerAliases.addAliases(builder, cIn, RecipeIngredientRole.INPUT);
      }

      FluidStack hOut = heatable.out();
      if (hOut != null && !hOut.isEmpty()) {
         IRecipeSlotBuilder hOutSlot = builder.addOutputSlot(95, 8).setFluidRenderer(hOut.getAmount(), false, 34, 17);
         JeiFluids.addFluidStack(hOutSlot, hOut);
         FluidContainerAliases.addAliases(builder, hOut, RecipeIngredientRole.OUTPUT);
      }

      FluidStack cOut = coolable.out();
      if (cOut != null && !cOut.isEmpty()) {
         IRecipeSlotBuilder cOutSlot = builder.addOutputSlot(113, 39).setFluidRenderer(cOut.getAmount(), false, 16, 38);
         JeiFluids.addFluidStack(cOutSlot, cOut);
         FluidContainerAliases.addAliases(builder, cOut, RecipeIngredientRole.OUTPUT);
      }
   }
}
