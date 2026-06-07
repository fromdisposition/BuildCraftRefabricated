package buildcraft.robotics.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

/** Matches any item equal (item + components) to one of the reference stacks. An empty reference set matches nothing. */
public class ArrayStackFilter implements IStackFilter {
   protected final ItemStack[] stacks;

   public ArrayStackFilter(ItemStack... stacks) {
      this.stacks = stacks;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      for (ItemStack reference : this.stacks) {
         if (reference != null && !reference.isEmpty() && ItemStack.isSameItemSameComponents(reference, stack)) {
            return true;
         }
      }

      return false;
   }
}
