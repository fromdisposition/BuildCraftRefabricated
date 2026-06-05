/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import buildcraft.lib.attachments.IAttachmentProvider;
import buildcraft.lib.attachments.RegisterAttachmentsEvent;
import buildcraft.lib.transfer.ItemAccessResourceHandler;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

public class ItemAccessItemHandler extends ItemAccessResourceHandler<ItemResource> {
    protected final Item validItem;
    protected final DataComponentType<ItemContainerContents> component;

    public ItemAccessItemHandler(ItemAccess itemAccess, DataComponentType<ItemContainerContents> component, int size) {
        super(itemAccess, size);

        this.validItem = itemAccess.getResource().getItem();
        this.component = component;
        Preconditions.checkArgument(size <=  256,
                "The max size of ItemContainerContents is 256 slots.");
    }

    protected ItemContainerContents getContents(ItemResource accessResource) {
        return accessResource.getOrDefault(component, ItemContainerContents.EMPTY);
    }

    protected ItemStack getStackFromContents(ItemContainerContents contents, int slot) {
        NonNullList<ItemStack> list = NonNullList.create();
        contents.copyInto(list);
        return slot < list.size() ? list.get(slot) : ItemStack.EMPTY;
    }

    @Override
    protected ItemResource getResourceFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return ItemResource.of(getStackFromContents(getContents(accessResource), index));
        } else {
            return ItemResource.EMPTY;
        }
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return getStackFromContents(getContents(accessResource), index).getCount();
        } else {
            return 0;
        }
    }

    @Override
    @Nullable
    protected ItemResource update(ItemResource accessResource, int index, ItemResource newResource, int newAmount) {
        var contents = getContents(accessResource);
        NonNullList<ItemStack> list = NonNullList.create();
        contents.copyInto(list);
        while (list.size() < size) {
            list.add(ItemStack.EMPTY);
        }
        list.set(index, newResource.toStack(newAmount));
        return accessResource.with(this.component, ItemContainerContents.fromItems(list));
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {

        return itemAccess.getResource().is(validItem);
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return resource.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
    }
}
