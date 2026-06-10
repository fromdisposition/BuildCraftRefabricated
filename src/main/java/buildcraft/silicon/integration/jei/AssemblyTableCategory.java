/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.lib.integration.jei.JeiCategoryDraw;
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
   private static final Identifier TEX = Identifier.parse("buildcraftsilicon:textures/gui/assembly_table.png");
   private static final int BG_U = 3, BG_V = 27, BG_W = 89, BG_H = 74;
   private static final int CARD_W = 129;
   private static final int INPUT_X = 5, INPUT_Y = 9, OUTPUT_X = 105, OUTPUT_Y = 9;
   private final IDrawable background;

   public AssemblyTableCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.ASSEMBLY,
         Component.translatable("gui.jei.category.buildcraft.assembly_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.ASSEMBLY_TABLE),
         CARD_W,
         JeiCategoryDraw.cardH(BG_H)
      );
      this.background = guiHelper.createDrawable(TEX, BG_U, BG_V, BG_W, BG_H);
   }

   public void draw(AssemblyRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      JeiCategoryDraw.mjPower(graphics, "gui.jei.category.buildcraft.assembly_table.power", recipe.microJoules(), BG_W, BG_H);
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
         outputSlotBuilder = (IRecipeSlotBuilder)builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).setOutputSlotBackground().addItemStacks(recipe.outputs());
      }

      int linkIdx = recipe.focusLinkInputIndex();
      if (linkIdx >= 0 && linkIdx < inputCount && inputSlotBuilders[linkIdx] != null && outputSlotBuilder != null) {
         builder.createFocusLink(new IIngredientAcceptor[]{inputSlotBuilders[linkIdx], outputSlotBuilder});
      }
   }
}
