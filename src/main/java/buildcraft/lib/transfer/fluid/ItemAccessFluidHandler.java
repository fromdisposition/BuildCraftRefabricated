/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.fluid;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import buildcraft.lib.fluids.SimpleFluidContent;
import buildcraft.lib.transfer.ItemAccessResourceHandler;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class ItemAccessFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    protected final Item validItem;
    protected final DataComponentType<SimpleFluidContent> component;
    protected int capacity;

    public ItemAccessFluidHandler(ItemAccess itemAccess, DataComponentType<SimpleFluidContent> component, int capacity) {
        super(itemAccess, 1);

        this.validItem = itemAccess.getResource().getItem();
        this.component = component;
        this.capacity = capacity;
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return FluidResource.of(accessResource.getOrDefault(component, SimpleFluidContent.EMPTY).copy());
        } else {
            return FluidResource.EMPTY;
        }
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return accessResource.getOrDefault(component, SimpleFluidContent.EMPTY).getAmount();
        } else {
            return 0;
        }
    }

    @Override
    @Nullable
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        return accessResource.with(component, SimpleFluidContent.copyOf(newResource.toStack(newAmount)));
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {

        return itemAccess.getResource().is(validItem);
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return capacity;
    }
}
