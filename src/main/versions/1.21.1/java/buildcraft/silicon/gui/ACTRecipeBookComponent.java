/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

/**
 * 1.21.1 override (versions/1.21.1). On 1.21.1 the vanilla {@link RecipeBookComponent} is a CONCRETE class,
 * so this is a thin no-arg alias with no overrides (the shared src/main version subclasses the abstract
 * generic {@code RecipeBookComponent<M>}).
 */
public class ACTRecipeBookComponent extends RecipeBookComponent {
}
