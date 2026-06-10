/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.lib.integration.jei.JeiPowerText;
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
   private static final int TEX_U = 0;
   private static final int TEX_V = 18;
   private static final int TEX_W = 154;
   private static final int TEX_H = 76;
   private static final int SLOT_Y_SHIFT = -TEX_V;
   private static final int INPUT_X = 8;
   private static final int INPUT_Y = 36 + SLOT_Y_SHIFT;
   private static final int OPTIONS_X = 43;
   private static final int OPTIONS_Y = 36 + SLOT_Y_SHIFT;
   private static final int OUTPUT_X = 8;
   private static final int OUTPUT_Y = 90 + SLOT_Y_SHIFT;
   private static final int POWER_Y = TEX_H + 2;
   private static final int WIDTH = TEX_W;
   private static final int HEIGHT = TEX_H + 12;
   private final IDrawable background;

   public ProgrammingTableCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.PROGRAMMING,
         Component.translatable("gui.jei.category.buildcraft.programming_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.PROGRAMMING_TABLE),
         WIDTH,
         HEIGHT
      );
      this.background = guiHelper.createDrawable(Identifier.parse("buildcraftsilicon:textures/gui/programming_table.png"), TEX_U, TEX_V, TEX_W, TEX_H);
   }

   public void draw(ProgrammingRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      JeiPowerText.drawRightAligned(graphics, "gui.jei.category.buildcraft.programming_table.power", recipe.microJoules(), WIDTH, POWER_Y);
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
