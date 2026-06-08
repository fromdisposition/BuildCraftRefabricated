/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.recipes;

import javax.annotation.Nullable;

public final class BuildcraftRecipeRegistry {
   @Nullable
   public static IIntegrationRecipeRegistry integrationRecipes;
   @Nullable
   public static IRefineryRecipeManager refineryRecipes;
   @Nullable
   public static IProgrammingRecipeRegistry programmingTable;

   private BuildcraftRecipeRegistry() {
   }
}
