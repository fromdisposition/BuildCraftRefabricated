/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.tile.ItemHandlerSimple;

public class SlotPackager extends SlotPhantom {
   public SlotPackager(ItemHandlerSimple itemHandler, int slotIndex, int posX, int posY) {
      super(itemHandler, slotIndex, posX, posY, false);
   }

   @Override
   public boolean canShift() {
      return false;
   }
}
