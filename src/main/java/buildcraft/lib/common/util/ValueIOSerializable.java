/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.common.util;

import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;

public interface ValueIOSerializable {
   void serialize(BcValueOut var1);

   void deserialize(BcValueIn var1);
}
