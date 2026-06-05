/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import buildcraft.lib.transfer.resource.Resource;
import buildcraft.lib.transfer.resource.ResourceStack;

public abstract class ResourceStacksResourceHandler<R extends Resource> extends StacksResourceHandler<ResourceStack<R>, R> {

    public ResourceStacksResourceHandler(int size, R emptyResource, Codec<ResourceStack<R>> stackCodec) {
        super(size, new ResourceStack<>(emptyResource, 0), stackCodec);
    }

    public ResourceStacksResourceHandler(NonNullList<ResourceStack<R>> stacks, R emptyResource, Codec<ResourceStack<R>> stackCodec) {
        super(stacks, new ResourceStack<>(emptyResource, 0), stackCodec);
    }

    @Override
    public R getResourceFrom(ResourceStack<R> stack) {
        return stack.resource();
    }

    @Override
    public int getAmountFrom(ResourceStack<R> stack) {
        return stack.amount();
    }

    @Override
    protected ResourceStack<R> getStackFrom(R resource, int amount) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) {
            return emptyStack;
        }
        return new ResourceStack<>(resource, amount);
    }

    @Override
    protected ResourceStack<R> copyOf(ResourceStack<R> stack) {
        return stack;
    }

    @Override
    public boolean matches(ResourceStack<R> stack, R resource) {
        return stack.resource().equals(resource);
    }
}
