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

public class IntegrationTableCategory extends AbstractRecipeCategory<IntegrationRecipeJei> {
   private static final Identifier TEX = Identifier.parse("buildcraftsilicon:textures/gui/bcr/integration_table.png");
   private static final int BG_U = 4, BG_V = 14, BG_W = 152, BG_H = 74;
   private static final int CENTER_X = 30, CENTER_Y = 31, OUTPUT_X = 129, OUTPUT_Y = 31;
   private static final int POWER_ALIGN_W = OUTPUT_X + 18;
   private static final int POWER_Y = BG_H + 5;
   private static final int CARD_H = BG_H + 16;
   private static final int[][] RING = {{5, 6}, {30, 6}, {55, 6}, {5, 31}, {55, 31}, {5, 56}, {30, 56}, {55, 56}};
   private final IDrawable background;

   public IntegrationTableCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.INTEGRATION,
         Component.translatable("gui.jei.category.buildcraft.integration_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.INTEGRATION_TABLE),
         BG_W,
         CARD_H
      );
      this.background = guiHelper.createDrawable(TEX, BG_U, BG_V, BG_W, BG_H);
   }

   @Override
   public void draw(IntegrationRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      String mj = LocaleUtil.localizeMj(recipe.microJoules());
      if (!mj.isEmpty()) {
         JeiCategoryDraw.textRight(graphics, LocaleUtil.localize("gui.jei.category.buildcraft.integration_table.power", mj), POWER_ALIGN_W, POWER_Y);
      }
   }

   @Override
   public void setRecipe(IRecipeLayoutBuilder builder, IntegrationRecipeJei recipe, IFocusGroup focuses) {
      IRecipeSlotBuilder centerBuilder = builder.addInputSlot(CENTER_X, CENTER_Y).addItemStacks(List.of(recipe.center()));
      IRecipeSlotBuilder outputBuilder = builder.addInputSlot(OUTPUT_X, OUTPUT_Y).addItemStacks(List.of(recipe.output()));
      List<ItemStack> ring = recipe.ring();

      for (int i = 0; i < RING.length; i++) {
         if (i < ring.size() && !ring.get(i).isEmpty()) {
            builder.addInputSlot(RING[i][0], RING[i][1]).addItemStacks(List.of(ring.get(i)));
         }
      }

      builder.createFocusLink(centerBuilder, outputBuilder);
   }
}
