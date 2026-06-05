/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.item.ItemResource;

public final class TransferConvert {
    private TransferConvert() {}

    public static final long DROPLETS_PER_MB = 81L;

    public static FluidVariant toVariant(FluidResource resource) {
        return FluidVariant.of(resource.getFluid(), resource.getComponentsPatch());
    }

    public static FluidResource toFluidResource(FluidVariant variant) {
        if (variant.isBlank()) {
            return FluidResource.EMPTY;
        }
        return FluidResource.of(variant.getFluid(), variant.getComponentsPatch());
    }

    public static ItemVariant toVariant(ItemResource resource) {
        return ItemVariant.of(resource.getItem(), resource.getComponentsPatch());
    }

    public static ItemResource toItemResource(ItemVariant variant) {
        if (variant.isBlank()) {
            return ItemResource.EMPTY;
        }
        return ItemResource.of(variant.getItem(), variant.getComponentsPatch());
    }

    public static long dropletsToMb(long droplets) {
        return droplets / DROPLETS_PER_MB;
    }

    public static long mbToDroplets(long millibuckets) {
        return millibuckets * DROPLETS_PER_MB;
    }
}
