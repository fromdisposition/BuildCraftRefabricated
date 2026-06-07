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
