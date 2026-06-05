/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import buildcraft.lib.common.CommonHooks;
import buildcraft.lib.transfer.CombinedResourceHandler;
import buildcraft.lib.transfer.RangedResourceHandler;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.ResourceHandlerUtil;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.transaction.SnapshotJournal;
import buildcraft.lib.transfer.transaction.TransactionContext;

public final class PlayerInventoryWrapper extends VanillaContainerWrapper {

    public static PlayerInventoryWrapper of(Player player) {
        return of(player.getInventory());
    }

    public static PlayerInventoryWrapper of(Inventory inventory) {
        return (PlayerInventoryWrapper) VanillaContainerWrapper.of(inventory);
    }

    private final DroppedItems droppedItems = new DroppedItems();
    private final Inventory inventory;

    PlayerInventoryWrapper(Inventory inventory) {
        super(inventory);
        this.inventory = inventory;
    }

    @Override
    void resize() {

        size = Inventory.SLOT_BODY_ARMOR;
        while (slotWrappers.size() < size) {
            int index = slotWrappers.size();
            if (Inventory.INVENTORY_SIZE <= index && index < Inventory.SLOT_OFFHAND) {
                var equipmentSlot = Inventory.EQUIPMENT_SLOT_MAPPING.get(index);
                slotWrappers.add(new ArmorSlotWrapper(index, equipmentSlot));
            } else {
                slotWrappers.add(new SlotWrapper(index));
            }
        }
    }

    @Override
    void onRootCommit() {
        super.onRootCommit();

        if (!inventory.player.level().isClientSide()) {
            inventory.player.containerMenu.broadcastChanges();
        }
    }

    public ResourceHandler<ItemResource> getSlot(int slot) {
        return getSlotWrapper(slot);
    }

    public ResourceHandler<ItemResource> getMainHandSlot() {
        if (Inventory.isHotbarSlot(inventory.getSelectedSlot())) {
            return getSlot(inventory.getSelectedSlot());
        } else {
            throw new RuntimeException("Unexpected player selected slot: " + inventory.getSelectedSlot());
        }
    }

    public ResourceHandler<ItemResource> getHandSlot(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> getMainHandSlot();
            case OFF_HAND -> getSlot(Inventory.SLOT_OFFHAND);
        };
    }

    public ResourceHandler<ItemResource> getHandSlots() {
        return new CombinedResourceHandler<>(getMainHandSlot(), getHandSlot(InteractionHand.OFF_HAND));
    }

    public ResourceHandler<ItemResource> getMainSlots() {
        return RangedResourceHandler.of(this, 0, Inventory.INVENTORY_SIZE);
    }

    public ResourceHandler<ItemResource> getArmorSlot(EquipmentSlot slot) {
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            throw new IllegalArgumentException("EquipmentSlot is not an armor slot: " + slot);
        }
        return getSlot(slot.getIndex(Inventory.INVENTORY_SIZE));
    }

    public ResourceHandler<ItemResource> getArmorSlots() {
        return RangedResourceHandler.of(this, Inventory.INVENTORY_SIZE, Inventory.SLOT_OFFHAND);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = 0;

        for (InteractionHand hand : InteractionHand.values()) {
            var handSlot = getHandSlot(hand);

            if (handSlot.getResource(0).equals(resource)) {
                inserted += handSlot.insert(resource, amount - inserted, transaction);
                if (inserted == amount) {
                    return inserted;
                }
            }
        }

        inserted += ResourceHandlerUtil.insertStacking(getMainSlots(), resource, amount - inserted, transaction);

        return inserted;
    }

    public void placeItemBackInInventory(ItemResource resource, int amount, TransactionContext transactionContext) {
        int inserted = insert(resource, amount, transactionContext);
        if (inserted < amount) {

            drop(resource, amount - inserted, false, false, transactionContext);
        }
    }

    public void drop(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) return;

        if (!inventory.player.level().isClientSide()) {
            droppedItems.addDrop(resource, amount, dropAround, includeThrowerName, transaction);
        }
    }

    @Override
    public String toString() {
        return "PlayerInventoryWrapper{player=%s}".formatted(inventory.player);
    }

    private class DroppedItems extends SnapshotJournal<Integer> {
        final Deque<DropInfo> entries = new ArrayDeque<>();

        void addDrop(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
            updateSnapshots(transaction);
            entries.add(new DropInfo(resource, amount, dropAround, includeThrowerName));
        }

        @Override
        protected Integer createSnapshot() {
            return entries.size();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {

            int previousSize = snapshot;

            while (entries.size() > previousSize) {
                entries.removeLast();
            }
        }

        @Override
        protected void onRootCommit(Integer originalState) {

            while (!entries.isEmpty()) {
                DropInfo dropInfo = entries.removeFirst();
                int remainder = dropInfo.amount;

                int maxStackSize = dropInfo.resource.getMaxStackSize();
                while (remainder > 0) {
                    int dropped = Math.min(maxStackSize, remainder);

                    CommonHooks.onPlayerTossEvent(inventory.player, dropInfo.resource.toStack(dropped), dropInfo.dropAround, dropInfo.includeThrowerName);
                    remainder -= dropped;
                }
            }
        }

        private record DropInfo(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName) {}
    }

    private class ArmorSlotWrapper extends SlotWrapper {
        private final EquipmentSlot slot;

        ArmorSlotWrapper(int index, EquipmentSlot slot) {
            super(index);
            this.slot = slot;
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return inventory.player.isEquippableInSlot(resource.toStack(), slot) && super.isValid(resource);
        }

        @Override
        protected int getCapacity(ItemResource resource) {
            return resource.isEmpty() ? EquipmentSlot.NO_COUNT_LIMIT : slot.limit(resource.toStack(1)).getCount();
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {

            if (!inventory.player.isCreative() && EnchantmentHelper.has(resource.toStack(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                return 0;
            }
            return super.extract(index, resource, amount, transaction);
        }
    }
}
