package buildcraft.api.inventory;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IItemTransactor {
   @Nonnull
   ItemStack insert(@Nonnull ItemStack var1, boolean var2, boolean var3);

   default NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
      NonNullList<ItemStack> leftOver = NonNullList.create();

      for (ItemStack stack : stacks) {
         ItemStack leftOverStack = this.insert(stack, false, simulate);
         if (!leftOverStack.isEmpty()) {
            leftOver.add(leftOverStack);
         }
      }

      return leftOver;
   }

   @Nonnull
   ItemStack extract(@Nullable IStackFilter var1, int var2, int var3, boolean var4);

   default boolean canFullyAccept(@Nonnull ItemStack stack) {
      return this.insert(stack, true, true).isEmpty();
   }

   default boolean canPartiallyAccept(@Nonnull ItemStack stack) {
      return this.insert(stack, false, true).getCount() < stack.getCount();
   }

   @FunctionalInterface
   interface IItemExtractable extends IItemTransactor {
      @Nonnull
      @Override
      default ItemStack insert(@Nonnull ItemStack stack, boolean allOrNone, boolean simulate) {
         return stack;
      }
   }

   @FunctionalInterface
   interface IItemInsertable extends IItemTransactor {
      @Nonnull
      @Override
      default ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
         return ItemStack.EMPTY;
      }
   }
}
