/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.energy.recipe.CombustionFuelRecipe;
import buildcraft.energy.recipe.CoolantRecipe;
import buildcraft.energy.recipe.SolidCoolantRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;

public final class BCEnergyRecipeTypes {
   public static final RecipeType<CombustionFuelRecipe> COMBUSTION_FUEL = register("combustion_fuel");
   public static final RecipeType<CoolantRecipe> COOLANT = register("coolant");
   public static final RecipeType<SolidCoolantRecipe> SOLID_COOLANT = register("solid_coolant");

   private BCEnergyRecipeTypes() {
   }

   private static <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeType<T> register(String name) {
      String full = "buildcraftenergy:" + name;
      return Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.parse(full), new RecipeType<T>() {
         @Override
         public String toString() {
            return full;
         }
      });
   }

   public static void register() {
      // Fields are initialized by class loading; this method triggers that.
   }
}
