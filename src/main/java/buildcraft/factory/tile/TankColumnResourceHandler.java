/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.util.List;

import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.transaction.TransactionContext;

import buildcraft.lib.misc.FluidUtilBC;

@SuppressWarnings("removal")
public class TankColumnResourceHandler implements ResourceHandler<FluidResource> {

    private final TileTank owner;

    public TankColumnResourceHandler(TileTank owner) {
        this.owner = owner;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        checkIndex(index);

        for (TileTank t : owner.getTankColumn()) {
            FluidResource fluid = t.tank.getResource(0);
            if (!fluid.isEmpty()) {
                return fluid;
            }
        }
        return FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        checkIndex(index);
        long total = 0;
        for (TileTank t : owner.getTankColumn()) {
            total += t.tank.getAmountAsLong(0);
        }
        return total;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        checkIndex(index);
        long total = 0;
        for (TileTank t : owner.getTankColumn()) {
            total += t.tank.getCapacityAsLong(0, resource);
        }
        return total;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        checkIndex(index);
        return !resource.isEmpty();
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        checkIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        resource = FluidUtilBC.canonicalFluidResource(resource);

        List<TileTank> tanks = owner.getTankColumn();
        FluidResource resolvedResource = resource;

        for (TileTank t : tanks) {
            FluidResource current = t.tank.getResource(0);
            if (current.isEmpty()) {
                continue;
            }
            if (!FluidUtilBC.areEquivalentFluidResources(current, resolvedResource)) {
                return 0;
            }

            resolvedResource = current;
        }

        boolean gaseous = FluidUtilBC.isGaseous(resolvedResource.toStack(1));

        int remaining = amount;
        int totalInserted = 0;

        if (gaseous) {
            for (int i = tanks.size() - 1; i >= 0; i--) {
                TileTank t = tanks.get(i);
                if (remaining <= 0) break;
                int accepted = t.tank.insert(0, resolvedResource, remaining, transaction);
                if (accepted > 0) {
                    remaining -= accepted;
                    totalInserted += accepted;
                }
            }
        } else {
            for (TileTank t : tanks) {
                if (remaining <= 0) break;
                int accepted = t.tank.insert(0, resolvedResource, remaining, transaction);
                if (accepted > 0) {
                    remaining -= accepted;
                    totalInserted += accepted;
                }
            }
        }

        if (totalInserted > 0) {
            owner.requestColumnBalance();
        }
        return totalInserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        checkIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        resource = FluidUtilBC.canonicalFluidResource(resource);

        List<TileTank> tanks = owner.getTankColumn();
        FluidResource resolvedResource = resource;
        for (TileTank t : tanks) {
            FluidResource current = t.tank.getResource(0);
            if (current.isEmpty()) {
                continue;
            }
            if (FluidUtilBC.areEquivalentFluidResources(current, resolvedResource)) {
                resolvedResource = current;
                break;
            }
        }

        boolean gaseous = FluidUtilBC.isGaseous(resolvedResource.toStack(1));

        int remaining = amount;
        int totalExtracted = 0;

        if (gaseous) {
            for (TileTank t : tanks) {
                if (remaining <= 0) break;
                int extracted = t.tank.extract(0, resolvedResource, remaining, transaction);
                if (extracted > 0) {
                    remaining -= extracted;
                    totalExtracted += extracted;
                }
            }
        } else {
            for (int i = tanks.size() - 1; i >= 0; i--) {
                if (remaining <= 0) break;
                TileTank t = tanks.get(i);
                int extracted = t.tank.extract(0, resolvedResource, remaining, transaction);
                if (extracted > 0) {
                    remaining -= extracted;
                    totalExtracted += extracted;
                }
            }
        }

        if (totalExtracted > 0) {
            owner.requestColumnBalance();
        }
        return totalExtracted;
    }

    private static void checkIndex(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size 1");
        }
    }
}
