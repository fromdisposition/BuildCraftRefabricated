/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Recipes the server sent to this client. Since 1.21.2 vanilla only sends recipe book data, so a type is only visible
 * here when its serializer was passed to Fabric's {@code RecipeSynchronization}; on 1.21.1 vanilla still sends every
 * recipe. Works the same in singleplayer and on a dedicated server.
 */
public final class ClientRecipes {
   private ClientRecipes() {
   }

   public static <I extends RecipeInput, T extends Recipe<I>> List<T> ofType(RecipeType<T> type) {
      List<T> out = new ArrayList<>();
      //? if >= 1.21.10 {
      var connection = Minecraft.getInstance().getConnection();
      if (connection == null) {
         return out;
      }
      //? if >= 26.1 {
      var synced = ((net.fabricmc.fabric.api.recipe.v1.FabricRecipeAccess)connection.recipes()).getSynchronizedRecipes();
      //?} else {
      /*var synced = ((net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager)connection.recipes()).getSynchronizedRecipes();
      *///?}
      for (RecipeHolder<T> holder : synced.getAllOfType(type)) {
         out.add(holder.value());
      }
      //?} else {
      /*var level = Minecraft.getInstance().level;
      if (level == null) {
         return out;
      }
      for (RecipeHolder<T> holder : level.getRecipeManager().getAllRecipesFor(type)) {
         out.add(holder.value());
      }
      *///?}
      return out;
   }
}
