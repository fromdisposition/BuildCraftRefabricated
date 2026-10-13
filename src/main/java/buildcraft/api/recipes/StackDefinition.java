/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.recipes;

import buildcraft.api.core.IStackFilter;

public final class StackDefinition {
   public final IStackFilter filter;
   public final int count;

   public StackDefinition(IStackFilter filter, int count) {
      this.filter = filter;
      this.count = count;
   }

   public StackDefinition(IStackFilter filter) {
      this(filter, 1);
   }
}
