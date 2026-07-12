/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.fabric.BCRegistries;
import buildcraft.transport.recipe.PipeColourRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BCTransportRecipeSerializers {
   private BCTransportRecipeSerializers() {
   }

   public static void register() {
      Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BCRegistries.id("buildcrafttransport", "pipe_colour"), PipeColourRecipe.SERIALIZER);
   }
}
