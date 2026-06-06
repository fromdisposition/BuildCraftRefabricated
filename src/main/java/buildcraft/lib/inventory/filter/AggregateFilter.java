package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class AggregateFilter implements IStackFilter {
   private final IStackFilter[] filters;

   public AggregateFilter(IStackFilter... iFilters) {
      this.filters = iFilters;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      for (IStackFilter f : this.filters) {
         if (!f.matches(stack)) {
            return false;
         }
      }

      return true;
   }
}
