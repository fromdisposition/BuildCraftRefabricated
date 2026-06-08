/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.integration.jei;

import buildcraft.api.recipes.IRefineryRecipeManager;
import mezz.jei.api.recipe.types.IRecipeType;

public final class DistillerRecipeTypes {
   public static final IRecipeType<IRefineryRecipeManager.IDistillationRecipe> DISTILLER = IRecipeType.create(
      "buildcraftfactory", "distiller", IRefineryRecipeManager.IDistillationRecipe.class
   );

   private DistillerRecipeTypes() {
   }
}
