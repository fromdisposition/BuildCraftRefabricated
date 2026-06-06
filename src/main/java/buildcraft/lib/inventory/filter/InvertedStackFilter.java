package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class InvertedStackFilter implements IStackFilter {
   private final IStackFilter filter;

   public InvertedStackFilter(IStackFilter filter) {
      this.filter = filter;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      return !stack.isEmpty() && !this.filter.matches(stack);
   }
}
