package buildcraft.lib.gui.slot;

import buildcraft.lib.tile.ItemHandlerSimple;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class SlotOutput extends SlotBase {
   public SlotOutput(ItemHandlerSimple handler, int slotIndex, int posX, int posY) {
      super(handler, slotIndex, posX, posY);
   }

   @Override
   public boolean mayPlace(@Nonnull ItemStack itemstack) {
      return false;
   }
}
