/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSection;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionEnd;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionStart;
import buildcraft.lib.integration.jei.JeiTransferUtil;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerSimple;

@SuppressWarnings("this-escape")
public class ContainerHeatExchange extends ContainerBC_Neptune {
    private static final ItemHandlerSimple FALLBACK_SLOTS = createFallbackSlots();

    @Nullable
    public final TileHeatExchange tile;

    public final WidgetFluidTank widgetTankStartInput;
    public final WidgetFluidTank widgetTankStartOutput;
    public final WidgetFluidTank widgetTankEndInput;
    public final WidgetFluidTank widgetTankEndOutput;

    public ContainerHeatExchange(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, resolveStartTile(MenuBlockEntityLookup.get(playerInv, pos, TileHeatExchange.class)));
    }

    public ContainerHeatExchange(int containerId, Inventory playerInv, @Nullable TileHeatExchange tile) {
        super(BCFactoryMenuTypes.HEAT_EXCHANGE, containerId, playerInv.player);
        this.tile = tile;

        ItemHandlerSimple machineSlots = tile != null ? tile.containerSlots : FALLBACK_SLOTS;
        addSlot(new SlotBase(machineSlots, 0, 8, 23));
        addSlot(new SlotBase(machineSlots, 1, 8, 64));
        addSlot(new SlotBase(machineSlots, 2, 152, 12));
        addSlot(new SlotBase(machineSlots, 3, 152, 54));

        addFullPlayerInventory(8, 89);

        ExchangeSectionStart start = startSection(tile);
        ExchangeSectionEnd end = start != null ? start.getEndSection() : null;

        widgetTankStartInput = addWidget(new WidgetFluidTank(this, start != null ? start.tankInput : null));
        widgetTankStartOutput = addWidget(new WidgetFluidTank(this, start != null ? start.tankOutput : null));
        widgetTankEndInput = addWidget(new WidgetFluidTank(this, end != null ? end.tankInput : null));
        widgetTankEndOutput = addWidget(new WidgetFluidTank(this, end != null ? end.tankOutput : null));
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

    @Nullable
    public ExchangeSectionStart startSection() {
        return startSection(tile);
    }

    @Nullable
    public ExchangeSectionEnd endSection() {
        ExchangeSectionStart start = startSection();
        return start != null ? start.getEndSection() : null;
    }

    @Nullable
    private static ExchangeSectionStart startSection(@Nullable TileHeatExchange tile) {
        if (tile == null) return null;
        ExchangeSection section = tile.getSection();
        return section instanceof ExchangeSectionStart s ? s : null;
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

            if (index < 4) {

                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
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
