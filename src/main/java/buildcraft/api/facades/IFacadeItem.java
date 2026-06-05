/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.facades;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

public interface IFacadeItem {

    @Nullable
    default FacadeType getFacadeType(@Nonnull ItemStack stack) {
        IFacade facade = getFacade(stack);
        if (facade == null) {
            return null;
        }
        return facade.getType();
    }

    @Nonnull
    ItemStack getFacadeForBlock(BlockState state);

    ItemStack createFacadeStack(IFacade facade);

    @Nullable
    IFacade getFacade(@Nonnull ItemStack facade);
}
