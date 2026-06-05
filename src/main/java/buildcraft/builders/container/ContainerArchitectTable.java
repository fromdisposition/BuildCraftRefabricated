/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.gui.ContainerBCTile;

import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.tile.TileArchitectTable;

@SuppressWarnings("this-escape")
public class ContainerArchitectTable extends ContainerBCTile<TileArchitectTable> {
    public static final int NET_SET_NAME = 10;

    private static final int DATA_SCANNING = 0;
    private static final int DATA_PROGRESS = 1;
    private static final int DATA_TOTAL = 2;
    private static final int DATA_VALID = 3;
    private static final int DATA_COUNT = 4;

    private final ContainerData data;
    private final SnapshotContainer snapshotContainer;

    public ContainerArchitectTable(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getTile(playerInv, pos));
    }

    public ContainerArchitectTable(int containerId, Inventory playerInv, TileArchitectTable tile) {
        super(BCBuildersMenuTypes.ARCHITECT, containerId, playerInv.player, tile);

        if (tile != null && tile.getLevel() != null && !tile.getLevel().isClientSide()) {
            this.data = new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case DATA_SCANNING -> tile.isScanning() ? 1 : 0;
                        case DATA_PROGRESS -> tile.getScanProgress();
                        case DATA_TOTAL -> tile.getScanTotal();
                        case DATA_VALID -> tile.getIsValid() ? 1 : 0;
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {

                }

                @Override
                public int getCount() {
                    return DATA_COUNT;
                }
            };
        } else {
            this.data = new SimpleContainerData(DATA_COUNT);
        }

        addDataSlots(this.data);

        snapshotContainer = new SnapshotContainer(tile);
        addSlot(new SnapshotInputSlot(snapshotContainer, 0, 52, 125));
        addSlot(new SnapshotOutputSlot(snapshotContainer, 1, 111, 125));

        addFullPlayerInventory(8, 158, playerInv);
    }

    private static TileArchitectTable getTile(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TileArchitectTable architect) {
                return architect;
            }
        }
        return null;
    }

    public boolean getSyncedScanning() {
        return data.get(DATA_SCANNING) != 0;
    }

    public int getSyncedProgress() {
        return data.get(DATA_PROGRESS);
    }

    public int getSyncedTotal() {
        return data.get(DATA_TOTAL);
    }

    public boolean getSyncedValid() {
        return data.get(DATA_VALID) != 0;
    }

    public String getTileName() {
        return tile != null ? tile.name : "<unnamed>";
    }

    public void setTileName(String newName) {
        if (tile != null && tile.getLevel() != null) {
            tile.name = newName;
            if (!tile.getLevel().isClientSide()) {
                tile.setChanged();
            }
        }
    }

    @Override
    public void readMessage(int id, buildcraft.lib.net.PacketBufferBC buffer, boolean isClient, buildcraft.fabric.network.BCPayloadContext ctx) {
        if (id == NET_SET_NAME && !isClient) {
            String newName = buffer.readUtf();
            setTileName(newName);
            return;
        }
        super.readMessage(id, buffer, isClient, ctx);
    }

    private static class SnapshotInputSlot extends Slot {
        public SnapshotInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ItemSnapshot;
        }
    }

    private static class SnapshotOutputSlot extends Slot {
        public SnapshotOutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private static class SnapshotContainer implements Container {
        private final TileArchitectTable tile;

        SnapshotContainer(TileArchitectTable tile) {
            this.tile = tile;
        }

        @Override
        public int getContainerSize() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return (tile == null) || (tile.getSnapshotIn().isEmpty() && tile.getSnapshotOut().isEmpty());
        }

        @Override
        public ItemStack getItem(int slot) {
            if (tile == null) return ItemStack.EMPTY;
            return slot == 0 ? tile.getSnapshotIn() : tile.getSnapshotOut();
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            if (tile == null) return ItemStack.EMPTY;
            ItemStack current = getItem(slot);
            if (current.isEmpty()) return ItemStack.EMPTY;
            ItemStack result = current.split(count);
            if (current.isEmpty()) {
                setItem(slot, ItemStack.EMPTY);
            } else {
                setChanged();
            }
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (tile == null) return ItemStack.EMPTY;
            ItemStack current = getItem(slot);
            setItem(slot, ItemStack.EMPTY);
            return current;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (tile == null) return;
            if (slot == 0) {
                tile.setSnapshotIn(stack);
            } else {
                tile.setSnapshotOut(stack);
            }
        }

        @Override
        public void setChanged() {
            if (tile != null) tile.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            if (tile != null) {
                tile.setSnapshotIn(ItemStack.EMPTY);
                tile.setSnapshotOut(ItemStack.EMPTY);
            }
        }
    }
}
