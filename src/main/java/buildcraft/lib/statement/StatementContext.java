/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.statement;

import buildcraft.api.statements.IGuiSlot;
import buildcraft.lib.gui.ISimpleDrawable;
import java.util.List;
import javax.annotation.Nullable;

public interface StatementContext<S extends IGuiSlot> {
   List<? extends StatementContext.StatementGroup<S>> getAllPossible();

   interface StatementGroup<S extends IGuiSlot> {
      List<S> getValues();

      @Nullable
      ISimpleDrawable getSourceIcon();

      default int getLedgerColour() {
         return 0;
      }
   }
}
