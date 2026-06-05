/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class BlockAttachmentCache<T, C extends @Nullable Object> {

    public static <T, C extends @Nullable Object> BlockAttachmentCache<T, C> create(BlockAttachment<T, C> capability, ServerLevel level, BlockPos pos, C context) {
        return create(capability, level, pos, context, () -> true, () -> {});
    }

    public static <T, C extends @Nullable Object> BlockAttachmentCache<T, C> create(BlockAttachment<T, C> capability, ServerLevel level, BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
        Objects.requireNonNull(capability);
        Objects.requireNonNull(isValid);
        Objects.requireNonNull(invalidationListener);
        pos = pos.immutable();

        var cache = new BlockAttachmentCache<>(capability, level, pos, context, isValid, invalidationListener);
        if (level instanceof AttachmentQueries.CapabilityListenerLevel listenerLevel) {
            listenerLevel.buildcraft$registerCapabilityListener(pos, cache.listener);
        }
        return cache;
    }

    private final BlockAttachment<T, C> capability;
    private final ServerLevel level;
    private final BlockPos pos;
    private final C context;

    private boolean cacheValid = false;
    @Nullable
    private T cachedCap = null;

    private boolean canQuery = true;
    private final IAttachmentInvalidationListener listener;

    private BlockAttachmentCache(BlockAttachment<T, C> capability, ServerLevel level, BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
        this.capability = capability;
        this.level = level;
        this.pos = pos;
        this.context = context;

        this.listener = () -> {
            if (!cacheValid) {

                return isValid.getAsBoolean();
            }

            canQuery = false;

            cacheValid = false;

            cachedCap = null;

            if (isValid.getAsBoolean()) {

                invalidationListener.run();

                canQuery = true;
                return true;
            } else {

                return false;
            }
        };
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }

    public C context() {
        return context;
    }

    @Nullable
    public T getCapability() {
        if (!canQuery)
            throw new IllegalStateException("Do not call getCapability on an invalid cache or from the invalidation listener!");

        if (!cacheValid) {
            if (!level.isLoaded(pos)) {

                cachedCap = null;
            } else {
                cachedCap = capability.getCapability(level, pos, null, null, context);
            }
            cacheValid = true;
        }

        return cachedCap;
    }
}
