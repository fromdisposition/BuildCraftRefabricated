/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.integration.jei.JeiTransferUtil;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerSimple;

@SuppressWarnings("this-escape")
public class ContainerDistiller extends ContainerBC_Neptune {
    private static final ItemHandlerSimple FALLBACK_SLOTS = createFallbackSlots();

    public final TileDistiller_BC8 tile;
    public final WidgetFluidTank widgetTankIn;
    public final WidgetFluidTank widgetTankGasOut;
    public final WidgetFluidTank widgetTankLiquidOut;

    public ContainerDistiller(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileDistiller_BC8.class));
    }

    public ContainerDistiller(int containerId, Inventory playerInv, TileDistiller_BC8 tile) {
        super(BCFactoryMenuTypes.DISTILLER, containerId, playerInv.player);
        this.tile = tile;

        ItemHandlerSimple machineSlots = tile != null ? tile.containerSlots : FALLBACK_SLOTS;
        addSlot(new SlotBase(machineSlots, 0, 8, 35));
        addSlot(new SlotBase(machineSlots, 1, 152, 10));
        addSlot(new SlotBase(machineSlots, 2, 152, 55));

        addFullPlayerInventory(8, 79);

        widgetTankIn = addWidget(new WidgetFluidTank(this, tile != null ? tile.getTankIn() : null));
        widgetTankGasOut = addWidget(new WidgetFluidTank(this, tile != null ? tile.getTankGasOut() : null));
        widgetTankLiquidOut = addWidget(new WidgetFluidTank(this, tile != null ? tile.getTankLiquidOut() : null));
    }

    private static ItemHandlerSimple createFallbackSlots() {
        ItemHandlerSimple slots = new ItemHandlerSimple(3, 1);
        slots.setChecker((slot, stack) -> false);
        return slots;
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        if (id == NET_JEI_TRANSFER_BUCKETS && !isClient && tile != null) {

            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                int slot = buffer.readVarInt();
                Item bucket = BuiltInRegistries.ITEM.getValue(Identifier.parse(buffer.readUtf()));
                JeiTransferUtil.moveBucketToSlot(player.getInventory(), bucket, tile.containerSlots, slot);
            }
            return;
        }
        super.readMessage(id, buffer, isClient, ctx);
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
        ItemStack itemstack = ItemStack.EMPTY;
        net.minecraft.world.inventory.Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }
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
