/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;

import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.EnumContainerContentsMode;
import buildcraft.builders.snapshot.EnumFluidHandlingMode;
import buildcraft.builders.tile.TileBuilder;

@SuppressWarnings("this-escape")
public class ContainerBuilder extends ContainerBCTile<TileBuilder> {

    private static final int DATA_CAN_EXCAVATE = 0;
    private static final int DATA_SNAPSHOT_TYPE = 1;
    private static final int DATA_LEFT_TO_BREAK = 2;
    private static final int DATA_LEFT_TO_PLACE = 3;
    private static final int DATA_FLUID_MODE = 4;
    private static final int DATA_CONTENTS_MODE = 5;
    private static final int DATA_COUNT = 6;

    private static final int NET_DISPLAY_LIST = 10;

    private static final int NET_TANK_LEVELS = 11;

    public static final int NET_FLUID_MODE_CLICK = 12;

    public static final int NET_CONTENTS_MODE_CLICK = 13;

    private final ContainerData data;
    public final List<WidgetFluidTank> widgetTanks;

    private List<ItemStack> lastBroadcastDisplay = new ArrayList<>();

    private final List<ItemStack> syncedDisplay = new ArrayList<>();

    private final FluidSnapshot[] lastBroadcastTanks = new FluidSnapshot[TileBuilder.TANK_COUNT];

    private record FluidSnapshot(Identifier fluidId, int amount) {
        static final FluidSnapshot EMPTY = new FluidSnapshot(null, 0);
        boolean matches(FluidSnapshot other) {
            if (this == other) return true;
            if (amount != other.amount) return false;
            if (fluidId == null) return other.fluidId == null;
            return fluidId.equals(other.fluidId);
        }
    }

    public ContainerBuilder(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getTile(playerInv, pos));
    }

    public ContainerBuilder(int containerId, Inventory playerInv, TileBuilder tile) {
        super(BCBuildersMenuTypes.BUILDER, containerId, playerInv.player, tile);

        if (tile != null && tile.getLevel() != null && !tile.getLevel().isClientSide()) {
            this.data = new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case DATA_CAN_EXCAVATE -> tile.canExcavate() ? 1 : 0;
                        case DATA_SNAPSHOT_TYPE -> tile.snapshotType == null ? -1 : tile.snapshotType.ordinal();
                        case DATA_LEFT_TO_BREAK -> tile.getBuilder() != null ? tile.getBuilder().leftToBreak : 0;
                        case DATA_LEFT_TO_PLACE -> tile.getBuilder() != null ? tile.getBuilder().leftToPlace : 0;
                        case DATA_FLUID_MODE -> tile.getFluidMode().ordinal();
                        case DATA_CONTENTS_MODE -> tile.getContainerContentsMode().ordinal();
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

        BuilderContainer slotContainer = new BuilderContainer(tile);

        addSlot(new SnapshotInputSlot(slotContainer, 0, 80, 27));

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlot(new Slot(slotContainer, 1 + sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
            }
        }

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
                addSlot(new SlotDisplay(this::getDisplay, x + y * 4, 179 + x * 18, 18 + y * 18));
            }
        }

        List<WidgetFluidTank> widgets = new ArrayList<>(4);
        if (tile != null) {
            for (int i = 0; i < TileBuilder.TANK_COUNT; i++) {
                widgets.add(addWidget(new WidgetFluidTank(this, tile.getTank(i))));
            }
        }
        widgetTanks = List.copyOf(widgets);

        addFullPlayerInventory(8, 140, playerInv);
    }

    private static TileBuilder getTile(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TileBuilder builder) {
                return builder;
            }
        }
        return null;
    }

    private ItemStack getDisplay(int index) {

        if (tile != null && tile.getLevel() != null && tile.getLevel().isClientSide()) {
            return index >= 0 && index < syncedDisplay.size() ? syncedDisplay.get(index) : ItemStack.EMPTY;
        }

        if (tile == null || tile.snapshotType != EnumSnapshotType.BLUEPRINT) return ItemStack.EMPTY;
        List<ItemStack> live = tile.blueprintBuilder.remainingDisplayRequired;
        return index >= 0 && index < live.size() ? live.get(index) : ItemStack.EMPTY;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (tile == null || tile.getLevel() == null || tile.getLevel().isClientSide()) return;

        broadcastDisplayList();
        broadcastTankLevels();
    }

    private void broadcastDisplayList() {

        List<ItemStack> current = tile.snapshotType == EnumSnapshotType.BLUEPRINT
            && tile.blueprintBuilder != null
            ? tile.blueprintBuilder.remainingDisplayRequired
            : List.of();
        if (displayListEquals(current, lastBroadcastDisplay)) return;

        List<ItemStack> snap = new ArrayList<>(current.size());
        for (ItemStack s : current) snap.add(s.copy());
        lastBroadcastDisplay = snap;

        var registries = tile.getLevel().registryAccess();
        sendMessage(NET_DISPLAY_LIST, buf -> {
            RegistryFriendlyByteBuf rbuf = new RegistryFriendlyByteBuf(buf, registries);
            rbuf.writeVarInt(snap.size());
            for (ItemStack stack : snap) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(rbuf, stack);
            }
        });
    }

    private void broadcastTankLevels() {

        FluidSnapshot[] current = new FluidSnapshot[TileBuilder.TANK_COUNT];
        boolean changed = false;
        for (int i = 0; i < TileBuilder.TANK_COUNT; i++) {
            FluidStacksResourceHandler t = tile.getTank(i);
            FluidResource res = t.getResource(0);
            int amount = (int) t.getAmountAsLong(0);
            if (res.isEmpty() || amount == 0) {
                current[i] = FluidSnapshot.EMPTY;
            } else {
                current[i] = new FluidSnapshot(
                    BuiltInRegistries.FLUID.getKey(res.getFluid()),
                    amount
                );
            }
            FluidSnapshot last = lastBroadcastTanks[i];
            if (last == null || !last.matches(current[i])) {
                changed = true;
            }
        }
        if (!changed) return;

        for (int i = 0; i < TileBuilder.TANK_COUNT; i++) {
            lastBroadcastTanks[i] = current[i];
        }

        sendMessage(NET_TANK_LEVELS, buf -> {
            for (int i = 0; i < TileBuilder.TANK_COUNT; i++) {
                FluidSnapshot fs = current[i];
                if (fs.fluidId == null || fs.amount <= 0) {
                    buf.writeUtf("");
                    buf.writeVarInt(0);
                } else {
                    buf.writeUtf(fs.fluidId.toString());
                    buf.writeVarInt(fs.amount);
                }
            }
        });
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        if (id == NET_FLUID_MODE_CLICK && !isClient) {
            if (tile != null) {
                tile.cycleFluidMode();
            }
            return;
        }
        if (id == NET_CONTENTS_MODE_CLICK && !isClient) {
            if (tile != null) {
                tile.cycleContainerContentsMode();
            }
            return;
        }
        if (id == NET_DISPLAY_LIST && isClient) {
            RegistryFriendlyByteBuf rbuf = new RegistryFriendlyByteBuf(buffer, ctx.player().level().registryAccess());
            int count = rbuf.readVarInt();
            List<ItemStack> newList = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(rbuf);
                if (!stack.isEmpty()) newList.add(stack);
            }
            syncedDisplay.clear();
            syncedDisplay.addAll(newList);
            return;
        }
        if (id == NET_TANK_LEVELS && isClient) {

            for (int i = 0; i < TileBuilder.TANK_COUNT; i++) {
                String fluidIdStr = buffer.readUtf();
                int amount = buffer.readVarInt();
                if (tile == null) continue;
                FluidStacksResourceHandler handler = tile.getTank(i);
                if (handler == null) continue;
                applyTankState(handler, fluidIdStr, amount);
            }
            return;
        }
        super.readMessage(id, buffer, isClient, ctx);
    }

    private static void applyTankState(FluidStacksResourceHandler handler, String fluidIdStr, int amount) {
        try (Transaction tx = Transaction.openRoot()) {
            FluidResource existing = handler.getResource(0);
            if (!existing.isEmpty()) {
                handler.extract(0, existing, Integer.MAX_VALUE, tx);
            }
            if (!fluidIdStr.isEmpty() && amount > 0) {
                Identifier fluidId = Identifier.tryParse(fluidIdStr);
                if (fluidId != null) {
                    Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId);
                    if (fluid != null && fluid != Fluids.EMPTY) {
                        handler.insert(0, FluidResource.of(fluid), amount, tx);
                    }
                }
            }
            tx.commit();
        }
    }

    private static boolean displayListEquals(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            ItemStack sa = a.get(i);
            ItemStack sb = b.get(i);
            if (!ItemStack.isSameItemSameComponents(sa, sb) || sa.getCount() != sb.getCount()) {
                return false;
            }
        }
        return true;
    }

    public boolean getSyncedCanExcavate() {
        return data.get(DATA_CAN_EXCAVATE) != 0;
    }

    public int getSyncedSnapshotType() {
        return data.get(DATA_SNAPSHOT_TYPE);
    }

    public int getSyncedLeftToBreak() {
        return data.get(DATA_LEFT_TO_BREAK);
    }

    public int getSyncedLeftToPlace() {
        return data.get(DATA_LEFT_TO_PLACE);
    }

    public EnumFluidHandlingMode getSyncedFluidMode() {
        return EnumFluidHandlingMode.fromOrdinal(data.get(DATA_FLUID_MODE));
    }

    public EnumContainerContentsMode getSyncedContentsMode() {
        return EnumContainerContentsMode.fromOrdinal(data.get(DATA_CONTENTS_MODE));
    }

    private static class SnapshotInputSlot extends Slot {
        public SnapshotInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!(stack.getItem() instanceof ItemSnapshot item)) return false;

            return item.isUsed();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static class BuilderContainer implements Container {
        private final TileBuilder tile;

        BuilderContainer(TileBuilder tile) {
            this.tile = tile;
        }

        @Override
        public int getContainerSize() {
            return 1 + TileBuilder.RESOURCE_SLOTS;
        }

        @Override
        public boolean isEmpty() {
            if (tile == null) return true;
            if (!tile.getSnapshot().isEmpty()) return false;
            for (int i = 0; i < TileBuilder.RESOURCE_SLOTS; i++) {
                if (!tile.getResource(i).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (tile == null) return ItemStack.EMPTY;
            if (slot == 0) return tile.getSnapshot();
            return tile.getResource(slot - 1);
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

                setItem(slot, current);
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
                tile.setSnapshot(stack);
            } else {
                tile.setResource(slot - 1, stack);
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
            if (tile == null) return;
            tile.setSnapshot(ItemStack.EMPTY);
            for (int i = 0; i < TileBuilder.RESOURCE_SLOTS; i++) {
                tile.setResource(i, ItemStack.EMPTY);
            }
        }
    }
}
