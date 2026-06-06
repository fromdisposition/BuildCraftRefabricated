package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.misc.StackUtil;
import javax.annotation.Nonnull;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class InventoryWrapper extends AbstractInvItemTransactor {
   private final Container inventory;

   public InventoryWrapper(Container inventory) {
      this.inventory = inventory;
   }

   @Nonnull
   @Override
   protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
      ItemStack current = this.inventory.getItem(slot);
      if (!this.inventory.canPlaceItem(slot, stack)) {
         return stack;
      }

      if (current.isEmpty()) {
         int max = Math.min(this.inventory.getMaxStackSize(), stack.getMaxStackSize());
         ItemStack split = stack.split(max);
         if (!simulate) {
            this.inventory.setItem(slot, split);
         }

         return stack.isEmpty() ? StackUtil.EMPTY : stack;
      } else if (StackUtil.canMerge(current, stack)) {
         ItemStack merged = current.copy();
         merged.setCount(merged.getCount() + stack.getCount());
         int size = Math.min(this.inventory.getMaxStackSize(), merged.getMaxStackSize());
         if (merged.getCount() > size) {
            stack.setCount(stack.getCount() - (merged.getCount() - size));
            merged.setCount(size);
            if (!simulate) {
               this.inventory.setItem(slot, merged);
            }

            return stack;
         } else {
            if (!simulate) {
               this.inventory.setItem(slot, merged);
            }

            return StackUtil.EMPTY;
         }
      } else {
         return stack;
      }
   }

   @Nonnull
   @Override
   protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
      ItemStack current = this.inventory.getItem(slot);
      if (current.isEmpty()) {
         return StackUtil.EMPTY;
      }

      if (filter.matches(current.copy())) {
         if (current.getCount() < min) {
            return StackUtil.EMPTY;
         }

         int size = Math.min(current.getCount(), max);
         current = current.copy();
         ItemStack other = current.split(size);
         if (!simulate) {
            if (current.getCount() <= 0) {
               current = StackUtil.EMPTY;
            }

            this.inventory.setItem(slot, current);
         }

         return other;
      } else {
         return StackUtil.EMPTY;
      }
   }

   @Override
   protected int size() {
      return this.inventory.getContainerSize();
   }

   @Override
   protected boolean isEmpty(int slot) {
      return this.inventory.getItem(slot).isEmpty();
   }
}
