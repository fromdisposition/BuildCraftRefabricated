/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import buildcraft.lib.tile.ItemHandlerSimple;
import javax.annotation.Nonnull;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotBase extends Slot {
   public final int handlerIndex;
   public final ItemHandlerSimple itemHandler;

   public SlotBase(ItemHandlerSimple itemHandler, int slotIndex, int posX, int posY) {
      super(new SlotBase.DummyContainer(itemHandler), slotIndex, posX, posY);
      this.handlerIndex = slotIndex;
      this.itemHandler = itemHandler;
   }

   public boolean canShift() {
      return true;
   }

   public boolean mayPlace(@Nonnull ItemStack stack) {
      return stack.isEmpty() ? false : this.itemHandler.canSet(this.handlerIndex, stack);
   }

   @Nonnull
   public ItemStack getItem() {
      return this.itemHandler.getStackInSlot(this.handlerIndex);
   }

   public void set(@Nonnull ItemStack stack) {
      this.itemHandler.setStackInSlot(this.handlerIndex, stack);
      this.setChanged();
   }

   public void setChanged() {
      super.setChanged();
   }

   public int getMaxStackSize() {
      return this.itemHandler.getSlotLimit(this.handlerIndex);
   }

   public int getMaxStackSize(@Nonnull ItemStack stack) {
      int slotLimit = this.itemHandler.getSlotLimit(this.handlerIndex);
      return Math.min(slotLimit, stack.getMaxStackSize());
   }

   @Nonnull
   public ItemStack remove(int amount) {
      return this.itemHandler.extractItem(this.handlerIndex, amount, false);
   }

   private static class DummyContainer implements Container {
      private final ItemHandlerSimple handler;

      public DummyContainer(ItemHandlerSimple handler) {
         this.handler = handler;
      }

      public int getContainerSize() {
         return this.handler.size();
      }

      public boolean isEmpty() {
         return false;
      }

      public ItemStack getItem(int index) {
         return this.handler.getStackInSlot(index);
      }

      public ItemStack removeItem(int index, int count) {
         return ItemStack.EMPTY;
      }

      public ItemStack removeItemNoUpdate(int index) {
         return ItemStack.EMPTY;
      }

      public void setItem(int index, ItemStack stack) {
      }

      public void setChanged() {
      }

      public boolean stillValid(Player player) {
         return true;
      }

      public void clearContent() {
      }
   }
}
