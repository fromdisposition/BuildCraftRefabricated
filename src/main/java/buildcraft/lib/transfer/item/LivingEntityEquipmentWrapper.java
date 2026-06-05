/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import buildcraft.lib.transfer.CombinedResourceHandler;
import buildcraft.lib.transfer.ResourceHandler;

public class LivingEntityEquipmentWrapper {

    private static final Map<LivingEntity, LivingEntityEquipmentWrapper> wrappers = new MapMaker().weakKeys().weakValues().makeMap();

    public static ResourceHandler<ItemResource> of(LivingEntity entity, EquipmentSlot.Type equipmentType) {
        if (entity instanceof Player player) {
            return switch (equipmentType) {
                case HAND -> PlayerInventoryWrapper.of(player).getHandSlots();
                case HUMANOID_ARMOR -> PlayerInventoryWrapper.of(player).getArmorSlots();
                default -> throw new IllegalArgumentException("Wrapping the equipment type " + equipmentType + " of a player is not supported.");
            };
        }

        return internalOf(entity, equipmentType);
    }

    public static ResourceHandler<ItemResource> of(LivingEntity entity, EquipmentSlot equipmentSlot) {
        if (entity instanceof Player player) {
            if (equipmentSlot == EquipmentSlot.MAINHAND) {
                return PlayerInventoryWrapper.of(player).getMainHandSlot();
            } else if (equipmentSlot == EquipmentSlot.OFFHAND) {
                return PlayerInventoryWrapper.of(player).getHandSlot(InteractionHand.OFF_HAND);
            } else if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                return PlayerInventoryWrapper.of(player).getArmorSlot(equipmentSlot);
            }
            throw new IllegalArgumentException("Wrapping the equipment slot " + equipmentSlot + " of a player is not supported.");
        }
        return internalOf(entity, equipmentSlot.getType()).getSlotWrapper(equipmentSlot.getIndex());
    }

    private static EquipmentTypeWrapper internalOf(LivingEntity entity, EquipmentSlot.Type equipmentType) {
        var wrapper = wrappers.computeIfAbsent(entity, LivingEntityEquipmentWrapper::new);
        return wrapper.byType.get(equipmentType);
    }

    private final LivingEntity entity;
    private final Map<EquipmentSlot.Type, EquipmentTypeWrapper> byType;

    private LivingEntityEquipmentWrapper(LivingEntity entity) {
        this.entity = entity;
        this.byType = new EnumMap<>(EquipmentSlot.Type.class);
        for (var equipmentType : EquipmentSlot.Type.values()) {
            var slotWrappers = new ArrayList<SlotWrapper>();
            for (var equipmentSlot : EquipmentSlot.VALUES) {
                if (equipmentSlot.getType() == equipmentType) {
                    slotWrappers.add(new SlotWrapper(equipmentSlot));
                }
            }
            this.byType.put(equipmentType, new EquipmentTypeWrapper(slotWrappers.toArray(SlotWrapper[]::new)));
        }
    }

    private class EquipmentTypeWrapper extends CombinedResourceHandler<ItemResource> {
        EquipmentTypeWrapper(SlotWrapper... handlers) {
            super(handlers);
        }

        SlotWrapper getSlotWrapper(int index) {
            return (SlotWrapper) getHandlerFromIndex(index);
        }
    }

    private class SlotWrapper extends ItemStackResourceHandler {
        private final EquipmentSlot slot;

        private SlotWrapper(EquipmentSlot slot) {
            this.slot = slot;
        }

        @Override
        protected ItemStack getStack() {
            return entity.getItemBySlot(slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            entity.setItemSlot(slot, stack);
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return resource.isEmpty() || entity.isEquippableInSlot(resource.toStack(), slot);
        }

        @Override
        protected int getCapacity(ItemResource resource) {
            ItemStack probe = resource.isEmpty() ? ItemStack.EMPTY : resource.toStack();
            int slotLimit = slot.limit(probe).getMaxStackSize();
            if (slotLimit <= 0) {
                slotLimit = Item.ABSOLUTE_MAX_STACK_SIZE;
            }
            return resource.isEmpty() ? slotLimit : Math.min(slotLimit, resource.getMaxStackSize());
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {

            entity.onEquipItem(slot, originalState, getStack());
        }

        @Override
        public String toString() {
            return "entity equipment wrapper[entity=" + entity + ",slot=" + slot + "]";
        }
    }
}
