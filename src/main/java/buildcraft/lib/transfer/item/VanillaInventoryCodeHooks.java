/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.AttachmentQueries;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.ResourceHandlerUtil;
import buildcraft.lib.transfer.transaction.Transaction;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class VanillaInventoryCodeHooks {

    public static boolean extractHook(Hopper dest, ResourceHandler<ItemResource> handler) {
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            var itemResource = handler.getResource(index);
            if (itemResource.isEmpty()) continue;
            try (var tx = Transaction.openRoot()) {
                int extracted = handler.extract(index, itemResource, 1, tx);
                if (extracted == 0) {
                    continue;
                }

                var extractedStack = itemResource.toStack();
                int destSize = dest.getContainerSize();
                for (int j = 0; j < destSize; j++) {
                    ItemStack destStack = dest.getItem(j);
                    if (dest.canPlaceItem(j, extractedStack) && (destStack.isEmpty() || destStack.getCount() < destStack.getMaxStackSize() && destStack.getCount() < dest.getMaxStackSize() && itemResource.matches(destStack))) {
                        if (destStack.isEmpty()) {
                            dest.setItem(j, extractedStack);
                        } else {
                            destStack.grow(1);
                            dest.setItem(j, destStack);
                        }
                        dest.setChanged();
                        tx.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean insertHook(HopperBlockEntity hopper, ResourceHandler<ItemResource> itemHandler) {
        if (ResourceHandlerUtil.isFull(itemHandler)) {
            return false;
        }
        try (var tx = Transaction.openRoot()) {
            int size = hopper.getContainerSize();
            for (int i = 0; i < size; ++i) {
                var hopperItem = hopper.getItem(i);
                if (hopperItem.isEmpty()) {
                    continue;
                }

                ItemStack originalSlotContents = hopperItem.copy();
                ItemStack insertStack = hopper.removeItem(i, 1);

                if (itemHandler.insert(ItemResource.of(insertStack), 1, tx) == 1) {
                    tx.commit();
                    return true;
                } else {

                    hopper.setItem(i, originalSlotContents);
                }
            }
        }

        return false;
    }

    public static ContainerOrHandler getEntityContainerOrHandler(Level level, double x, double y, double z, @Nullable Direction side) {
        List<Entity> list = level.getEntities(
                (Entity) null,
                new AABB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D),
                entity -> {

                    if (!entity.isAlive()) {
                        return false;
                    }
                    return entity instanceof Container
                            || AttachmentQueries.getEntity(entity, Attachments.Item.ENTITY_AUTOMATION, side) != null;
                });
        if (!list.isEmpty()) {
            var entity = list.get(level.getRandom().nextInt(list.size()));
            if (entity instanceof Container container) {
                return new ContainerOrHandler(container, null);
            }
            ResourceHandler<ItemResource> entityCap =
                    AttachmentQueries.getEntity(entity, Attachments.Item.ENTITY_AUTOMATION, side);
            if (entityCap != null) {
                return new ContainerOrHandler(null, entityCap);
            }
        }
        return ContainerOrHandler.EMPTY;
    }
}
