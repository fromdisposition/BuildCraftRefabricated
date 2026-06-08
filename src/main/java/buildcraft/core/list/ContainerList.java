/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.core.BCCoreMenuTypes;
import buildcraft.core.PaperAdvancement;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.tile.ItemHandlerSimple;
import javax.annotation.Nonnull;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerList extends BcMenu {
   private static final int NET_LABEL = 1;
   private static final int NET_BUTTON = 2;
   public ListHandler.Line[] lines;
   private final InteractionHand hand;
   private final ItemHandlerSimple[] lineInventories;

   public static ContainerList fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
      InteractionHand hand = InteractionHand.values()[buf.readByte()];
      return new ContainerList(containerId, playerInv, hand);
   }

   public ContainerList(int containerId, Inventory playerInv, InteractionHand hand) {
      super(BCCoreMenuTypes.LIST, containerId, playerInv.player);
      this.hand = hand;
      this.lines = ListHandler.getLines(this.getListItemStack());
      this.lineInventories = new ItemHandlerSimple[this.lines.length];

      for (int line = 0; line < this.lines.length; line++) {
         this.lineInventories[line] = new ItemHandlerSimple(9);

         for (int slot = 0; slot < 9; slot++) {
            this.lineInventories[line].setStackInSlot(slot, this.lines[line].getStack(slot));
         }

         for (int slot = 0; slot < 9; slot++) {
            this.addSlot(new ContainerList.ListPhantomSlot(this.lineInventories[line], slot, 8 + slot * 18, 32 + line * 34, line));
         }
      }

      this.addFullPlayerInventory(8, 103);
   }

   @Override
   public boolean stillValid(Player player) {
      return !this.getListItemStack().isEmpty();
   }

   @Nonnull
   public ItemStack getListItemStack() {
      ItemStack stack = this.player.getItemInHand(this.hand);
      return !stack.isEmpty() && stack.getItem() instanceof ItemList_BC8 ? stack : ItemStack.EMPTY;
   }

   private void applyButtonToggle(int lineIndex, int button) {
      this.lines[lineIndex].toggleOption(button);

      if ((button == 1 || button == 2) && this.lines[lineIndex].isOneStackMode()) {
         for (int i = 1; i < 9; i++) {
            this.lineInventories[lineIndex].setStackInSlot(i, ItemStack.EMPTY);
            this.lines[lineIndex].setStack(i, ItemStack.EMPTY);
         }
      }

      ItemStack listStack = this.getListItemStack();
      ListHandler.saveLines(listStack, this.lines);
      ItemList_BC8.updateModelData(listStack);
   }

   public void switchButton(int lineIndex, int button) {
      this.applyButtonToggle(lineIndex, button);
      if (this.player.level().isClientSide()) {
         this.sendMessage(2, buffer -> {
            buffer.writeByte(lineIndex);
            buffer.writeByte(button);
         });
      }
   }

   public void setLabel(String text) {
      ItemStack stack = this.getListItemStack();
      if (!stack.isEmpty() && stack.getItem() instanceof ItemList_BC8 list) {
         list.setLocationName(stack, text);
      }

      if (this.player.level().isClientSide()) {
         this.sendMessage(1, buffer -> buffer.writeUtf(text));
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      super.readMessage(id, buffer, isClient, ctx);
      if (!isClient) {
         if (id == 2) {
            int lineIndex = buffer.readUnsignedByte();
            int button = buffer.readUnsignedByte();
            if (lineIndex >= 0 && lineIndex < this.lines.length && button >= 0 && button < 3) {
               this.applyButtonToggle(lineIndex, button);
            }
         } else if (id == 1) {
            this.setLabel(buffer.readUtf(1024));
         }
      }
   }

   @Override
   public ItemStack quickMoveStack(Player player, int slotIndex) {
      return ItemStack.EMPTY;
   }

   private class ListPhantomSlot extends SlotPhantom {
      final int lineIndex;

      ListPhantomSlot(ItemHandlerSimple handler, int slotIndex, int x, int y, int lineIndex) {
         super(handler, slotIndex, x, y, false);
         this.lineIndex = lineIndex;
      }

      @Override
      public void set(@Nonnull ItemStack stack) {
         int slotIndex = this.handlerIndex;
         if (slotIndex <= 0 || !ContainerList.this.lines[this.lineIndex].isOneStackMode()) {
            super.set(stack);
            ContainerList.this.lines[this.lineIndex].setStack(slotIndex, stack);
            ItemStack listStack = ContainerList.this.getListItemStack();
            ListHandler.saveLines(listStack, ContainerList.this.lines);
            ItemList_BC8.updateModelData(listStack);
            AdvancementUtil.unlockAdvancement(ContainerList.this.player, PaperAdvancement.ID, "write_to_list");
         }
      }
   }
}
