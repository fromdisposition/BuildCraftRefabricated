/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.energy.recipe.CombustionFuelRecipe;
import buildcraft.energy.recipe.CoolantRecipe;
import buildcraft.energy.recipe.SolidCoolantRecipe;
import buildcraft.fabric.BCRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BCEnergyRecipeSerializers {
   private BCEnergyRecipeSerializers() {
   }

   public static void register() {
      Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BCRegistries.id("buildcraftenergy", "combustion_fuel"), CombustionFuelRecipe.SERIALIZER);
      Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BCRegistries.id("buildcraftenergy", "coolant"), CoolantRecipe.SERIALIZER);
      Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BCRegistries.id("buildcraftenergy", "solid_coolant"), SolidCoolantRecipe.SERIALIZER);
   }
}
