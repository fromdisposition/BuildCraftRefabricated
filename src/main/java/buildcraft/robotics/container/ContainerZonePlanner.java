/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.robotics.zone.ZonePlan;

@SuppressWarnings("this-escape")
public class ContainerZonePlanner extends ContainerBCTile<TileZonePlanner> {

    public ContainerZonePlanner(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getTile(playerInv, pos));
    }

    public ContainerZonePlanner(int containerId, Inventory playerInv, TileZonePlanner tile) {
        super(BCRoboticsMenuTypes.ZONE_PLANNER, containerId, playerInv.player, tile);

        addFullPlayerInventory(88, 146);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                addSlot(new SlotBase(tile.invPaintbrushes, x * 4 + y, 8 + x * 18, 146 + y * 18));
            }
        }

        addSlot(new SlotBase(tile.invInputPaintbrush, 0, 8, 125));
        addSlot(new SlotBase(tile.invInputMapLocation, 0, 26, 125));
        addSlot(new SlotOutput(tile.invInputResult, 0, 74, 125));

        addSlot(new SlotBase(tile.invOutputPaintbrush, 0, 233, 9));
        addSlot(new SlotBase(tile.invOutputMapLocation, 0, 233, 27));
        addSlot(new SlotOutput(tile.invOutputResult, 0, 233, 75));
    }

    public static final int NET_PAINT = 200;

    public static final int NET_REQUEST_LAYERS = 201;

    public static final int NET_LAYERS = 202;

    public void sendPaint(int layer, int x, int z, boolean set) {
        sendMessage(NET_PAINT, buf -> {
            buf.writeByte(layer);
            buf.writeVarInt(x);
            buf.writeVarInt(z);
            buf.writeBoolean(set);
        });
    }

    public void requestLayers() {
        sendMessage(NET_REQUEST_LAYERS, buf -> {});
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        if (id == NET_PAINT && !isClient) {
            int layer = buffer.readByte() & 0xFF;
            int x = buffer.readVarInt();
            int z = buffer.readVarInt();
            boolean set = buffer.readBoolean();
            if (tile != null) {
                tile.applyPaint(layer, x, z, set);
            }
            return;
        }
        if (id == NET_REQUEST_LAYERS && !isClient) {
            if (tile != null) {
                sendMessage(NET_LAYERS, buf -> {
                    for (ZonePlan plan : tile.layers) {
                        (plan == null ? new ZonePlan() : plan).writeToByteBuf(buf);
                    }
                });
            }
            return;
        }
        if (id == NET_LAYERS && isClient) {
            if (tile != null) {
                for (int i = 0; i < tile.layers.length; i++) {
                    ZonePlan plan = new ZonePlan();
                    plan.readFromByteBuf(buffer);
                    tile.layers[i] = plan;
                }
            }
            return;
        }
        super.readMessage(id, buffer, isClient, ctx);
    }

    private static TileZonePlanner getTile(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TileZonePlanner planner) {
                return planner;
            }
        }
        return null;
    }

    private static final int PLAYER_SLOTS_END = 36;
    private static final int MACHINE_SLOTS_END = 58;

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        net.minecraft.world.inventory.Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (slotIndex < PLAYER_SLOTS_END) {

                if (!this.moveItemStackTo(stack, PLAYER_SLOTS_END, MACHINE_SLOTS_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if (!this.moveItemStackTo(stack, 0, PLAYER_SLOTS_END, true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return result;
    }
}
