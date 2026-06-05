/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventHandler;

public class PipeBehaviourDiamondFluid extends PipeBehaviourDiamond {
    public PipeBehaviourDiamondFluid(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDiamondFluid(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public void sideCheck(PipeEventFluid.SideCheck sideCheck) {
        FluidStack toCompare = sideCheck.fluid;
        for (Direction face : Direction.values()) {
            if (sideCheck.isAllowed(face) && pipe.isConnected(face)) {
                int offset = FILTERS_PER_SIDE * face.ordinal();
                boolean sideAllowed = false;
                boolean foundItem = false;
                for (int i = 0; i < FILTERS_PER_SIDE; i++) {
                    ItemStack compareTo = filters.getStackInSlot(offset + i);
                    if (compareTo.isEmpty()) continue;

                    buildcraft.lib.transfer.access.ItemAccess access = buildcraft.lib.transfer.access.ItemAccess.forStack(compareTo);
                    buildcraft.lib.transfer.ResourceHandler<buildcraft.lib.transfer.fluid.FluidResource> handler = access.getCapability(buildcraft.lib.attachments.Attachments.Fluid.ITEM);
                    FluidStack target = FluidStack.EMPTY;
                    if (handler != null) {
                        for (int j = 0; j < handler.size(); j++) {
                            long amt = handler.getAmountAsLong(j);
                            if (amt > 0) {
                                target = handler.getResource(j).toStack((int)amt);
                                break;
                            }
                        }
                    }

                    if (target.isEmpty()) {
                        continue;
                    }
                    foundItem = true;
                    if (FluidStack.isSameFluidSameComponents(target, toCompare)) {
                        sideAllowed = true;
                        break;
                    }
                }
                if (foundItem) {
                    if (sideAllowed) {
                        sideCheck.increasePriority(face, 12);
                    } else {
                        sideCheck.disallow(face);
                    }
                }
            }
        }
    }
}
