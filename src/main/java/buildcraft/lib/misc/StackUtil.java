/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import buildcraft.api.items.IList;
import buildcraft.api.recipes.IngredientStack;

public class StackUtil {

    @Nonnull
    public static final ItemStack EMPTY;

    static {
        ItemStack stack = ItemStack.EMPTY;
        if (stack == null) throw new NullPointerException("Empty ItemStack was null!");
        EMPTY = stack;
    }

    public static boolean canMerge(@Nonnull ItemStack a, @Nonnull ItemStack b) {
        if (!ItemStack.isSameItem(a, b)) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(a, b);
    }

    public static boolean contains(@Nonnull ItemStack required, @Nonnull ItemStack container) {
        if (canMerge(required, container)) {
            return container.getCount() >= required.getCount();
        }
        return false;
    }

    public static boolean contains(@Nonnull ItemStack required, Collection<ItemStack> containers) {
        for (ItemStack possible : containers) {
            if (possible == null) {
                throw new NullPointerException("Found a null itemstack in " + containers);
            }
            if (contains(required, possible)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(@Nonnull IngredientStack ingredientStack, @Nonnull ItemStack stack) {
        return !stack.isEmpty()
                && ingredientStack.ingredient.test(stack)
                && stack.getCount() >= ingredientStack.count;
    }

    public static boolean contains(@Nonnull IngredientStack ingredientStack, @Nonnull NonNullList<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (contains(ingredientStack, stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAll(Collection<ItemStack> required, Collection<ItemStack> containers) {
        for (ItemStack req : required) {
            if (req == null) {
                throw new NullPointerException("Found a null itemstack in " + containers);
            }
            if (req.isEmpty()) continue;
            if (!contains(req, containers)) {
                return false;
            }
        }
        return true;
    }

    public static int mergeStacks(@Nonnull ItemStack mergeSource, @Nonnull ItemStack mergeTarget, boolean doMerge) {
        if (!canMerge(mergeSource, mergeTarget)) {
            return 0;
        }
        int mergeCount = Math.min(mergeTarget.getMaxStackSize() - mergeTarget.getCount(), mergeSource.getCount());
        if (mergeCount < 1) {
            return 0;
        }
        if (doMerge) {
            mergeTarget.setCount(mergeTarget.getCount() + mergeCount);
        }
        return mergeCount;
    }

    public static boolean isMatchingItem(@Nonnull ItemStack base, @Nonnull ItemStack comparison) {
        if (base.isEmpty() || comparison.isEmpty()) return false;
        return ItemStack.isSameItemSameComponents(base, comparison);
    }

    public static boolean isMatchingItemOrList(@Nonnull ItemStack filter, @Nonnull ItemStack toTest) {
        if (filter.isEmpty() || toTest.isEmpty()) return false;
        if (filter.getItem() instanceof IList list) {
            return list.matches(filter, toTest);
        }
        return isMatchingItem(filter, toTest);
    }

    public static boolean matchesStackOrList(@Nonnull ItemStack filter, @Nonnull ItemStack toTest) {
        return isMatchingItemOrList(filter, toTest);
    }

    public static NonNullList<ItemStack> listOf() {
        return NonNullList.withSize(0, EMPTY);
    }

    public static NonNullList<ItemStack> listOf(ItemStack... stacks) {
        switch (stacks.length) {
            case 0:
                return listOf();
            case 1:
                return NonNullList.withSize(1, stacks[0]);
            default:
        }
        NonNullList<ItemStack> list = NonNullList.withSize(stacks.length, EMPTY);
        for (int i = 0; i < stacks.length; i++) {
            list.set(i, stacks[i]);
        }
        return list;
    }

    @Nonnull
    public static <T> T asNonNull(@Nullable T obj) {
        if (obj == null) {
            throw new NullPointerException("Object was null!");
        }
        return obj;
    }

    @Nonnull
    public static <T> T asNonNullSoft(@Nullable T obj, @Nonnull T fallback) {
        if (obj == null) {
            return fallback;
        } else {
            return obj;
        }
    }

    @Nonnull
    public static ItemStack asNonNullSoft(@Nullable ItemStack stack) {
        return asNonNullSoft(stack, EMPTY);
    }

    public static <E> Collector<E, ?, NonNullList<E>> nonNullListCollector() {
        return Collectors.toCollection(NonNullList::create);
    }

    public static int hash(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return Objects.hash(stack.getItem(), stack.getComponents());
    }

    public static NonNullList<ItemStack> mergeSameItems(java.util.List<ItemStack> items) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        for (ItemStack toAdd : items) {
            boolean found = false;
            for (ItemStack stack : stacks) {
                if (canMerge(stack, toAdd)) {
                    stack.grow(toAdd.getCount());
                    found = true;
                }
            }
            if (!found) {
                stacks.add(toAdd.copy());
            }
        }
        return stacks;
    }
}
