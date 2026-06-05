/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;

public final class MessageVolumeBoxes implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessageVolumeBoxes> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:volume_boxes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageVolumeBoxes> STREAM_CODEC =
            StreamCodec.of(MessageVolumeBoxes::encode, MessageVolumeBoxes::decode);

    private final boolean fullSync;
    private final List<CompoundTag> fullTags;
    private final List<UUID> removedIds;
    private final List<CompoundTag> changedTags;

    private MessageVolumeBoxes(boolean fullSync, List<CompoundTag> fullTags,
            List<UUID> removedIds, List<CompoundTag> changedTags) {
        this.fullSync = fullSync;
        this.fullTags = fullTags;
        this.removedIds = removedIds;
        this.changedTags = changedTags;
    }

    public static MessageVolumeBoxes full(List<CompoundTag> tags) {
        return new MessageVolumeBoxes(true, List.copyOf(tags), List.of(), List.of());
    }

    public static MessageVolumeBoxes delta(List<UUID> removed, List<CompoundTag> changed) {
        return new MessageVolumeBoxes(false, List.of(), List.copyOf(removed), List.copyOf(changed));
    }

    public boolean isFullSync() {
        return fullSync;
    }

    public List<CompoundTag> fullTags() {
        return fullTags;
    }

    public List<UUID> removedIds() {
        return removedIds;
    }

    public List<CompoundTag> changedTags() {
        return changedTags;
    }

    private static void encode(RegistryFriendlyByteBuf buf, MessageVolumeBoxes msg) {
        buf.writeBoolean(msg.fullSync);
        if (msg.fullSync) {
            buf.writeShort(msg.fullTags.size());
            for (CompoundTag tag : msg.fullTags) {
                buf.writeNbt(tag);
            }
        } else {
            buf.writeShort(msg.removedIds.size());
            for (UUID id : msg.removedIds) {
                buf.writeUUID(id);
            }
            buf.writeShort(msg.changedTags.size());
            for (CompoundTag tag : msg.changedTags) {
                buf.writeNbt(tag);
            }
        }
    }

    private static MessageVolumeBoxes decode(RegistryFriendlyByteBuf buf) {
        boolean fullSync = buf.readBoolean();
        if (fullSync) {
            int count = BCPacketLimits.validateCount(buf.readShort(), BCPacketLimits.MAX_VOLUME_BOXES, "volume boxes");
            List<CompoundTag> tags = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                tags.add(buf.readNbt());
            }
            return MessageVolumeBoxes.full(tags);
        }
        int removedCount = BCPacketLimits.validateCount(buf.readShort(), BCPacketLimits.MAX_VOLUME_BOXES, "volume removed");
        List<UUID> removed = new ArrayList<>(removedCount);
        for (int i = 0; i < removedCount; i++) {
            removed.add(buf.readUUID());
        }
        int changedCount = BCPacketLimits.validateCount(buf.readShort(), BCPacketLimits.MAX_VOLUME_BOXES, "volume changed");
        List<CompoundTag> changed = new ArrayList<>(changedCount);
        for (int i = 0; i < changedCount; i++) {
            changed.add(buf.readNbt());
        }
        return MessageVolumeBoxes.delta(removed, changed);
    }

    @Override
    public Type<MessageVolumeBoxes> type() {
        return TYPE;
    }

    public static void handle(MessageVolumeBoxes message, BCPayloadContext ctx) {
        Level world = ctx.player().level();

        if (message.isFullSync()) {
            Set<UUID> previousIds = new HashSet<>();
            for (VolumeBox vb : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
                previousIds.add(vb.id);
            }

            List<VolumeBox> rebuilt = new ArrayList<>(message.fullTags().size());
            for (CompoundTag tag : message.fullTags()) {
                rebuilt.add(new VolumeBox(world, tag));
            }

            ClientVolumeBoxes.INSTANCE.volumeBoxes.clear();
            ClientVolumeBoxes.INSTANCE.volumeBoxes.addAll(rebuilt);

            for (VolumeBox vb : rebuilt) {
                if (!previousIds.contains(vb.id)) {
                    for (Addon addon : vb.addons.values()) {
                        if (addon != null) {
                            addon.onAdded();
                        }
                    }
                }
            }
            return;
        }

        Set<UUID> previousIds = new HashSet<>();
        for (VolumeBox vb : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
            previousIds.add(vb.id);
        }

        for (UUID removed : message.removedIds()) {
            ClientVolumeBoxes.INSTANCE.volumeBoxes.removeIf(vb -> vb.id.equals(removed));
        }

        for (CompoundTag tag : message.changedTags()) {
            VolumeBox updated = new VolumeBox(world, tag);
            ClientVolumeBoxes.INSTANCE.volumeBoxes.removeIf(vb -> vb.id.equals(updated.id));
            ClientVolumeBoxes.INSTANCE.volumeBoxes.add(updated);
            if (!previousIds.contains(updated.id)) {
                for (Addon addon : updated.addons.values()) {
                    if (addon != null) {
                        addon.onAdded();
                    }
                }
            }
        }
    }
}
