package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.tile.BcItemInventory;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class DelegatingItemHandlerFilter implements IStackFilter {
   private final ISingleStackFilter perStackFilter;
   private final BcItemInventory handler;

   public DelegatingItemHandlerFilter(ISingleStackFilter perStackFilter, BcItemInventory handler) {
      this.perStackFilter = perStackFilter;
      this.handler = handler;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      for (int slot = 0; slot < this.handler.getSlots(); slot++) {
         ItemStack slotStack = this.handler.getStackInSlot(slot);
         if (!slotStack.isEmpty() && this.perStackFilter.matches(slotStack, stack)) {
            return true;
         }
      }

      return false;
   }
}
