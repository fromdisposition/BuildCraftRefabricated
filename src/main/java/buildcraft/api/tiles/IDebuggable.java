/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.tiles;

import java.util.List;
import net.minecraft.core.Direction;

public interface IDebuggable {
   void getDebugInfo(List<String> var1, List<String> var2, Direction var3);

   default void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
   }
}
