/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.lib.fabric.transfer.FluidStorageOps;
import buildcraft.lib.fabric.transfer.ItemFluidLookup;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.lib.transfer.fabric.TransferConvert;
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
            Transaction tx = Transaction.openOuter();

            try {
               long moved = FluidStorageOps.move(hand, tank, TransferConvert.mbToDroplets(1000L), tx);
               if (moved > 0L) {
                  tx.commit();
                  slots.setStackInSlot(slot, stack);
               }
            } catch (Throwable var9) {
               if (tx != null) {
                  try {
                     tx.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (tx != null) {
               tx.close();
            }
         }
      }
   }

   public static void syncFillSlot(ItemHandlerSimple slots, int slot, Storage<FluidVariant> tank) {
      ItemStack stack = slots.getStackInSlot(slot);
      if (!stack.isEmpty() && stack.getCount() == 1) {
         Storage<FluidVariant> hand = ItemFluidLookup.storage(stack);
         if (hand != null) {
            Transaction tx = Transaction.openOuter();

            try {
               long moved = FluidStorageOps.move(tank, hand, TransferConvert.mbToDroplets(1000L), tx);
               if (moved > 0L) {
                  tx.commit();
                  slots.setStackInSlot(slot, stack);
               }
            } catch (Throwable var9) {
               if (tx != null) {
                  try {
                     tx.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (tx != null) {
               tx.close();
            }
         }
      }
   }
}
