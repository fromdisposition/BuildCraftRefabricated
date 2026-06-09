/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
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

public class IntegrationTableCategory extends AbstractRecipeCategory<IntegrationRecipeJei> {
   private static final int[][] RING_POS = new int[][]{{19, 24}, {44, 24}, {69, 24}, {19, 49}, {69, 49}, {19, 74}, {44, 74}, {69, 74}};
   private static final int CENTER_X = 44;
   private static final int CENTER_Y = 49;
   private static final int OUTPUT_X = 138;
   private static final int OUTPUT_Y = 49;
   private static final int POWER_X = 4;
   private static final int POWER_Y = 78;
   private static final int WIDTH = 154;
   private static final int HEIGHT = 90;
   private final IDrawable background;

   public IntegrationTableCategory(IGuiHelper guiHelper) {
      super(
         BCJeiRecipeTypes.INTEGRATION,
         Component.translatable("gui.jei.category.buildcraft.integration_table"),
         guiHelper.createDrawableItemLike(BCSiliconItems.INTEGRATION_TABLE),
         WIDTH,
         HEIGHT
      );
      this.background = guiHelper.createDrawable(Identifier.parse("buildcraftsilicon:textures/gui/integration_table.png"), 0, 0, WIDTH, HEIGHT);
   }

   public void draw(IntegrationRecipeJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      this.background.draw(graphics);
      String powerStr = Component.translatable(
            "gui.jei.category.buildcraft.integration_table.power", new Object[]{LocaleUtil.localizeMj(recipe.microJoules())}
         )
         .getString();
      Font font = Minecraft.getInstance().font;
      new BCGraphics(graphics).text(font, powerStr, POWER_X, POWER_Y, -12566464, false);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, IntegrationRecipeJei recipe, IFocusGroup focuses) {
      IRecipeSlotBuilder centerBuilder = (IRecipeSlotBuilder)builder.addInputSlot(CENTER_X, CENTER_Y).addItemStacks(List.of(recipe.center()));
      IRecipeSlotBuilder outputBuilder = (IRecipeSlotBuilder)builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).setOutputSlotBackground().addItemStacks(List.of(recipe.output()));
      List<ItemStack> ring = recipe.ring();

      for (int i = 0; i < RING_POS.length; i++) {
         if (i < ring.size() && !ring.get(i).isEmpty()) {
            builder.addInputSlot(RING_POS[i][0], RING_POS[i][1]).addItemStacks(List.of(ring.get(i)));
         }
      }

      builder.createFocusLink(new IIngredientAcceptor[]{centerBuilder, outputBuilder});
   }
}
