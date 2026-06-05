/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.container;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.api.core.BCLog;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.nbt.NbtSquisher;
import buildcraft.lib.net.BCPacketLimits;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.tile.TileElectronicLibrary;

@SuppressWarnings("this-escape")
public class ContainerElectronicLibrary extends ContainerBCTile<TileElectronicLibrary> {

    public static final int NET_SELECTED = 1;

    public static final int NET_DOWNLOAD = 2;

    public static final int NET_UPLOAD_REQUEST = 3;

    public static final int NET_UPLOAD_DATA = 4;

    private static final int DATA_PROGRESS_DOWN = 0;
    private static final int DATA_PROGRESS_UP = 1;
    private static final int DATA_COUNT = 2;

    private final ContainerData data;

    private final List<byte[]> uploadChunks = new ArrayList<>();
    private final List<byte[]> downloadChunks = new ArrayList<>();

    public ContainerElectronicLibrary(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getTile(playerInv, pos));
    }

    public ContainerElectronicLibrary(int containerId, Inventory playerInv, TileElectronicLibrary tile) {
        super(BCBuildersMenuTypes.LIBRARY, containerId, playerInv.player, tile);

        if (tile.getLevel() != null && !tile.getLevel().isClientSide()) {
            this.data = new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case DATA_PROGRESS_DOWN -> tile.progressDown;
                        case DATA_PROGRESS_UP -> tile.progressUp;
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {}

                @Override
                public int getCount() {
                    return DATA_COUNT;
                }
            };
        } else {
            this.data = new SimpleContainerData(DATA_COUNT);
        }
        addDataSlots(this.data);

        addSlot(new SlotOutput(tile.invDownOut, 0, 175, 57));
        addSlot(new SlotBase(tile.invDownIn, 0, 219, 57));
        addSlot(new SlotBase(tile.invUpIn, 0, 175, 79));
        addSlot(new SlotOutput(tile.invUpOut, 0, 219, 79));

        addFullPlayerInventory(8, 138, playerInv);
    }

    private static TileElectronicLibrary getTile(Inventory playerInv, BlockPos pos) {
        var level = playerInv.player.level();
        if (level != null) {
            var be = level.getBlockEntity(pos);
            if (be instanceof TileElectronicLibrary lib) {
                return lib;
            }
        }
        return null;
    }

    public int getSyncedProgressDown() {
        return data.get(DATA_PROGRESS_DOWN);
    }

    public int getSyncedProgressUp() {
        return data.get(DATA_PROGRESS_UP);
    }

    public void sendSelectedToServer(Snapshot.Key selected) {
        sendMessage(NET_SELECTED, buf -> {
            buf.writeByte(selected != null ? 1 : 0);
            if (selected != null) {
                selected.writeToByteBuf(buf);
            }
        });
    }

    public void sendDownloadData(byte[] data) {
        if (data.length == 0) {
            sendMessage(NET_DOWNLOAD, buf -> {
                buf.writeByte(1);
                buf.writeByteArray(new byte[0]);
            });
            return;
        }

        int offset = 0;
        while (offset < data.length) {
            int end = Math.min(offset + BCPacketLimits.MAX_CHUNK_BYTES, data.length);
            final byte[] chunk = java.util.Arrays.copyOfRange(data, offset, end);
            final boolean last = (end >= data.length);
            sendMessage(NET_DOWNLOAD, buf -> {
                buf.writeByte(last ? 1 : 0);
                buf.writeByteArray(chunk);
            });
            offset = end;
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        super.readMessage(id, buffer, isClient, ctx);

        if (id == NET_SELECTED && !isClient) {
            tile.selected = (buffer.readByte() != 0) ? new Snapshot.Key(buffer) : null;
            return;
        }

        if (id == NET_DOWNLOAD && isClient) {
            boolean last = (buffer.readByte() != 0);
            byte[] chunk = readBoundedChunk(buffer);
            downloadChunks.add(chunk);
            if (last) {
                assembleDownload();
            }
            return;
        }

        if (id == NET_UPLOAD_REQUEST && isClient) {
            Snapshot.Key key = new Snapshot.Key(buffer);
            sendSnapshotToServer(key);
            return;
        }

        if (id == NET_UPLOAD_DATA && !isClient) {
            boolean last = (buffer.readByte() != 0);
            byte[] chunk = readBoundedChunk(buffer);
            uploadChunks.add(chunk);
            if (last) {
                assembleUpload();
            }
        }
    }

    private static byte[] readBoundedChunk(PacketBufferBC buffer) {
        byte[] chunk = buffer.readByteArray();
        BCPacketLimits.validateChunkSize(chunk.length);
        return chunk;
    }

    private void sendSnapshotToServer(Snapshot.Key key) {
        Snapshot snapshot = GlobalSavedDataSnapshots.get(GlobalSavedDataSnapshots.Side.CLIENT).getSnapshot(key);
        if (snapshot == null) {
            BCLog.logger.warn("[library] Upload requested for unknown snapshot key: " + key);
            return;
        }
        byte[] data;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtSquisher.squishVanilla(Snapshot.writeToNBT(snapshot), baos);
            data = baos.toByteArray();
        } catch (java.io.IOException e) {
            BCLog.logger.warn("[library] Failed to serialize snapshot for upload", e);
            return;
        }

        sendChunkedData(NET_UPLOAD_DATA, data);
    }

    private void sendChunkedData(int messageId, byte[] data) {
        if (data.length == 0) {
            sendMessage(messageId, buf -> {
                buf.writeByte(1);
                buf.writeByteArray(new byte[0]);
            });
            return;
        }

        int offset = 0;
        while (offset < data.length) {
            int end = Math.min(offset + BCPacketLimits.MAX_CHUNK_BYTES, data.length);
            final byte[] chunk = java.util.Arrays.copyOfRange(data, offset, end);
            final boolean last = (end >= data.length);
            sendMessage(messageId, buf -> {
                buf.writeByte(last ? 1 : 0);
                buf.writeByteArray(chunk);
            });
            offset = end;
        }
    }

    private void assembleDownload() {
        byte[] assembled = assembleChunks(downloadChunks);
        downloadChunks.clear();
        if (assembled == null) {
            return;
        }

        try {
            Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(new ByteArrayInputStream(assembled)));
            snapshot.computeKey();
            GlobalSavedDataSnapshots.get(GlobalSavedDataSnapshots.Side.CLIENT).addSnapshot(snapshot);
        } catch (java.io.IOException e) {
            BCLog.logger.warn("[library] Failed to deserialize downloaded snapshot", e);
        }
    }

    private void assembleUpload() {
        byte[] assembled = assembleChunks(uploadChunks);
        uploadChunks.clear();
        if (assembled == null) {
            return;
        }

        try {
            Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(new ByteArrayInputStream(assembled)));
            snapshot.computeKey();
            tile.onUploadReceived(snapshot);
        } catch (java.io.IOException e) {
            BCLog.logger.warn("[library] Failed to deserialize uploaded snapshot", e);
        }
    }

    private byte[] assembleChunks(List<byte[]> chunks) {
        int total = chunks.stream().mapToInt(c -> c.length).sum();
        try {
            BCPacketLimits.validateAssembledSize(total);
        } catch (IllegalArgumentException e) {
            BCLog.logger.warn("[library] Rejected oversized assembled snapshot: {} bytes", total);
            return null;
        }
        byte[] assembled = new byte[total];
        int pos = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, assembled, pos, chunk.length);
            pos += chunk.length;
        }
        return assembled;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
