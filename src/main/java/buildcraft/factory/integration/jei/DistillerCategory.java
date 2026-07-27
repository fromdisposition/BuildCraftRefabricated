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
import buildcraft.lib.integration.jei.JeiCategoryDraw;
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

public class DistillerCategory extends AbstractRecipeCategory<IRefineryRecipeManager.IDistillationRecipe> {
   private static final Identifier TEX = Identifier.parse("buildcraftfactory:textures/gui/bcr/distiller.png");
   private static final int BG_U = 3, BG_V = 12, BG_W = 170, BG_H = 74;
   private static final int IN_X = 41, IN_Y = 19, IN_W = 16, IN_H = 38;
   private static final int GAS_X = 95, GAS_Y = 6, GAS_W = 34, GAS_H = 17;
   private static final int LIQ_X = 95, LIQ_Y = 50, LIQ_W = 34, LIQ_H = 17;
   private final IDrawable background;

   public DistillerCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.DISTILLER,
         Component.translatable("gui.jei.category.buildcraft.distiller"),
         guiHelper.createDrawableItemLike(BCFactoryItems.DISTILLER),
         BG_W,
         JeiCategoryDraw.cardH(BG_H)
      );
      this.background = guiHelper.createDrawable(TEX, BG_U, BG_V, BG_W, BG_H);
   }

   @Override
   public void draw(IRefineryRecipeManager.IDistillationRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      JeiCategoryDraw.mjPower(graphics, "gui.jei.category.buildcraft.distiller.power", recipe.powerRequired(), BG_W, BG_H);
   }

   @Override
   public void setRecipe(IRecipeLayoutBuilder builder, IRefineryRecipeManager.IDistillationRecipe recipe, IFocusGroup focuses) {
      FluidStack in = recipe.in();
      if (!in.isEmpty()) {
         IRecipeSlotBuilder inSlot = builder.addInputSlot(IN_X, IN_Y).setFluidRenderer(in.getAmount(), false, IN_W, IN_H);
         JeiFluids.addFluidStack(inSlot, in);
         FluidContainerAliases.addAliases(builder, in, RecipeIngredientRole.INPUT);
      }

      FluidStack outGas = recipe.outGas();
      if (outGas != null && !outGas.isEmpty()) {
         IRecipeSlotBuilder gasSlot = builder.addOutputSlot(GAS_X, GAS_Y).setFluidRenderer(outGas.getAmount(), false, GAS_W, GAS_H);
         JeiFluids.addFluidStack(gasSlot, outGas);
         FluidContainerAliases.addAliases(builder, outGas, RecipeIngredientRole.OUTPUT);
      }

      FluidStack outLiquid = recipe.outLiquid();
      if (outLiquid != null && !outLiquid.isEmpty()) {
         IRecipeSlotBuilder liqSlot = builder.addOutputSlot(LIQ_X, LIQ_Y).setFluidRenderer(outLiquid.getAmount(), false, LIQ_W, LIQ_H);
         JeiFluids.addFluidStack(liqSlot, outLiquid);
         FluidContainerAliases.addAliases(builder, outLiquid, RecipeIngredientRole.OUTPUT);
      }
   }
}
