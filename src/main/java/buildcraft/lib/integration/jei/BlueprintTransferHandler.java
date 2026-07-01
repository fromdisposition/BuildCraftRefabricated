/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.integration.jei;

import buildcraft.lib.gui.IBcMenu;
import buildcraft.lib.misc.RegistryKeyUtil;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class BlueprintTransferHandler<C extends AbstractContainerMenu> implements IRecipeTransferHandler<C, RecipeHolder<CraftingRecipe>> {
   public static final int NET_JEI_RECIPE_TRANSFER = 100;
   private final Class<? extends C> containerClass;
   private final MenuType<C> menuType;

   public BlueprintTransferHandler(Class<? extends C> containerClass, MenuType<C> menuType) {
      this.containerClass = containerClass;
      this.menuType = menuType;
   }

   public Class<? extends C> getContainerClass() {
      return this.containerClass;
   }

   public Optional<MenuType<C>> getMenuType() {
      return Optional.of(this.menuType);
   }

   public mezz.jei.api.recipe.types.IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
      return RecipeTypes.CRAFTING;
   }

   @Nullable
   public IRecipeTransferError transferRecipe(
      C container, RecipeHolder<CraftingRecipe> recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer
   ) {
      if (!doTransfer) {
         return null;
      }

      if (container instanceof IBcMenu bcContainer) {
         String recipeIdStr = RegistryKeyUtil.id(recipe.id()).toString();
         bcContainer.sendMessage(100, buf -> buf.writeUtf(recipeIdStr));
      }

      return null;
   }
}
