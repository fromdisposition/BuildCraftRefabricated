package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class PassThroughStackFilter implements IStackFilter {
   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      return !stack.isEmpty() && stack.getCount() > 0;
   }
}
