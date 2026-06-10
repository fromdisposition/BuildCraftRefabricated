/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.lib.integration.jei.JeiCategoryDraw;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.tile.TileProgrammingTable;
import java.util.List;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ProgrammingTableCategory extends AbstractRecipeCategory<ProgrammingRecipeJei> {
   private static final Identifier TEX = Identifier.parse("buildcraftsilicon:textures/gui/programming_table.png");
   private static final int BG_U = 0, BG_V = 18, BG_W = 154, BG_H = 76;
   private static final int INPUT_X = 8, INPUT_Y = 18, OPTIONS_X = 43, OPTIONS_Y = 18, OUTPUT_X = 8, OUTPUT_Y = 72;
   private final IDrawable background;

   public ProgrammingTableCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.PROGRAMMING,
         Component.translatable("gui.jei.category.buildcraft.programming_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.PROGRAMMING_TABLE),
         BG_W,
         JeiCategoryDraw.cardH(BG_H)
      );
      this.background = guiHelper.createDrawable(TEX, BG_U, BG_V, BG_W, BG_H);
   }

   public void draw(ProgrammingRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      JeiCategoryDraw.mjPower(graphics, "gui.jei.category.buildcraft.programming_table.power", recipe.microJoules(), BG_W, BG_H);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, ProgrammingRecipeJei recipe, IFocusGroup focuses) {
      IRecipeSlotBuilder inputBuilder = (IRecipeSlotBuilder)builder.addInputSlot(INPUT_X, INPUT_Y).addItemStacks(List.of(recipe.input()));
      int col = recipe.optionIndex() % TileProgrammingTable.WIDTH;
      int row = recipe.optionIndex() / TileProgrammingTable.WIDTH;
      IRecipeSlotBuilder optionBuilder = (IRecipeSlotBuilder)builder.addInputSlot(OPTIONS_X + col * 18, OPTIONS_Y + row * 18)
         .addItemStacks(List.of(recipe.option()));
      IRecipeSlotBuilder outputBuilder = (IRecipeSlotBuilder)builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).setOutputSlotBackground().addItemStacks(List.of(recipe.option()));
      builder.createFocusLink(new IIngredientAcceptor[]{inputBuilder, optionBuilder, outputBuilder});
   }
}
