package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class DelegatingArrayFilter implements IStackFilter {
   private final ISingleStackFilter perStackFilter;
   private final NonNullList<ItemStack> stacks;

   public DelegatingArrayFilter(ISingleStackFilter perStackFilter, NonNullList<ItemStack> stacks) {
      this.perStackFilter = perStackFilter;
      this.stacks = stacks;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      for (ItemStack possible : this.stacks) {
         if (this.perStackFilter.matches(possible, stack)) {
            return true;
         }
      }

      return false;
   }
}
