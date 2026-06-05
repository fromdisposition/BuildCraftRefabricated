/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import buildcraft.lib.transfer.ResourceHandler;

public final class CarriedSlotWrapper extends ItemStackResourceHandler {

    private static final Map<AbstractContainerMenu, CarriedSlotWrapper> wrappers = new MapMaker().weakKeys().weakValues().makeMap();

    public static ResourceHandler<ItemResource> of(AbstractContainerMenu menu) {
        return wrappers.computeIfAbsent(menu, CarriedSlotWrapper::new);
    }

    private final AbstractContainerMenu menu;

    private CarriedSlotWrapper(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    @Override
    protected ItemStack getStack() {
        return menu.getCarried();
    }

    @Override
    protected void setStack(ItemStack stack) {
        menu.setCarried(stack);
    }

    @Override
    public String toString() {
        return "CarriedSlotWrapper[" + menu + "/" + BuiltInRegistries.MENU.getId(menu.getType()) + "]";
    }
}
