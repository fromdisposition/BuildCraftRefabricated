package buildcraft.api.core;

import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IStackFilter {
   boolean matches(@Nonnull ItemStack var1);

   default IStackFilter and(IStackFilter filter) {
      IStackFilter before = this;
      return stack -> before.matches(stack) && filter.matches(stack);
   }

   default NonNullList<ItemStack> getExamples() {
      return NonNullList.withSize(0, ItemStack.EMPTY);
   }
}
