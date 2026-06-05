/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

import buildcraft.lib.fabric.PacketDistributor;

public class WorldSavedDataVolumeBoxes extends SavedData {
    private static final String DATA_NAME = "buildcraft_volume_boxes";

    public Level world;
    public final List<VolumeBox> volumeBoxes = new ArrayList<>();

    public static SavedDataType<WorldSavedDataVolumeBoxes> createType(Level world) {
        return new SavedDataType<>(

                Identifier.withDefaultNamespace(DATA_NAME),

                () -> new WorldSavedDataVolumeBoxes(world),
                buildCodec(world),
                net.minecraft.util.datafix.DataFixTypes.LEVEL
        );
    }

    private static Codec<WorldSavedDataVolumeBoxes> buildCodec(Level world) {
        return CompoundTag.CODEC.xmap(
                nbt -> fromNbt(nbt, world),
                WorldSavedDataVolumeBoxes::toNbt
        );
    }

    private static WorldSavedDataVolumeBoxes fromNbt(CompoundTag nbt, Level world) {
        WorldSavedDataVolumeBoxes instance = new WorldSavedDataVolumeBoxes(world);
        if (nbt.contains("volumeBoxes")) {
            ListTag listTag = nbt.getList("volumeBoxes").orElseGet(ListTag::new);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i).orElseGet(CompoundTag::new);
                instance.volumeBoxes.add(new VolumeBox(world, tag));
            }
        }
        return instance;
    }

    private static CompoundTag toNbt(WorldSavedDataVolumeBoxes data) {
        CompoundTag nbt = new CompoundTag();
        ListTag listTag = new ListTag();
        for (VolumeBox volumeBox : data.volumeBoxes) {
            listTag.add(volumeBox.writeToNBT());
        }
        nbt.put("volumeBoxes", listTag);
        return nbt;
    }

    public WorldSavedDataVolumeBoxes(Level world) {
        this.world = world;
    }

    public VolumeBox getVolumeBoxAt(BlockPos pos) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.box.contains(pos)).findFirst().orElse(null);
    }

    public void addVolumeBox(BlockPos pos) {
        VolumeBox box = new VolumeBox(world, pos);
        volumeBoxes.add(box);
        setDirty();
        broadcastDelta(Set.of(box.id), Set.of(), pos);
    }

    public void sendTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, createFullMessage());
    }

    public void markDirtyAndBroadcast() {
        setDirty();
        broadcastDelta(
                volumeBoxes.stream().map(vb -> vb.id).collect(java.util.stream.Collectors.toSet()),
                Set.of());
    }

    public void markDirtyAndBroadcast(VolumeBox box) {
        setDirty();
        broadcastDelta(Set.of(box.id), Set.of(), getTrackingPos(box));
    }

    public void markRemovedAndBroadcast(UUID id, BlockPos lastKnownPos) {
        volumeBoxes.removeIf(vb -> vb.id.equals(id));
        setDirty();
        broadcastDelta(Set.of(), Set.of(id), lastKnownPos);
    }

    private MessageVolumeBoxes createFullMessage() {
        List<CompoundTag> tags = volumeBoxes.stream().map(VolumeBox::writeToNBT).toList();
        return MessageVolumeBoxes.full(tags);
    }

    private void broadcastDelta(Set<UUID> updatedIds, Set<UUID> removedIds) {
        if (updatedIds.isEmpty() && removedIds.isEmpty()) {
            return;
        }
        BlockPos anchor = BlockPos.ZERO;
        if (!updatedIds.isEmpty()) {
            VolumeBox first = getVolumeBoxFromId(updatedIds.iterator().next());
            if (first != null) {
                anchor = getTrackingPos(first);
            }
        }
        broadcastDelta(updatedIds, removedIds, anchor);
    }

    private void broadcastDelta(Set<UUID> updatedIds, Set<UUID> removedIds, BlockPos trackingAnchor) {
        if (!(world instanceof ServerLevel sl)) {
            return;
        }
        if (updatedIds.isEmpty() && removedIds.isEmpty()) {
            return;
        }

        List<CompoundTag> changed = updatedIds.stream()
                .map(this::getVolumeBoxFromId)
                .filter(Objects::nonNull)
                .map(VolumeBox::writeToNBT)
                .toList();
        MessageVolumeBoxes message = MessageVolumeBoxes.delta(new ArrayList<>(removedIds), changed);

        Set<ServerPlayer> sent = new HashSet<>();
        if (!updatedIds.isEmpty()) {
            for (UUID id : updatedIds) {
                VolumeBox box = getVolumeBoxFromId(id);
                if (box != null) {
                    for (ServerPlayer player : PlayerLookup.tracking(sl, getTrackingPos(box))) {
                        if (sent.add(player)) {
                            PacketDistributor.sendToPlayer(player, message);
                        }
                    }
                }
            }
        }
        if (!removedIds.isEmpty()) {
            for (ServerPlayer player : PlayerLookup.tracking(sl, trackingAnchor)) {
                if (sent.add(player)) {
                    PacketDistributor.sendToPlayer(player, message);
                }
            }
        }
    }

    private static BlockPos getTrackingPos(VolumeBox volumeBox) {
        if (volumeBox.box.isInitialized()) {
            return volumeBox.box.min();
        }
        return BlockPos.ZERO;
    }

    public VolumeBox getVolumeBoxFromId(UUID id) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.id.equals(id)).findFirst().orElse(null);
    }

    public VolumeBox getCurrentEditing(Player player) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.isEditingBy(player)).findFirst().orElse(null);
    }

    public void tick() {
        boolean dirty = false;
        Set<UUID> tickUpdated = new HashSet<>();
        for (VolumeBox volumeBox : volumeBoxes) {
            if (volumeBox.isEditing()) {
                Player player = volumeBox.getPlayer(world);
                if (player == null) {
                    volumeBox.pauseEditing();
                    dirty = true;
                    tickUpdated.add(volumeBox.id);
                } else {
                    AABB oldAabb = volumeBox.box.getBoundingBox();
                    volumeBox.box.reset();
                    volumeBox.box.extendToEncompass(volumeBox.getHeld());
                    BlockPos lookingAt = BlockPos.containing(
                            player.position()
                                    .add(0, player.getEyeHeight(), 0)
                                    .add(player.getLookAngle().scale(volumeBox.getDist())));
                    volumeBox.box.extendToEncompass(lookingAt);
                    if (!volumeBox.box.getBoundingBox().equals(oldAabb)) {
                        dirty = true;
                        tickUpdated.add(volumeBox.id);
                    }
                }
            }

            if (!volumeBox.locks.isEmpty()) {
                boolean removed = volumeBox.locks.removeIf(lock -> !lock.cause.stillWorks(world));
                if (removed) {
                    dirty = true;
                    tickUpdated.add(volumeBox.id);
                }
            }
        }

        if (dirty) {
            setDirty();
            if (world instanceof ServerLevel sl) {
                Set<ServerPlayer> editors = new HashSet<>();
                for (UUID id : tickUpdated) {
                    VolumeBox volumeBox = getVolumeBoxFromId(id);
                    if (volumeBox != null && volumeBox.isEditing()) {
                        Player player = volumeBox.getPlayer(world);
                        if (player instanceof ServerPlayer serverPlayer) {
                            editors.add(serverPlayer);
                        }
                    }
                }
                List<CompoundTag> changed = tickUpdated.stream()
                        .map(this::getVolumeBoxFromId)
                        .filter(Objects::nonNull)
                        .map(VolumeBox::writeToNBT)
                        .toList();
                MessageVolumeBoxes message = MessageVolumeBoxes.delta(List.of(), changed);
                for (ServerPlayer editor : editors) {
                    PacketDistributor.sendToPlayer(editor, message);
                }
            }
        }
    }

    public static WorldSavedDataVolumeBoxes get(Level world) {
        if (world.isClientSide()) {
            throw new IllegalArgumentException("Tried to create a world saved data instance on the client!");
        }
        ServerLevel serverLevel = (ServerLevel) world;
        return serverLevel.getDataStorage().computeIfAbsent(createType(world));
    }
}
