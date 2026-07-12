/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.lib.fabric.transfer.fluid.FluidStorageOps;
import buildcraft.lib.fabric.transfer.fluid.ItemFluidLookup;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public final class FactoryFluidContainers {
   private FactoryFluidContainers() {
   }

   public static void syncDrainSlot(ItemHandlerSimple slots, int slot, Storage<FluidVariant> tank) {
      transferSlot(slots, slot, tank, true);
   }

   public static void syncFillSlot(ItemHandlerSimple slots, int slot, Storage<FluidVariant> tank) {
      transferSlot(slots, slot, tank, false);
   }

   private static void transferSlot(ItemHandlerSimple slots, int slot, Storage<FluidVariant> tank, boolean drainToTank) {
      ItemStack stack = slots.getStackInSlot(slot);
      if (stack.isEmpty() || stack.getCount() != 1) {
         return;
      }

      MachineSlotStorage slotStorage = new MachineSlotStorage(slots, slot);
      ContainerItemContext context = ContainerItemContext.ofSingleSlot(slotStorage);
      Storage<FluidVariant> hand = ItemFluidLookup.storage(stack, context);
      if (hand == null) {
         return;
      }

      try (Transaction tx = Transaction.openOuter()) {
         int moved = drainToTank ? FluidStorageOps.moveMb(hand, tank, 1000, tx) : FluidStorageOps.moveMb(tank, hand, 1000, tx);
         if (moved > 0) {
            tx.commit();
         }
      }
   }

   /** Writes bucket/container item changes back into the machine slot after a transfer commit. */
   private static final class MachineSlotStorage extends SingleVariantStorage<ItemVariant> {
      private final ItemHandlerSimple slots;
      private final int slot;

      private MachineSlotStorage(ItemHandlerSimple slots, int slot) {
         this.slots = slots;
         this.slot = slot;
         ItemStack stack = slots.getStackInSlot(slot);
         this.variant = ItemVariant.of(stack);
         this.amount = stack.getCount();
      }

      @Override
      protected ItemVariant getBlankVariant() {
         return ItemVariant.blank();
      }

      @Override
      protected long getCapacity(ItemVariant variant) {
         return variant.isBlank() ? this.slots.getSlotLimit(this.slot) : variant.toStack().getMaxStackSize();
      }

      @Override
      protected void onFinalCommit() {
         if (this.variant.isBlank() || this.amount <= 0L) {
            this.slots.setStackInSlot(this.slot, ItemStack.EMPTY);
         } else {
            this.slots.setStackInSlot(this.slot, this.variant.toStack((int)this.amount));
         }
      }
   }
}
