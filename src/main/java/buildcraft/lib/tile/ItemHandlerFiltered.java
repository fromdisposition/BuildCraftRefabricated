package buildcraft.lib.tile;

import buildcraft.lib.misc.StackUtil;
import net.minecraft.world.item.ItemStack;

public class ItemHandlerFiltered extends ItemHandlerSimple {
   private final ItemHandlerSimple filter;
   private final boolean emptyIsAnything;

   public ItemHandlerFiltered(ItemHandlerSimple filter, boolean emptyIsAnything) {
      super(filter.size());
      this.emptyIsAnything = emptyIsAnything;
      this.filter = filter;
      this.setChecker((slot, stack) -> {
         ItemStack inSlot = filter.getStackInSlot(slot);
         return inSlot.isEmpty() ? emptyIsAnything : StackUtil.canMerge(stack, inSlot);
      });
   }

   @Override
   public int getSlotLimit(int slot) {
      return !this.emptyIsAnything && this.getFilter(slot).isEmpty() ? 0 : super.getSlotLimit(slot);
   }

   public ItemStack getFilter(int slot) {
      ItemStack current = this.getStackInSlot(slot);
      return !current.isEmpty() ? current : this.filter.getStackInSlot(slot);
   }
}
