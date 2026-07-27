/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

/**
 * 1.21.1 override (versions/1.21.1). On 1.21.1 the vanilla {@link RecipeBookComponent} is a CONCRETE class
 * that implements the whole widget itself and is driven by the RecipeBookMenu container, so this is a thin
 * no-arg alias with no overrides. The shared src/main version (compiled on 1.21.10+/26.x) subclasses the
 * abstract generic {@code RecipeBookComponent<M>} and supplies the ghost-recipe hooks by hand.
 */
public class AWRecipeBookComponent extends RecipeBookComponent {
}
