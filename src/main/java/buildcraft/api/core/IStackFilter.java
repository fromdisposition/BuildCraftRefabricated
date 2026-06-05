/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.api.core;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

public interface IStackFilter {

    boolean matches(@Nonnull ItemStack stack);

    default IStackFilter and(IStackFilter filter) {
        IStackFilter before = this;
        return (stack) -> before.matches(stack) && filter.matches(stack);
    }

    default NonNullList<ItemStack> getExamples() {
        return NonNullList.withSize(0, ItemStack.EMPTY);
    }
}
