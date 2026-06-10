/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.lib.integration.jei.JeiPowerText;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
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
   private static final int TEX_U = 3;
   private static final int TEX_V = 27;
   private static final int TEX_W = 89;
   private static final int TEX_H = 74;
   private static final int SLOT_X = 5;
   private static final int SLOT_Y = 9;
   private static final int MAX_INPUT_SLOTS = 12;
   private static final int OUTPUT_X = 105;
   private static final int OUTPUT_Y = 9;
   private static final int POWER_Y = TEX_H + 2;
   private static final int WIDTH = 129;
   private static final int HEIGHT = TEX_H + 12;
   private final IDrawable background;

   public AssemblyTableCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.ASSEMBLY,
         Component.translatable("gui.jei.category.buildcraft.assembly_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.ASSEMBLY_TABLE),
         WIDTH,
         HEIGHT
      );
      this.background = guiHelper.createDrawable(Identifier.parse("buildcraftsilicon:textures/gui/assembly_table.png"), TEX_U, TEX_V, TEX_W, TEX_H);
   }

   public void draw(AssemblyRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      JeiPowerText.drawRightAligned(graphics, "gui.jei.category.buildcraft.assembly_table.power", recipe.microJoules(), TEX_W, POWER_Y);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, AssemblyRecipeJei recipe, IFocusGroup focuses) {
      List<List<ItemStack>> inputs = recipe.inputSlots();
      int inputCount = Math.min(inputs.size(), MAX_INPUT_SLOTS);
      IRecipeSlotBuilder[] inputSlotBuilders = new IRecipeSlotBuilder[inputCount];

      for (int i = 0; i < inputCount; i++) {
         List<ItemStack> slot = inputs.get(i);
         if (!slot.isEmpty()) {
            int col = i % 3;
            int row = i / 3;
            int x = SLOT_X + col * 18;
            int y = SLOT_Y + row * 18;
            inputSlotBuilders[i] = (IRecipeSlotBuilder)builder.addInputSlot(x, y).addItemStacks(slot);
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
