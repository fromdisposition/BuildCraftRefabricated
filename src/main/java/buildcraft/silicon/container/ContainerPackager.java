/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.gui.SlotPackager;
import buildcraft.silicon.item.ItemPackage;
import buildcraft.silicon.tile.TilePackager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ContainerPackager extends ContainerBCTile<TilePackager> {
   public ContainerPackager(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv.player, getTile(playerInv, pos));
   }

   public ContainerPackager(int containerId, Player player, TilePackager tile) {
      super(BCSiliconMenuTypes.PACKAGER, containerId, player, tile);
      if (tile != null) {
         this.addSlot(new SlotBase(tile.invInput, 0, 124, 7) {
            @Override
            public boolean mayPlace(ItemStack stack) {
               return !stack.isEmpty() && (stack.is(Items.PAPER) || ItemPackage.isPackage(stack));
            }
         });

         for (int x = 0; x < 9; x++) {
            this.addSlot(new SlotBase(tile.invStorage, x, 8 + x * 18, 84));
         }

         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
               this.addSlot(new SlotPackager(tile.invPattern, x + y * 3, 30 + x * 18, 17 + y * 18));
            }
         }

         this.addSlot(new SlotOutput(tile.invOutput, 0, 123, 59));
      }

      this.addFullPlayerInventory(8, 115);
   }

   @Override
   public void clicked(int slotId, int dragType, ContainerInput containerInput, Player player) {
      Slot slot = slotId < 0 || slotId >= this.slots.size() ? null : this.slots.get(slotId);
      if (this.tile != null && slot instanceof SlotPackager pattern) {
         int index = pattern.handlerIndex;
         ItemStack held = this.getCarried();
         if (held.isEmpty()) {
            this.tile.setPatternSlot(index, !this.tile.isPatternSlotSet(index));
         } else {
            ItemStack copy = held.copy();
            copy.setCount(1);
            pattern.set(copy);
            this.tile.setPatternSlot(index, true);
         }

         if (!player.level().isClientSide()) {
            MessageUtil.sendUpdateToTrackingPlayers(this.tile);
         }

         return;
      }

      super.clicked(slotId, dragType, containerInput, player);
   }

   private static TilePackager getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TilePackager table ? table : null;
   }
}
