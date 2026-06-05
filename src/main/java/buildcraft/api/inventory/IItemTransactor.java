package buildcraft.api.inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import buildcraft.api.core.IStackFilter;

public interface IItemTransactor {

    @Nonnull
    ItemStack insert(@Nonnull ItemStack stack, boolean allOrNone, boolean simulate);

    default NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        NonNullList<ItemStack> leftOver = NonNullList.create();
        for (ItemStack stack : stacks) {
            ItemStack leftOverStack = insert(stack, false, simulate);
            if (!leftOverStack.isEmpty()) {
                leftOver.add(leftOverStack);
            }
        }
        return leftOver;
    }

    @Nonnull
    ItemStack extract(@Nullable IStackFilter filter, int min, int max, boolean simulate);

    default boolean canFullyAccept(@Nonnull ItemStack stack) {
        return insert(stack, true, true).isEmpty();
    }

    default boolean canPartiallyAccept(@Nonnull ItemStack stack) {
        return insert(stack, false, true).getCount() < stack.getCount();
    }

    @FunctionalInterface
    interface IItemInsertable extends IItemTransactor {
        @Nonnull
        @Override
        default ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }

    @FunctionalInterface
    interface IItemExtractable extends IItemTransactor {
        @Nonnull
        @Override
        default ItemStack insert(@Nonnull ItemStack stack, boolean allOrNone, boolean simulate) {
            return stack;
        }
    }
}
