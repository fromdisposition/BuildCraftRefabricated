/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerTank extends BcMenu {
   public final TileTank tile;
   public final WidgetFluidTank widgetTank;

   public ContainerTank(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileTank.class));
   }

   public ContainerTank(int containerId, Inventory playerInv, TileTank tank) {
      super(BCFactoryMenuTypes.TANK, containerId, playerInv.player);
      this.tile = tank;
      this.addFullPlayerInventory(8, 96);
      this.widgetTank = this.addWidget(new WidgetFluidTank(this, tank == null ? null : tank.fluidTank));
   }

   @Override
   public boolean stillValid(Player player) {
      return this.tile != null && Container.stillValidBlockEntity(this.tile, player);
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }
}
