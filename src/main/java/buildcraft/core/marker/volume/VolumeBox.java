/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.data.Box;

public class VolumeBox {
    public final Level world;
    public UUID id;
    public Box box;
    private UUID player = null;
    private UUID oldPlayer = null;
    private BlockPos held = null;
    private double dist = 0;
    private BlockPos oldMin = null, oldMax = null;
    public final Map<EnumAddonSlot, Addon> addons = new EnumMap<>(EnumAddonSlot.class);
    public final List<Lock> locks = new ArrayList<>();

    public VolumeBox(Level world, BlockPos at) {
        if (world == null)
            throw new NullPointerException("world");
        this.world = world;
        id = UUID.randomUUID();
        box = new Box(at, at);
    }

    public VolumeBox(Level world, CompoundTag nbt) {
        if (world == null)
            throw new NullPointerException("world");
        this.world = world;

        String idStr = nbt.getString("id").orElse("");
        try {
            id = idStr.isEmpty() ? UUID.randomUUID() : UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            buildcraft.api.core.BCLog.logger.warn("[core.volume] Invalid volume box id '{}', generating new", idStr);
            id = UUID.randomUUID();
        }
        box = new Box();
        box.initialize(nbt.getCompound("box").orElseGet(CompoundTag::new));

        player = nbt.contains("player") ? UUID.fromString(nbt.getString("player").orElse("")) : null;
        oldPlayer = nbt.contains("oldPlayer") ? UUID.fromString(nbt.getString("oldPlayer").orElse("")) : null;

        if (nbt.contains("held")) {
            CompoundTag heldTag = nbt.getCompound("held").orElseGet(CompoundTag::new);
            held = new BlockPos(heldTag.getInt("X").orElse(0), heldTag.getInt("Y").orElse(0),
                    heldTag.getInt("Z").orElse(0));
        }
        dist = nbt.getDouble("dist").orElse(0.0);

        if (nbt.contains("oldMin")) {
            CompoundTag oldMinTag = nbt.getCompound("oldMin").orElseGet(CompoundTag::new);
            oldMin = new BlockPos(oldMinTag.getInt("X").orElse(0), oldMinTag.getInt("Y").orElse(0),
                    oldMinTag.getInt("Z").orElse(0));
        }
        if (nbt.contains("oldMax")) {
            CompoundTag oldMaxTag = nbt.getCompound("oldMax").orElseGet(CompoundTag::new);
            oldMax = new BlockPos(oldMaxTag.getInt("X").orElse(0), oldMaxTag.getInt("Y").orElse(0),
                    oldMaxTag.getInt("Z").orElse(0));
        }

        if (nbt.contains("addons")) {
            ListTag addonsList = nbt.getList("addons").orElseGet(ListTag::new);
            for (int i = 0; i < addonsList.size(); i++) {
                CompoundTag addonsEntryTag = addonsList.getCompound(i).orElseGet(CompoundTag::new);
                String addonClassName = addonsEntryTag.getString("addonClass").orElse("");
                try {
                    Class<? extends Addon> addonClass = AddonsRegistry.INSTANCE
                            .getClassByName(Identifier.parse(addonClassName));
                    Addon addon = addonClass.getDeclaredConstructor().newInstance();
                    addon.volumeBox = this;
                    addon.readFromNBT(addonsEntryTag.getCompound("addonData").orElseGet(CompoundTag::new));
                    String slotStr = addonsEntryTag.getString("slot").orElse("");
                    EnumAddonSlot slot = EnumAddonSlot.valueOf(slotStr);
                    addons.put(slot, addon);
                    addon.postReadFromNbt();
                } catch (Exception e) {
                    BCLog.logger.warn("[core.volume] Failed to load a volume box addon from NBT", e);
                }
            }
        }

        if (nbt.contains("locks")) {
            ListTag locksList = nbt.getList("locks").orElseGet(ListTag::new);
            for (int i = 0; i < locksList.size(); i++) {
                CompoundTag lockTag = locksList.getCompound(i).orElseGet(CompoundTag::new);
                try {
                    Lock lock = new Lock();
                    lock.readFromNBT(lockTag);
                    locks.add(lock);
                } catch (RuntimeException e) {
                    buildcraft.api.core.BCLog.logger.warn("[core.volume] Skipping invalid volume box lock entry", e);
                }
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isEditing() {
        return player != null;
    }

    private void resetEditing() {
        oldMin = oldMax = null;
        held = null;
        dist = 0;
    }

    public void cancelEditing() {
        player = null;
        box.reset();
        box.extendToEncompass(oldMin);
        box.extendToEncompass(oldMax);
        resetEditing();
    }

    public void confirmEditing() {
        player = null;
        resetEditing();
        addons.values().forEach(Addon::onVolumeBoxSizeChange);
    }

    @SuppressWarnings("WeakerAccess")
    public void pauseEditing() {
        oldPlayer = player;
        player = null;
    }

    public void resumeEditing() {
        player = oldPlayer;
        oldPlayer = null;
    }

    public void setPlayer(Player player) {
        this.player = player.getGameProfile().id();
    }

    public boolean isEditingBy(Player player) {
        return player != null && Objects.equals(this.player, player.getGameProfile().id());
    }

    public boolean isPausedEditingBy(Player player) {
        return oldPlayer != null && Objects.equals(oldPlayer, player.getGameProfile().id());
    }

    @SuppressWarnings("WeakerAccess")
    public Player getPlayer(Level world) {
        return world.getPlayerByUUID(player);
    }

    public void setHeldDistOldMinOldMax(BlockPos held, double dist, BlockPos oldMin, BlockPos oldMax) {
        this.held = held;
        this.dist = dist;
        this.oldMin = oldMin;
        this.oldMax = oldMax;
    }

    @SuppressWarnings("WeakerAccess")
    public BlockPos getHeld() {
        return held;
    }

    @SuppressWarnings("WeakerAccess")
    public double getDist() {
        return dist;
    }

    public Stream<Lock.Target> getLockTargetsStream() {
        return locks.stream().flatMap(lock -> lock.targets.stream());
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", id.toString());
        nbt.put("box", this.box.writeToNBT());

        if (player != null) {
            nbt.putString("player", player.toString());
        }
        if (oldPlayer != null) {
            nbt.putString("oldPlayer", oldPlayer.toString());
        }
        if (held != null) {
            CompoundTag heldTag = new CompoundTag();
            heldTag.putInt("X", held.getX());
            heldTag.putInt("Y", held.getY());
            heldTag.putInt("Z", held.getZ());
            nbt.put("held", heldTag);
        }
        nbt.putDouble("dist", dist);
        if (oldMin != null) {
            CompoundTag oldMinTag = new CompoundTag();
            oldMinTag.putInt("X", oldMin.getX());
            oldMinTag.putInt("Y", oldMin.getY());
            oldMinTag.putInt("Z", oldMin.getZ());
            nbt.put("oldMin", oldMinTag);
        }
        if (oldMax != null) {
            CompoundTag oldMaxTag = new CompoundTag();
            oldMaxTag.putInt("X", oldMax.getX());
            oldMaxTag.putInt("Y", oldMax.getY());
            oldMaxTag.putInt("Z", oldMax.getZ());
            nbt.put("oldMax", oldMaxTag);
        }

        ListTag addonsList = new ListTag();
        addons.entrySet().forEach(entry -> {
            CompoundTag addonsEntryTag = new CompoundTag();
            addonsEntryTag.putString("slot", entry.getKey().name());
            addonsEntryTag.putString("addonClass",
                    AddonsRegistry.INSTANCE.getNameByClass(entry.getValue().getClass()).toString());
            addonsEntryTag.put("addonData", entry.getValue().writeToNBT(new CompoundTag()));
            addonsList.add(addonsEntryTag);
        });
        nbt.put("addons", addonsList);

        ListTag locksList = new ListTag();
        for (Lock lock : locks) {
            locksList.add(lock.writeToNBT());
        }
        nbt.put("locks", locksList);
        return nbt;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && id.equals(((VolumeBox) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
