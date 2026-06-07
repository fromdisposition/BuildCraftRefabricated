package buildcraft.robotics.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

/** Matches any item equal (item + components) to one of the reference stacks. An empty reference set matches anything. */
public class ArrayStackFilter implements IStackFilter {
   protected ItemStack[] stacks;

   public ArrayStackFilter(ItemStack... stacks) {
      this.stacks = stacks;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      if (this.stacks.length == 0 || !this.hasFilter()) {
         return true;
      }

      for (ItemStack reference : this.stacks) {
         if (reference != null && !reference.isEmpty() && ItemStack.isSameItemSameComponents(reference, stack)) {
            return true;
         }
      }

      return false;
   }

   /** Returns true if the given filter matches any of this filter's reference stacks. */
   public boolean matches(IStackFilter other) {
      for (ItemStack reference : this.stacks) {
         if (reference != null && !reference.isEmpty() && other.matches(reference)) {
            return true;
         }
      }

      return false;
   }

   public ItemStack[] getStacks() {
      return this.stacks;
   }

   public boolean hasFilter() {
      for (ItemStack reference : this.stacks) {
         if (reference != null && !reference.isEmpty()) {
            return true;
         }
      }

      return false;
   }
}
