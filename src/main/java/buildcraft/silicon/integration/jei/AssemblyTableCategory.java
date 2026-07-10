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
import net.minecraft.world.item.ItemStack;

public class AssemblyTableCategory extends AbstractRecipeCategory<AssemblyRecipeJei> {
   private static final Identifier TEX = Identifier.parse("buildcraftsilicon:textures/gui/bcr/assembly_table.png");
   // Crop keeps a 4px panel margin on every side; ending it on the slot wells' white bevel row leaves a stray
   // white line along the bottom of the recipe card.
   private static final int BG_U = 3, BG_V = 13, BG_H = 80;
   private static final int INPUT_X = 5, INPUT_Y = 5, DISPLAY_X = 113, DISPLAY_Y = 5;
   private static final int BG_W = DISPLAY_X + 52 + INPUT_X;
   private static final int POWER_ALIGN_W = DISPLAY_X + 3 * 18;
   private static final int POWER_Y = BG_H + 5;
   private static final int CARD_H = BG_H + 16;
   private final IDrawable background;

   public AssemblyTableCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.ASSEMBLY,
         Component.translatable("gui.jei.category.buildcraft.assembly_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.ASSEMBLY_TABLE),
         BG_W,
         CARD_H
      );
      this.background = guiHelper.createDrawable(TEX, BG_U, BG_V, BG_W, BG_H);
   }

   public void draw(AssemblyRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      String mj = LocaleUtil.localizeMj(recipe.microJoules());
      if (!mj.isEmpty()) {
         JeiCategoryDraw.textRight(graphics, LocaleUtil.localize("gui.jei.category.buildcraft.assembly_table.power", mj), POWER_ALIGN_W, POWER_Y);
      }
   }

   public void setRecipe(IRecipeLayoutBuilder builder, AssemblyRecipeJei recipe, IFocusGroup focuses) {
      List<List<ItemStack>> inputs = recipe.inputSlots();
      int inputCount = Math.min(inputs.size(), 12);
      IRecipeSlotBuilder[] inputSlotBuilders = new IRecipeSlotBuilder[inputCount];

      for (int i = 0; i < inputCount; i++) {
         List<ItemStack> slot = inputs.get(i);
         if (!slot.isEmpty()) {
            inputSlotBuilders[i] = (IRecipeSlotBuilder)builder.addInputSlot(INPUT_X + i % 3 * 18, INPUT_Y + i / 3 * 18).addItemStacks(slot);
         }
      }

      IRecipeSlotBuilder outputSlotBuilder = null;
      if (!recipe.outputs().isEmpty()) {
         outputSlotBuilder = (IRecipeSlotBuilder)builder.addOutputSlot(DISPLAY_X, DISPLAY_Y).addItemStacks(recipe.outputs());
      }

      int linkIdx = recipe.focusLinkInputIndex();
      if (linkIdx >= 0 && linkIdx < inputCount && inputSlotBuilders[linkIdx] != null && outputSlotBuilder != null) {
         builder.createFocusLink(new IIngredientAcceptor[]{inputSlotBuilders[linkIdx], outputSlotBuilder});
      }
   }
}
