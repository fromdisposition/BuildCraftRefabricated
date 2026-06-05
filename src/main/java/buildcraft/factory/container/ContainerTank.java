/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.factory.tile.TankColumnResourceHandler;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.widget.WidgetFluidTank;

@SuppressWarnings("this-escape")
public class ContainerTank extends ContainerBC_Neptune {
    public final TileTank tile;
    public final WidgetFluidTank widgetTank;

    public ContainerTank(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileTank.class));
    }

    public ContainerTank(int containerId, Inventory playerInv, TileTank tank) {
        super(BCFactoryMenuTypes.TANK, containerId, playerInv.player);
        this.tile = tank;

        addFullPlayerInventory(8, 99);

        widgetTank = addWidget(new WidgetFluidTank(this, tank != null ? new TankColumnResourceHandler(tank) : null));
    }

    @Override
    public boolean stillValid(Player player) {
        if (tile == null) return false;
        if (tile.getLevel() == null || tile.getLevel().getBlockEntity(tile.getBlockPos()) != tile) {
            return false;
        }
        return player.distanceToSqr(
            tile.getBlockPos().getX() + 0.5,
            tile.getBlockPos().getY() + 0.5,
            tile.getBlockPos().getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        return ItemStack.EMPTY;
    }
}
