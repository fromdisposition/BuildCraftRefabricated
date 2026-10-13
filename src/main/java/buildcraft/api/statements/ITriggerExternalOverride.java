/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.statements;

import net.minecraft.core.Direction;

public interface ITriggerExternalOverride {
   ITriggerExternalOverride.Result override(Direction var1, IStatementContainer var2, ITriggerExternal var3, IStatementParameter[] var4);

   enum Result {
      TRUE,
      FALSE,
      IGNORE;
   }
}
