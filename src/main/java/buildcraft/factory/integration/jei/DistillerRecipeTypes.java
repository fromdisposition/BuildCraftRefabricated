/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.integration.jei;

import mezz.jei.api.recipe.types.IRecipeType;

import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;

public final class DistillerRecipeTypes {
    public static final IRecipeType<IDistillationRecipe> DISTILLER = IRecipeType.create(
            "buildcraftfactory", "distiller", IDistillationRecipe.class);

    private DistillerRecipeTypes() {}
}
