/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public class RegisterAttachmentsEvent {
    public RegisterAttachmentsEvent() {}

    public <T, C extends @Nullable Object> void registerBlock(BlockAttachment<T, C> capability, IBlockAttachmentProvider<T, C> provider, Block... blocks) {
        Objects.requireNonNull(provider);

        if (blocks.length == 0)
            throw new IllegalArgumentException("Must register at least one block");

        for (Block block : blocks) {
            Objects.requireNonNull(block);
            capability.providers.computeIfAbsent(block, b -> new ArrayList<>()).add(provider);
        }
    }

    public <T, C extends @Nullable Object, BE extends BlockEntity> void registerBlockEntity(BlockAttachment<T, C> capability, BlockEntityType<BE> blockEntityType, IAttachmentProvider<? super BE, C, T> provider) {
        Objects.requireNonNull(provider);

        IBlockAttachmentProvider<T, C> adaptedProvider = (level, pos, state, blockEntity, context) -> {

            if (blockEntity == null || blockEntity.getType() != blockEntityType)
                return null;
            return provider.getCapability((BE) blockEntity, context);
        };

        for (Block block : BuiltInRegistries.BLOCK) {
            boolean matches = false;
            for (var state : block.getStateDefinition().getPossibleStates()) {
                if (blockEntityType.isValid(state)) {
                    matches = true;
                    break;
                }
            }
            if (matches) {
                capability.providers.computeIfAbsent(block, b -> new ArrayList<>()).add(adaptedProvider);
            }
        }
    }

    public boolean isBlockRegistered(BlockAttachment<?, ?> capability, Block block) {
        Objects.requireNonNull(block);
        return capability.providers.containsKey(block);
    }

    public void setProxyable(BlockAttachment<?, ?> capability) {
        capability.setProxyable(true);
    }

    public void setNonProxyable(BlockAttachment<?, ?> capability) {
        capability.setProxyable(false);
    }

    public <T, C extends @Nullable Object, E extends Entity> void registerEntity(EntityAttachment<T, C> capability, EntityType<E> entityType, IAttachmentProvider<? super E, C, T> provider) {
        Objects.requireNonNull(provider);
        capability.providers.computeIfAbsent(entityType, et -> new ArrayList<>()).add((IAttachmentProvider<Entity, C, T>) provider);
    }

    public boolean isEntityRegistered(EntityAttachment<?, ?> capability, EntityType<?> entityType) {
        Objects.requireNonNull(entityType);
        return capability.providers.containsKey(entityType);
    }

    public <T, C extends @Nullable Object> void registerItem(ItemAttachment<T, C> capability, IAttachmentProvider<ItemStack, C, T> provider, ItemLike... items) {
        Objects.requireNonNull(provider);

        if (items.length == 0)
            throw new IllegalArgumentException("Must register at least one item");

        for (ItemLike itemLike : items) {
            Item item = Objects.requireNonNull(itemLike.asItem());
            capability.providers.computeIfAbsent(item, i -> new ArrayList<>()).add(provider);
        }
    }

    public boolean isItemRegistered(ItemAttachment<?, ?> capability, Item item) {
        Objects.requireNonNull(item);
        return capability.providers.containsKey(item);
    }
}
