/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import buildcraft.energy.BCEnergyItems;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.integration.jei.JeiCategoryDraw;
import buildcraft.lib.integration.jei.JeiFluids;
import buildcraft.lib.misc.LocaleUtil;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class CombustionCoolantCategory extends AbstractRecipeCategory<CombustionCoolantJei> {
   private static final int CARD_W = 176, CARD_H = 58;
   private static final int IN_X = 8, IN_Y = 4, OUT_X = 40, OUT_Y = 4, TANK_W = 16, TANK_H = 40;

   public CombustionCoolantCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.COMBUSTION_COOLANT,
         Component.translatable("gui.jei.category.buildcraft.combustion_engine_coolant"),
         guiHelper.createDrawableItemLike(BCEnergyItems.ENGINE_IRON),
         CARD_W,
         CARD_H
      );
   }

   @Override
   public void setRecipe(IRecipeLayoutBuilder builder, CombustionCoolantJei recipe, IFocusGroup focuses) {
      if (recipe.isSolid()) {
         builder.addInputSlot(IN_X, IN_Y).addItemStacks(List.of(recipe.item()));
         FluidStack water = recipe.fluid();
         if (water != null && !water.isEmpty()) {
            IRecipeSlotBuilder waterSlot = builder.addOutputSlot(OUT_X, OUT_Y).setFluidRenderer(water.getAmount(), false, TANK_W, TANK_H);
            JeiFluids.addFluidStack(waterSlot, water);
         }
      } else {
         FluidStack fluid = recipe.fluid();
         if (fluid != null && !fluid.isEmpty()) {
            IRecipeSlotBuilder fluidSlot = builder.addInputSlot(IN_X, IN_Y).setFluidRenderer(1000L, false, TANK_W, TANK_H);
            JeiFluids.addFluidStack(fluidSlot, fluid, 1000L);
         }
      }
   }

   @Override
   public void draw(CombustionCoolantJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      String line = recipe.isSolid()
         ? LocaleUtil.localize("gui.jei.category.buildcraft.combustion_engine_coolant.melts", recipe.fluid().getAmount())
         : LocaleUtil.localize("gui.jei.category.buildcraft.combustion_engine_coolant.cooling", String.format("%.4f", recipe.coolingPerMb()));
      JeiCategoryDraw.text(graphics, line, IN_X, 48);
   }
}
