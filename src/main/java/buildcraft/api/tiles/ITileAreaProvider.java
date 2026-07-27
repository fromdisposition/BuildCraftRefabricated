/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.tiles;

import buildcraft.api.core.IAreaProvider;
import net.minecraft.core.BlockPos;

public interface ITileAreaProvider extends IAreaProvider {
   boolean isValidFromLocation(BlockPos var1);
}
