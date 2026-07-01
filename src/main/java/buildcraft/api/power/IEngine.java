/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.power;

import net.minecraft.core.Direction;

public interface IEngine {
   boolean canReceiveFromEngine(Direction var1);

   boolean receivePower(long var1, boolean var3);
}
