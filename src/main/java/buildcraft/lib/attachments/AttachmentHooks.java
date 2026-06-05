/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package buildcraft.lib.attachments;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.fluid.BucketResourceHandler;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.item.PlayerInventoryWrapper;
import buildcraft.lib.transfer.item.VanillaContainerWrapper;
import buildcraft.lib.transfer.item.WorldlyContainerWrapper;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public final class AttachmentHooks {
    private static boolean initialized = false;
    static boolean initFinished = false;

    private AttachmentHooks() {}

    public static void init() {
        if (initialized) {
            throw new IllegalArgumentException("AttachmentHooks.init() called twice");
        }
        initialized = true;

        var event = new RegisterAttachmentsEvent();
        markProxyableCapabilities(event);
        buildcraft.lib.fluids.CauldronFluidContent.registerCapabilities(event);
        registerVanillaProviders(event);

        initFinished = true;
    }

    public static void markProxyableCapabilities(RegisterAttachmentsEvent event) {
        event.setProxyable(Attachments.Energy.BLOCK);
        event.setProxyable(Attachments.Fluid.BLOCK);
        event.setProxyable(Attachments.Item.BLOCK);
    }

    public static void registerVanillaProviders(RegisterAttachmentsEvent event) {
        registerChestBlocks(event);
        registerContainerBlockEntities(event);
        registerPlayerEntities(event);
        registerBucketItems(event);
        registerComposter(event);
    }

    private static void registerComposter(RegisterAttachmentsEvent event) {
        event.registerBlock(
                Attachments.Item.BLOCK,
                (level, pos, state, blockEntity, side) -> buildcraft.lib.transfer.item.ComposterWrapper.get(level, pos, side),
                Blocks.COMPOSTER);
    }

    private static void registerBucketItems(RegisterAttachmentsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof BucketItem)) {
                continue;
            }
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            if (itemId != null && "buildcraftenergy".equals(itemId.getNamespace())) {
                continue;
            }
            event.registerItem(
                    Attachments.Fluid.ITEM,
                    (stack, access) -> new BucketResourceHandler(access),
                    item);
        }
    }

    private static void registerChestBlocks(RegisterAttachmentsEvent event) {
        event.registerBlock(
                Attachments.Item.BLOCK,
                (level, pos, state, blockEntity, side) -> {
                    if (blockEntity instanceof Container container) {
                        return itemHandlerFor(container, side);
                    }
                    Block block = state.getBlock();
                    if (block instanceof ChestBlock chestBlock) {
                        Container container = ChestBlock.getContainer(chestBlock, state, level, pos, true);
                        if (container != null) {
                            return itemHandlerFor(container, side);
                        }
                    }
                    return null;
                },
                Blocks.CHEST,
                Blocks.TRAPPED_CHEST);
    }

    private static void registerContainerBlockEntities(RegisterAttachmentsEvent event) {
        List<BlockEntityType<?>> types = List.of(
                BlockEntityType.FURNACE,
                BlockEntityType.BLAST_FURNACE,
                BlockEntityType.SMOKER,
                BlockEntityType.BREWING_STAND,
                BlockEntityType.HOPPER,
                BlockEntityType.CHEST,
                BlockEntityType.TRAPPED_CHEST,
                BlockEntityType.BARREL,
                BlockEntityType.DISPENSER,
                BlockEntityType.DROPPER,
                BlockEntityType.SHULKER_BOX);
        for (BlockEntityType<?> type : types) {
            event.registerBlockEntity(
                    Attachments.Item.BLOCK,
                    type,
                    (blockEntity, side) -> blockEntity instanceof Container container
                            ? itemHandlerFor(container, side)
                            : null);
        }
    }

    private static void registerPlayerEntities(RegisterAttachmentsEvent event) {
        event.registerEntity(
                Attachments.Item.ENTITY,
                EntityType.PLAYER,
                (player, ignored) -> PlayerInventoryWrapper.of(player));
        event.registerEntity(
                Attachments.Item.ENTITY_AUTOMATION,
                EntityType.PLAYER,
                (player, side) -> PlayerInventoryWrapper.of(player));
    }

    @Nullable
    private static ResourceHandler<ItemResource> itemHandlerFor(Container container, @Nullable Direction side) {
        if (container instanceof WorldlyContainer worldly && side != null) {
            return new WorldlyContainerWrapper(worldly, side);
        }
        return VanillaContainerWrapper.of(container);
    }
}
