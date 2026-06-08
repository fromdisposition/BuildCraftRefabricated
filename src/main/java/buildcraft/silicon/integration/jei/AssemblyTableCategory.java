/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.api.mj.MjAPI;
import buildcraft.lib.gui.BCGraphics;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class AssemblyTableCategory extends AbstractRecipeCategory<AssemblyRecipeJei> {
   private static final int TEX_U = 3;
   private static final int TEX_V = 27;
   private static final int TEX_W = 89;
   private static final int TEX_H = 86;
   private static final int SLOT_X = 5;
   private static final int SLOT_Y = 9;
   private static final int SLOT_PITCH = 18;
   private static final int MAX_INPUT_SLOTS = 12;
   private static final int OUTPUT_X = 105;
   private static final int OUTPUT_Y = 9;
   private static final int POWER_X = 4;
   private static final int POWER_Y = 88;
   private static final int POWER_COLOR = -12566464;
   private static final int WIDTH = 129;
   private static final int HEIGHT = 98;
   private final IDrawable background;

   public AssemblyTableCategory(IGuiHelper guiHelper) {
      super(
         AssemblyRecipeJeiTypes.ASSEMBLY,
         Component.translatable("gui.jei.category.buildcraft.assembly_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.ASSEMBLY_TABLE),
         129,
         98
      );
      this.background = guiHelper.createDrawable(Identifier.parse("buildcraftsilicon:textures/gui/assembly_table.png"), 3, 27, 89, 86);
   }

   public void draw(AssemblyRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      double mj = (double)recipe.microJoules() / MjAPI.MJ;
      String powerStr = Component.translatable("gui.jei.category.buildcraft.assembly_table.power", new Object[]{LocaleUtil.localizeMj(recipe.microJoules())})
         .getString();
      Font font = Minecraft.getInstance().font;
      new BCGraphics(graphics).text(font, powerStr, 4, 88, -12566464, false);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, AssemblyRecipeJei recipe, IFocusGroup focuses) {
      List<List<ItemStack>> inputs = recipe.inputSlots();
      int inputCount = Math.min(inputs.size(), 12);
      IRecipeSlotBuilder[] inputSlotBuilders = new IRecipeSlotBuilder[inputCount];

      for (int i = 0; i < inputCount; i++) {
         List<ItemStack> slot = inputs.get(i);
         if (!slot.isEmpty()) {
            int col = i % 3;
            int row = i / 3;
            int x = 5 + col * 18;
            int y = 9 + row * 18;
            inputSlotBuilders[i] = (IRecipeSlotBuilder)builder.addInputSlot(x, y).addItemStacks(slot);
         }
      }

      IRecipeSlotBuilder outputSlotBuilder = null;
      if (!recipe.outputs().isEmpty()) {
         outputSlotBuilder = (IRecipeSlotBuilder)builder.addOutputSlot(105, 9).setOutputSlotBackground().addItemStacks(recipe.outputs());
      }

      int linkIdx = recipe.focusLinkInputIndex();
      if (linkIdx >= 0 && linkIdx < inputCount && inputSlotBuilders[linkIdx] != null && outputSlotBuilder != null) {
         builder.createFocusLink(new IIngredientAcceptor[]{inputSlotBuilders[linkIdx], outputSlotBuilder});
      }
   }
}
