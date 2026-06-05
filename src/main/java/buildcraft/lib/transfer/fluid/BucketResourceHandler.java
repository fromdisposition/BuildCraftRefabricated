/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.fluid;

import java.util.Objects;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import buildcraft.lib.fluids.FluidConstants;
import buildcraft.lib.transfer.ItemAccessResourceHandler;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.item.ItemResource;

public final class BucketResourceHandler extends ItemAccessResourceHandler<FluidResource> {
    public BucketResourceHandler(ItemAccess itemAccess) {
        super(itemAccess, 1);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        if (accessResource.getItem() instanceof BucketItem bucketItem) {
            return FluidResource.of(buildcraft.lib.fabric.Mc26Compat.bucketFluid(bucketItem));
        } else {
            return FluidResource.EMPTY;
        }
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        var resource = getResourceFrom(accessResource, index);
        return resource.isEmpty() ? 0 : FluidConstants.BUCKET_VOLUME;
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        if (newAmount == 0) {
            return ItemResource.of(Items.BUCKET);
        } else if (newAmount != FluidConstants.BUCKET_VOLUME) {
            return ItemResource.EMPTY;
        } else {
            return ItemResource.of(buildcraft.lib.fabric.Mc26Compat.fluidBucketItem(newResource.getFluid()));
        }
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return FluidConstants.BUCKET_VOLUME;
    }
}
