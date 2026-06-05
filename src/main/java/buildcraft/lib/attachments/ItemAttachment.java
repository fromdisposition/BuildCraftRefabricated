/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.fabric.transfer.FabricTransferBridge;
import buildcraft.lib.transfer.access.ItemAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class ItemAttachment<T, C extends @Nullable Object> extends BaseAttachment<T, C> {

    public static <T, C extends @Nullable Object> ItemAttachment<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return (ItemAttachment<T, C>) registry.create(name, typeClass, contextClass);
    }

    public static <T> ItemAttachment<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static synchronized List<ItemAttachment<?, ?>> getAll() {
        return registry.getAll();
    }

    private static final AttachmentRegistry<ItemAttachment<?, ?>> registry = new AttachmentRegistry<ItemAttachment<?, ?>>(ItemAttachment::new);

    private ItemAttachment(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    final Map<Item, List<IAttachmentProvider<ItemStack, C, T>>> providers = new IdentityHashMap<>();

    @ApiStatus.Internal
    @Nullable
    public T getCapability(ItemStack stack, C context) {
        if (stack.isEmpty()) {
            return null;
        }

        T fromProviders = getCapabilityFromProvidersOnly(stack, context);
        if (fromProviders != null) {
            return fromProviders;
        }

        if (this == Attachments.Fluid.ITEM && context instanceof ItemAccess access) {
            return (T) FabricTransferBridge.tryWrapItemFluid(stack, access);
        }
        return null;
    }

    @ApiStatus.Internal
    @Nullable
    public T getCapabilityFromProvidersOnly(ItemStack stack, C context) {
        if (stack.isEmpty()) {
            return null;
        }

        for (var provider : providers.getOrDefault(stack.getItem(), List.of())) {
            var ret = provider.getCapability(stack, context);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }
}
