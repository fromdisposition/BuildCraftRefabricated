/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.lib.fabric.transfer.fluid.FluidStorageOps;
import buildcraft.lib.fabric.transfer.fluid.ItemFluidLookup;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public final class FactoryFluidContainers {
   private FactoryFluidContainers() {
   }

   public static void syncDrainSlot(ItemHandlerSimple slots, int slot, Storage<FluidVariant> tank) {
      ItemStack stack = slots.getStackInSlot(slot);
      if (!stack.isEmpty() && stack.getCount() == 1) {
         Storage<FluidVariant> hand = ItemFluidLookup.storage(stack);
         if (hand != null) {
            try (Transaction tx = Transaction.openOuter()) {
               if (FluidStorageOps.moveMb(hand, tank, 1000, tx) > 0) {
                  tx.commit();
                  slots.setStackInSlot(slot, stack);
               }
            }
         }
      }
   }

   public static void syncFillSlot(ItemHandlerSimple slots, int slot, Storage<FluidVariant> tank) {
      ItemStack stack = slots.getStackInSlot(slot);
      if (!stack.isEmpty() && stack.getCount() == 1) {
         Storage<FluidVariant> hand = ItemFluidLookup.storage(stack);
         if (hand != null) {
            try (Transaction tx = Transaction.openOuter()) {
               if (FluidStorageOps.moveMb(tank, hand, 1000, tx) > 0) {
                  tx.commit();
                  slots.setStackInSlot(slot, stack);
               }
            }
         }
      }
   }
}
