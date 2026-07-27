/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record IntegrationRecipeJei(String id, ItemStack center, List<ItemStack> ring, ItemStack output, long microJoules) {
}
