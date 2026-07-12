/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.statements;

public interface IStatement extends IGuiSlot {
   int maxParameters();

   int minParameters();

   IStatementParameter createParameter(int var1);

   default IStatementParameter createParameter(IStatementParameter old, int index) {
      IStatementParameter _new = this.createParameter(index);
      if (old != null && _new != null) {
         return old.getClass() == _new.getClass() ? old : _new;
      } else {
         return _new;
      }
   }

   IStatement rotateLeft();

   IStatement[] getPossible();

   default boolean isPossibleOrdered() {
      return false;
   }
}
