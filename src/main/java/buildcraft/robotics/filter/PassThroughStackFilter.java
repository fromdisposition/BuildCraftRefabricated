package buildcraft.robotics.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

/** Matches any non-empty item. */
public class PassThroughStackFilter implements IStackFilter {
   public static final PassThroughStackFilter INSTANCE = new PassThroughStackFilter();

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      return !stack.isEmpty();
   }
}
