/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class BcMenu extends AbstractContainerMenu implements IBcMenu {
   public static final int NET_WIDGET = 0;
   public static final int NET_GHOST_SLOT_SET = 101;
   public static final int NET_BUCKET_TRANSFER = 103;
   public final Player player;
   private final List<Widget_Neptune<?>> widgets = new ArrayList<>();

   protected BcMenu(MenuType<?> menuType, int containerId, Player player) {
      super(menuType, containerId);
      this.player = player;
   }

   /** Data slots are 16-bit on the wire (writeShort), so a wider value arrives truncated and sign-extended (a
    * float typically decodes as NaN); split into unsigned 16-bit chunks and reassemble with masking on the client. */
   public static int chunk16(long value, int chunk) {
      return (int)(value >>> chunk * 16) & 0xFFFF;
   }

   public static int readInt32(ContainerData data, int firstSlot) {
      return data.get(firstSlot) & 0xFFFF | (data.get(firstSlot + 1) & 0xFFFF) << 16;
   }

   public static long readLong64(ContainerData data, int firstSlot) {
      return readInt32(data, firstSlot) & 4294967295L | (readInt32(data, firstSlot + 2) & 4294967295L) << 32;
   }

   public static float readFloat32(ContainerData data, int firstSlot) {
      return Float.intBitsToFloat(readInt32(data, firstSlot));
   }

   protected void addFullPlayerInventory(int startX, int startY) {
      this.addFullPlayerInventory(startX, startY, this.player.getInventory());
   }

   protected void addFullPlayerInventory(int startX, int startY, Inventory inv) {
      // addStandardInventorySlots lets mods that mixin-extend the player inventory (e.g. Inventory Extended) add
      // their extra rows to BC GUIs too; 1.21.1 has no such helper, so it keeps the manual loop.
      //? if >= 1.21.10 {
      this.addStandardInventorySlots(inv, startX, startY);
      //?} else {
      /*for (int sy = 0; sy < 3; sy++) {
         for (int sx = 0; sx < 9; sx++) {
            this.addSlot(new Slot(inv, sx + sy * 9 + 9, startX + sx * 18, startY + sy * 18));
         }
      }

      for (int sx = 0; sx < 9; sx++) {
         this.addSlot(new Slot(inv, sx, startX + sx * 18, startY + 58));
      }
      *///?}
   }

   public <W extends Widget_Neptune<?>> W addWidget(W widget) {
      if (widget == null) {
         throw new NullPointerException("widget");
      }

      this.widgets.add(widget);
      return widget;
   }

   public ImmutableList<Widget_Neptune<?>> getWidgets() {
      return ImmutableList.copyOf(this.widgets);
   }

   @Override
   public final void sendMessage(int id, IPayloadWriter writer) {
      BcMenuSupport.sendMessage(this, this.player, id, writer);
   }

   @Override
   public Player getPlayer() { return player; }

   @Override
   public void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer) {
      BcMenuSupport.sendWidgetData(this, this.widgets, widget, writer);
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      BcMenuSupport.readMessage(this, this.player, this.widgets, this::getBucketTransferSlots, id, buffer, isClient, ctx);
   }

   @javax.annotation.Nullable
   protected ItemHandlerSimple getBucketTransferSlots() {
      return null;
   }

   @Override
   public void clicked(int slotId, int dragType, ContainerInput containerInput, Player player) {
      if (!BcMenuSupport.clickPhantom(this, slotId)) {
         super.clicked(slotId, dragType, containerInput, player);
      }
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      return BcMenuSupport.quickMoveStack(this, index, this::moveItemStackTo);
   }

   @Override
   public boolean stillValid(Player player) {
      return player.isAlive() && !player.isRemoved();
   }
}
