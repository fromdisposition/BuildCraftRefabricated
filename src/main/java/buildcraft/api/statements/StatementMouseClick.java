/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.statements;

public final class StatementMouseClick {
   private int button;
   private boolean shift;

   public StatementMouseClick(int button, boolean shift) {
      this.button = button;
      this.shift = shift;
   }

   public boolean isShift() {
      return this.shift;
   }

   public int getButton() {
      return this.button;
   }
}
