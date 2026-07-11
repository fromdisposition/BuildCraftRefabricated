/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.lib.integration.jei.JeiCategoryDraw;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.tile.TileProgrammingTable;
import java.util.List;
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
   private static final Identifier TEX = Identifier.parse("buildcraftsilicon:textures/gui/bcr/programming_table.png");
   // Crop keeps a 4px panel margin on every side: the panel's own dark border (row/col 0) and white bevel (1..2)
   // must stay outside it, or they show up as a stray white line at the top of the recipe card.
   private static final int BG_U = 3, BG_V = 13, BG_W = 153, BG_H = 81;
   private static final int INPUT_X = 5, INPUT_Y = 5, OPTIONS_X = 40, OPTIONS_Y = 5, OUTPUT_X = 5, OUTPUT_Y = 59;
   private static final int POWER_ALIGN_W = OPTIONS_X + TileProgrammingTable.WIDTH * 18;
   private static final int POWER_Y = BG_H + 5;
   private static final int CARD_H = BG_H + 16;
   private final IDrawable background;

   public ProgrammingTableCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.PROGRAMMING,
         Component.translatable("gui.jei.category.buildcraft.programming_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.PROGRAMMING_TABLE),
         BG_W,
         CARD_H
      );
      this.background = guiHelper.createDrawable(TEX, BG_U, BG_V, BG_W, BG_H);
   }

   @Override
   public void draw(ProgrammingRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      String mj = LocaleUtil.localizeMj(recipe.microJoules());
      if (!mj.isEmpty()) {
         JeiCategoryDraw.textRight(graphics, LocaleUtil.localize("gui.jei.category.buildcraft.programming_table.power", mj), POWER_ALIGN_W, POWER_Y);
      }
   }

   @Override
   public void setRecipe(IRecipeLayoutBuilder builder, ProgrammingRecipeJei recipe, IFocusGroup focuses) {
      IRecipeSlotBuilder inputBuilder = builder.addInputSlot(INPUT_X, INPUT_Y).addItemStacks(List.of(recipe.input()));
      int col = recipe.optionIndex() % TileProgrammingTable.WIDTH;
      int row = recipe.optionIndex() / TileProgrammingTable.WIDTH;
      IRecipeSlotBuilder optionBuilder = builder.addInputSlot(OPTIONS_X + col * 18, OPTIONS_Y + row * 18)
         .addItemStacks(List.of(recipe.option()));
      IRecipeSlotBuilder outputBuilder = builder.addInputSlot(OUTPUT_X, OUTPUT_Y).addItemStacks(List.of(recipe.option()));
      builder.createFocusLink(inputBuilder, optionBuilder, outputBuilder);
   }
}
