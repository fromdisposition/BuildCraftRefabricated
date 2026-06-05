/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.attachments.BlockAttachment;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.AttachmentQueries;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.item.ItemResource;

import org.jspecify.annotations.Nullable;

public final class FabricTransferBridge {
    private FabricTransferBridge() {}

    private static final ThreadLocal<Boolean> REENTRANT = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static boolean reverseRegistered = false;

    public static synchronized void init() {
        if (reverseRegistered) {
            return;
        }
        reverseRegistered = true;

        FluidStorage.SIDED.registerFallback((level, pos, state, blockEntity, side) -> {
            if (REENTRANT.get()) {
                return null;
            }
            REENTRANT.set(Boolean.TRUE);
            try {
                ResourceHandler<FluidResource> handler =
                        AttachmentQueries.getBlock(level, Attachments.Fluid.BLOCK, pos, side);
                return handler == null ? null : new BcFluidStorage(handler);
            } finally {
                REENTRANT.set(Boolean.FALSE);
            }
        });

        ItemStorage.SIDED.registerFallback((level, pos, state, blockEntity, side) -> {
            if (REENTRANT.get()) {
                return null;
            }
            REENTRANT.set(Boolean.TRUE);
            try {
                ResourceHandler<ItemResource> handler =
                        AttachmentQueries.getBlock(level, Attachments.Item.BLOCK, pos, side);
                return handler == null ? null : new BcItemStorage(handler);
            } finally {
                REENTRANT.set(Boolean.FALSE);
            }
        });

        FluidStorage.ITEM.registerFallback((stack, context) -> {
            if (REENTRANT.get()) {
                return null;
            }
            REENTRANT.set(Boolean.TRUE);
            try {
                ItemAccess access = new ContainerItemContextItemAccess(context);
                ResourceHandler<FluidResource> handler =
                        Attachments.Fluid.ITEM.getCapabilityFromProvidersOnly(stack, access);
                return handler == null ? null : new BcFluidStorage(handler);
            } finally {
                REENTRANT.set(Boolean.FALSE);
            }
        });
    }

    public static @Nullable ResourceHandler<FluidResource> tryWrapItemFluid(ItemStack stack, ItemAccess access) {
        if (REENTRANT.get() || stack.isEmpty()) {
            return null;
        }
        REENTRANT.set(Boolean.TRUE);
        try {
            ContainerItemContext context = toContainerContext(access, stack);
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, context);
            if (storage == null) {
                return null;
            }
            return new FabricFluidResourceHandler(storage);
        } finally {
            REENTRANT.set(Boolean.FALSE);
        }
    }

    private static ContainerItemContext toContainerContext(ItemAccess access, ItemStack stack) {
        if (access instanceof ContainerItemContextItemAccess wrapped) {
            return wrapped.context();
        }
        return ContainerItemContext.withConstant(stack);
    }

    static boolean isReentrant() {
        return REENTRANT.get();
    }

    @SuppressWarnings("unchecked")
    public static <T, C> @Nullable T tryWrapForward(
            BlockAttachment<T, C> capability,
            Level level, BlockPos pos,
            @Nullable BlockState state, @Nullable BlockEntity blockEntity,
            C context) {
        if (REENTRANT.get()) {
            return null;
        }
        if (capability != Attachments.Fluid.BLOCK && capability != Attachments.Item.BLOCK) {
            return null;
        }
        Direction side = context instanceof Direction d ? d : null;

        REENTRANT.set(Boolean.TRUE);
        try {
            if (capability == Attachments.Fluid.BLOCK) {
                Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, blockEntity, side);
                if (storage == null) {
                    return null;
                }
                return (T) new FabricFluidResourceHandler(storage);
            } else {
                Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, blockEntity, side);
                if (storage == null) {
                    return null;
                }
                return (T) new FabricItemResourceHandler(storage);
            }
        } finally {
            REENTRANT.set(Boolean.FALSE);
        }
    }
}
