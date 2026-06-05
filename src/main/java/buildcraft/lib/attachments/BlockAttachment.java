/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class BlockAttachment<T, C extends @Nullable Object> extends BaseAttachment<T, C> {

    public static <T, C extends @Nullable Object> BlockAttachment<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return (BlockAttachment<T, C>) registry.create(name, typeClass, contextClass);
    }

    public static <T> BlockAttachment<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static <T> BlockAttachment<T, @Nullable Direction> createSided(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    public static synchronized List<BlockAttachment<?, ?>> getAll() {
        return registry.getAll();
    }

    public static synchronized List<BlockAttachment<?, ?>> getAllProxyable() {
        return registry.getAll().stream().filter(BlockAttachment::isProxyable).toList();
    }

    public boolean isProxyable() {
        return proxyable == TriState.TRUE;
    }

    private static final AttachmentRegistry<BlockAttachment<?, ?>> registry = new AttachmentRegistry<BlockAttachment<?, ?>>(BlockAttachment::new);

    private BlockAttachment(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    final Map<Block, List<IBlockAttachmentProvider<T, C>>> providers = new IdentityHashMap<>();
    private TriState proxyable = TriState.DEFAULT;

    void setProxyable(boolean proxyable) {
        if (AttachmentHooks.initFinished) {
            throw new IllegalStateException("Cannot call setProxyable after the RegisterAttachmentsEvent has been fired.");
        }
        switch (this.proxyable) {
            case DEFAULT -> this.proxyable = proxyable ? TriState.TRUE : TriState.FALSE;
            case TRUE -> {
                if (!proxyable) {
                    throw new IllegalStateException("Cannot make capability %s non-proxyable because it was already set to be proxyable.".formatted(name()));
                }
            }
            case FALSE -> {
                if (proxyable) {
                    throw new IllegalStateException("Cannot make capability %s proxyable because it was already set to be non-proxyable.".formatted(name()));
                }
            }
        }
    }

    @ApiStatus.Internal
    @Nullable
    public T getCapability(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {

        pos = pos.immutable();

        if (blockEntity == null) {
            if (state == null)
                state = level.getBlockState(pos);

            if (state.hasBlockEntity())
                blockEntity = level.getBlockEntity(pos);
        } else {
            if (state == null)
                state = blockEntity.getBlockState();
        }

        for (var provider : providers.getOrDefault(state.getBlock(), List.of())) {
            var ret = provider.getCapability(level, pos, state, blockEntity, context);
            if (ret != null)
                return ret;
        }

        T bridged = buildcraft.lib.fabric.transfer.FabricTransferBridge.tryWrapForward(
                this, level, pos, state, blockEntity, context);
        if (bridged != null)
            return bridged;
        return null;
    }
}
