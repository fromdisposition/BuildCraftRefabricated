/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.integration.jei;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.tile.item.ItemHandlerSimple;

public final class JeiTransferUtil {
    private JeiTransferUtil() {}

    public static int countMatching(Inventory playerInv, ItemStack want) {
        if (want.isEmpty()) return 0;
        int total = 0;
        for (int i = 0; i < playerInv.getContainerSize(); i++) {
            ItemStack s = playerInv.getItem(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, want)) {
                total += s.getCount();
            }
        }
        return total;
    }

    public static int moveMatchingToHandler(Inventory playerInv, ItemStack want, int max, ItemHandlerSimple dest) {
        if (want.isEmpty() || max <= 0) return 0;
        int toMove = Math.min(max, countMatching(playerInv, want));
        if (toMove <= 0) return 0;

        int remaining = toMove;
        for (int slot = 0; slot < dest.getSlots() && remaining > 0; slot++) {
            ItemStack attempt = want.copy();
            attempt.setCount(remaining);
            ItemStack leftover = dest.insertItem(slot, attempt, false);
            remaining -= (attempt.getCount() - leftover.getCount());
        }
        int moved = toMove - remaining;
        if (moved > 0) {
            removeFromInventory(playerInv, want, moved);
        }
        return moved;
    }

    public static boolean moveBucketToSlot(Inventory playerInv, Item bucket, ItemHandlerSimple dest, int slot) {
        if (bucket == null || bucket == Items.AIR) return false;
        if (slot < 0 || slot >= dest.getSlots()) return false;
        if (!dest.getStackInSlot(slot).isEmpty()) return false;

        for (int i = 0; i < playerInv.getContainerSize(); i++) {
            ItemStack s = playerInv.getItem(i);
            if (s.isEmpty() || !s.is(bucket)) continue;

            ItemStack one = s.copy();
            one.setCount(1);
            ItemStack leftover = dest.insertItem(slot, one, false);
            if (leftover.isEmpty()) {
                s.shrink(1);
                if (s.isEmpty()) {
                    playerInv.setItem(i, ItemStack.EMPTY);
                }
                playerInv.setChanged();
                return true;
            }
            return false;
        }
        return false;
    }

    private static void removeFromInventory(Inventory playerInv, ItemStack want, int count) {
        int remaining = count;
        for (int i = 0; i < playerInv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = playerInv.getItem(i);
            if (s.isEmpty() || !ItemStack.isSameItemSameComponents(s, want)) continue;
            int take = Math.min(remaining, s.getCount());
            s.shrink(take);
            remaining -= take;
            if (s.isEmpty()) {
                playerInv.setItem(i, ItemStack.EMPTY);
            }
        }
        playerInv.setChanged();
    }
}

