/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.integration.jei;

import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.factory.BCFactoryItems;
import buildcraft.lib.fluid.stack.FluidStack;
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
   private static final Identifier TEX = Identifier.parse("buildcraftfactory:textures/gui/bcr/heat_exchanger.png");
   private static final int BG_U = 3, BG_V = 10, BG_W = 170, BG_H = 84;
   private static final int HOT_IN_X = 41, HOT_IN_Y = 60, HOT_IN_W = 34, HOT_IN_H = 17;
   private static final int COOL_IN_X = 41, COOL_IN_Y = 8, COOL_IN_W = 16, COOL_IN_H = 38;
   private static final int HOT_OUT_X = 95, HOT_OUT_Y = 8, HOT_OUT_W = 34, HOT_OUT_H = 17;
   private static final int COOL_OUT_X = 113, COOL_OUT_Y = 39, COOL_OUT_W = 16, COOL_OUT_H = 38;
   private final IDrawable background;

   public HeatExchangerCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.HEAT_EXCHANGER,
         Component.translatable("gui.jei.category.buildcraft.heat_exchanger"),
         guiHelper.createDrawableItemLike(BCFactoryItems.HEAT_EXCHANGE),
         BG_W,
         BG_H
      );
      this.background = guiHelper.createDrawable(TEX, BG_U, BG_V, BG_W, BG_H);
   }

   @Override
   public void draw(HeatExchangerRecipePair pair, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
   }

   @Override
   public void setRecipe(IRecipeLayoutBuilder builder, HeatExchangerRecipePair pair, IFocusGroup focuses) {
      IRefineryRecipeManager.IHeatableRecipe heatable = pair.heatable();
      IRefineryRecipeManager.ICoolableRecipe coolable = pair.coolable();
      FluidStack hIn = heatable.in();
      if (!hIn.isEmpty()) {
         IRecipeSlotBuilder hInSlot = builder.addInputSlot(HOT_IN_X, HOT_IN_Y).setFluidRenderer(hIn.getAmount(), false, HOT_IN_W, HOT_IN_H);
         JeiFluids.addFluidStack(hInSlot, hIn);
         FluidContainerAliases.addAliases(builder, hIn, RecipeIngredientRole.INPUT);
      }

      FluidStack cIn = coolable.in();
      if (!cIn.isEmpty()) {
         IRecipeSlotBuilder cInSlot = builder.addInputSlot(COOL_IN_X, COOL_IN_Y).setFluidRenderer(cIn.getAmount(), false, COOL_IN_W, COOL_IN_H);
         JeiFluids.addFluidStack(cInSlot, cIn);
         FluidContainerAliases.addAliases(builder, cIn, RecipeIngredientRole.INPUT);
      }

      FluidStack hOut = heatable.out();
      if (hOut != null && !hOut.isEmpty()) {
         IRecipeSlotBuilder hOutSlot = builder.addOutputSlot(HOT_OUT_X, HOT_OUT_Y).setFluidRenderer(hOut.getAmount(), false, HOT_OUT_W, HOT_OUT_H);
         JeiFluids.addFluidStack(hOutSlot, hOut);
         FluidContainerAliases.addAliases(builder, hOut, RecipeIngredientRole.OUTPUT);
      }

      FluidStack cOut = coolable.out();
      if (cOut != null && !cOut.isEmpty()) {
         IRecipeSlotBuilder cOutSlot = builder.addOutputSlot(COOL_OUT_X, COOL_OUT_Y).setFluidRenderer(cOut.getAmount(), false, COOL_OUT_W, COOL_OUT_H);
         JeiFluids.addFluidStack(cOutSlot, cOut);
         FluidContainerAliases.addAliases(builder, cOut, RecipeIngredientRole.OUTPUT);
      }
   }
}
