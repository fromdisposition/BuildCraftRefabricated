/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import buildcraft.lib.tile.ItemHandlerSimple;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ContainerHeatExchange extends BcMenu {
   private static final ItemHandlerSimple FALLBACK_SLOTS = createFallbackSlots();
   @Nullable
   public final TileHeatExchange tile;
   public final WidgetFluidTank widgetTankStartInput;
   public final WidgetFluidTank widgetTankStartOutput;
   public final WidgetFluidTank widgetTankEndInput;
   public final WidgetFluidTank widgetTankEndOutput;
   private int lastSectionSyncHash = Integer.MIN_VALUE;

   public ContainerHeatExchange(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, resolveStartTile(MenuBlockEntityLookup.get(playerInv, pos, TileHeatExchange.class)));
   }

   public ContainerHeatExchange(int containerId, Inventory playerInv, @Nullable TileHeatExchange tile) {
      super(BCFactoryMenuTypes.HEAT_EXCHANGE, containerId, playerInv.player);
      this.tile = tile;
      ItemHandlerSimple machineSlots = tile != null && tile.getContainerSlots() != null ? tile.getContainerSlots() : FALLBACK_SLOTS;
      this.addSlot(new SlotBase(machineSlots, 0, 8, 28));
      this.addSlot(new SlotBase(machineSlots, 1, 8, 69));
      this.addSlot(new SlotBase(machineSlots, 2, 152, 17));
      this.addSlot(new SlotBase(machineSlots, 3, 152, 59));
      this.addFullPlayerInventory(8, 101);
      TileHeatExchange.ExchangeSectionStart start = startSection(tile);
      TileHeatExchange.ExchangeSectionEnd end = start != null ? start.getEndSection() : null;
      this.widgetTankStartInput = this.addWidget(new WidgetFluidTank(this, start != null ? start.tankInput : null));
      this.widgetTankStartOutput = this.addWidget(new WidgetFluidTank(this, start != null ? start.tankOutput : null));
      this.widgetTankEndInput = this.addWidget(new WidgetFluidTank(this, end != null ? end.tankInput : null));
      this.widgetTankEndOutput = this.addWidget(new WidgetFluidTank(this, end != null ? end.tankOutput : null));
   }

   @Override
   @Nullable
   protected ItemHandlerSimple getJeiBucketTransferSlots() {
      return this.tile != null ? this.tile.getContainerSlots() : null;
   }

   @Nullable
   public TileHeatExchange.ExchangeSectionStart startSection() {
      return startSection(this.tile);
   }

   @Nullable
   public TileHeatExchange.ExchangeSectionEnd endSection() {
      TileHeatExchange.ExchangeSectionStart start = this.startSection();
      return start != null ? start.getEndSection() : null;
   }

   @Nullable
   private static TileHeatExchange.ExchangeSectionStart startSection(@Nullable TileHeatExchange tile) {
      if (tile == null) {
         return null;
      } else {
         return tile.getSection() instanceof TileHeatExchange.ExchangeSectionStart s ? s : null;
      }
   }

   @Nullable
   private static TileHeatExchange resolveStartTile(@Nullable TileHeatExchange exchange) {
      return exchange != null ? exchange.findStart() : null;
   }

   private static ItemHandlerSimple createFallbackSlots() {
      ItemHandlerSimple slots = new ItemHandlerSimple(4, 1);
      slots.setChecker((slot, stack) -> false);
      return slots;
   }

   @Override
   public void broadcastChanges() {
      super.broadcastChanges();
      this.rebindSectionWidgets();
   }

   public int getSectionSyncHash() {
      return this.tile != null ? this.tile.getSyncHash() : 0;
   }

   private void rebindSectionWidgets() {
      if (this.tile == null) {
         return;
      }

      int hash = this.tile.getSyncHash();
      if (hash == this.lastSectionSyncHash) {
         return;
      }

      this.lastSectionSyncHash = hash;
      TileHeatExchange.ExchangeSectionStart start = this.startSection();
      TileHeatExchange.ExchangeSectionEnd end = start != null ? start.getEndSection() : null;
      this.widgetTankStartInput.setTank(start != null ? start.tankInput : null);
      this.widgetTankStartOutput.setTank(start != null ? start.tankOutput : null);
      this.widgetTankEndInput.setTank(end != null ? end.tankInput : null);
      this.widgetTankEndOutput.setTank(end != null ? end.tankOutput : null);
   }

   @Override
   public boolean stillValid(Player player) {
      return this.tile != null && Container.stillValidBlockEntity(this.tile, player);
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (index < 4) {
            if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, itemstack1);
      }

      return itemstack;
   }
}
