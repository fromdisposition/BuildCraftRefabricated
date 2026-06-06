package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class CompositeFilter implements IStackFilter {
   private final IStackFilter[] filters;

   public CompositeFilter(IStackFilter... iFilters) {
      this.filters = iFilters;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      for (IStackFilter f : this.filters) {
         if (f.matches(stack)) {
            return true;
         }
      }

      return false;
   }
}
