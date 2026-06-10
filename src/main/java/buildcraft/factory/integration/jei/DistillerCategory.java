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
import buildcraft.lib.integration.jei.JeiPowerText;
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

public class DistillerCategory extends AbstractRecipeCategory<IRefineryRecipeManager.IDistillationRecipe> {
   private static final int TEX_U = 3;
   private static final int TEX_V = 4;
   private static final int TEX_W = 170;
   private static final int TEX_H = 74;
   private static final int WIDTH = 170;
   private static final int HEIGHT = 86;
   private static final int TANK_IN_X = 41;
   private static final int TANK_IN_Y = 19;
   private static final int TANK_IN_W = 16;
   private static final int TANK_IN_H = 38;
   private static final int TANK_GAS_X = 95;
   private static final int TANK_GAS_Y = 6;
   private static final int TANK_GAS_W = 34;
   private static final int TANK_GAS_H = 17;
   private static final int TANK_LIQ_X = 95;
   private static final int TANK_LIQ_Y = 50;
   private static final int TANK_LIQ_W = 34;
   private static final int TANK_LIQ_H = 17;
   private static final int POWER_Y = TEX_H + 2;
   private final IDrawable background;

   public DistillerCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.DISTILLER,
         Component.translatable("gui.jei.category.buildcraft.distiller"),
         guiHelper.createDrawableItemLike(BCFactoryItems.DISTILLER),
         170,
         86
      );
      this.background = guiHelper.createDrawable(Identifier.parse("buildcraftfactory:textures/gui/distiller.png"), 3, 4, 170, 74);
   }

   public void draw(IRefineryRecipeManager.IDistillationRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      JeiPowerText.drawRightAligned(graphics, "gui.jei.category.buildcraft.distiller.power", recipe.powerRequired(), TEX_W, POWER_Y);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, IRefineryRecipeManager.IDistillationRecipe recipe, IFocusGroup focuses) {
      FluidStack in = recipe.in();
      if (!in.isEmpty()) {
         IRecipeSlotBuilder inSlot = builder.addInputSlot(41, 19).setFluidRenderer(in.getAmount(), false, 16, 38);
         JeiFluids.addFluidStack(inSlot, in);
         FluidContainerAliases.addAliases(builder, in, RecipeIngredientRole.INPUT);
      }

      FluidStack outGas = recipe.outGas();
      if (outGas != null && !outGas.isEmpty()) {
         IRecipeSlotBuilder gasSlot = builder.addOutputSlot(95, 6).setFluidRenderer(outGas.getAmount(), false, 34, 17);
         JeiFluids.addFluidStack(gasSlot, outGas);
         FluidContainerAliases.addAliases(builder, outGas, RecipeIngredientRole.OUTPUT);
      }

      FluidStack outLiquid = recipe.outLiquid();
      if (outLiquid != null && !outLiquid.isEmpty()) {
         IRecipeSlotBuilder liqSlot = builder.addOutputSlot(95, 50).setFluidRenderer(outLiquid.getAmount(), false, 34, 17);
         JeiFluids.addFluidStack(liqSlot, outLiquid);
         FluidContainerAliases.addAliases(builder, outLiquid, RecipeIngredientRole.OUTPUT);
      }
   }
}
