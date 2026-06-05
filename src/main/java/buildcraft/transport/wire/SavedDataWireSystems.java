/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;

import buildcraft.lib.fabric.PacketDistributor;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

import org.jspecify.annotations.Nullable;

public class SavedDataWireSystems extends SavedData {
    public static final SavedDataType<SavedDataWireSystems> TYPE = new SavedDataType<>(

        Identifier.withDefaultNamespace("buildcraft_wire_systems"),

        () -> new SavedDataWireSystems(null),
        makeCodec(null),
        DataFixTypes.SAVED_DATA_MAP_DATA
    );

    public Level world;
    public final Map<WireSystem, Boolean> wireSystems = new HashMap<>();
    public boolean gatesChanged = true;
    public boolean structureChanged = true;
    public final List<WireSystem> changedSystems = new ArrayList<>();
    public final List<ServerPlayer> changedPlayers = new ArrayList<>();
    public final Map<WireSystem.WireElement, IWireEmitter> emittersCache = new HashMap<>();

    private final Map<WireSystem.WireElement, List<WireSystem>> elementsToWireSystemsIndex = new HashMap<>();

    public SavedDataWireSystems(@Nullable ServerLevel level) {
        this.world = level;
    }

    private static Codec<SavedDataWireSystems> makeCodec(@Nullable ServerLevel level) {
        return CompoundTag.CODEC.flatXmap(
            tag -> {
                SavedDataWireSystems data = new SavedDataWireSystems(level);
                data.readFromTag(tag);
                return DataResult.success(data);
            },
            data -> DataResult.success(data.writeToTag())
        );
    }

    public void markStructureChanged() {
        structureChanged = true;
        gatesChanged = true;
        emittersCache.clear();
    }

    public List<WireSystem> getWireSystemsWithElement(WireSystem.WireElement element) {
        List<WireSystem> wireSystemsWithElement = this.elementsToWireSystemsIndex.get(element);
        return wireSystemsWithElement != null ? new ArrayList<>(wireSystemsWithElement) : Collections.emptyList();
    }

    public List<WireSystem> getWireSystemsWithElementAsReadOnlyList(WireSystem.WireElement element) {
        return this.elementsToWireSystemsIndex.getOrDefault(element, Collections.emptyList());
    }

    public void removeWireSystem(WireSystem wireSystem) {
        wireSystems.remove(wireSystem);
        wireSystem.elements.forEach(elementIn -> {
            elementsToWireSystemsIndex.computeIfPresent(elementIn, (element, systems) -> {
                systems.remove(wireSystem);
                return systems.isEmpty() ? null : systems;
            });
        });
        markStructureChanged();
    }

    public void addWireSystem(WireSystem wireSystem, boolean powered) {
        if (this.wireSystems.put(wireSystem, powered) == null) {
            wireSystem.elements.forEach(systemElement -> {
                List<WireSystem> wireSystemsWithElement = this.elementsToWireSystemsIndex.computeIfAbsent(systemElement, unused -> new ArrayList<>());
                if (wireSystemsWithElement.contains(wireSystem)) {
                    throw new IllegalStateException();
                }
                wireSystemsWithElement.add(wireSystem);
            });
        }
    }

    public void buildAndAddWireSystem(WireSystem.WireElement element) {
        WireSystem wireSystem = new WireSystem(this, element);
        if (!wireSystem.isEmpty()) {
            this.addWireSystem(wireSystem, false);
            wireSystems.put(wireSystem, wireSystem.update(this));
        }
        markStructureChanged();
    }

    public void rebuildWireSystemsAround(IPipeHolder holder) {
        Arrays.stream(EnumWirePart.values())
            .flatMap(part -> WireSystem.getConnectedElementsOfElement(world, new WireSystem.WireElement(holder.getPipePos(), part)).stream())
            .distinct()
            .forEach(this::buildAndAddWireSystem);
    }

    public IWireEmitter getEmitter(WireSystem.WireElement element) {
        if (element.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            if (!emittersCache.containsKey(element)) {
                BlockEntity tile = world.getBlockEntity(element.blockPos);
                if (tile instanceof IPipeHolder holder) {
                    PipePluggable plug = holder.getPluggable(element.emitterSide);
                    if (plug instanceof IWireEmitter emitter) {
                        emittersCache.put(element, emitter);
                    }
                }
            }
            return emittersCache.get(element);
        }
        return null;
    }

    public boolean isEmitterEmitting(WireSystem.WireElement element, DyeColor color) {
        BlockEntity tile = world.getBlockEntity(element.blockPos);
        if (tile instanceof IPipeHolder holder) {
            if (holder.getPluggable(element.emitterSide) instanceof IWireEmitter) {
                IWireEmitter emitter = getEmitter(element);
                return emitter != null && emitter.isEmitting(color);
            }
        }
        return false;
    }

    public void tick() {

        if (gatesChanged) {
            wireSystems.replaceAll((wireSystem, oldPowered) -> {
                boolean newPowered = wireSystem.update(this);
                if (oldPowered != newPowered) {
                    changedSystems.add(wireSystem);
                }
                return newPowered;
            });
        }

        boolean needsSync = structureChanged || !changedSystems.isEmpty() || !changedPlayers.isEmpty();
        if (needsSync && world instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.getPlayers(p -> true)) {
                if (structureChanged || changedPlayers.contains(player)) {
                    Map<Integer, WireSystem> changedWires = new HashMap<>();
                    for (WireSystem ws : wireSystems.keySet()) {
                        if (ws.isPlayerWatching(player)) {
                            changedWires.put(ws.getWiresHashCode(), ws);
                        }
                    }
                    if (!changedWires.isEmpty()) {
                        PacketDistributor.sendToPlayer(player, new PayloadWireSystems(changedWires));
                    }
                }

                if (!changedSystems.isEmpty() || structureChanged || changedPlayers.contains(player)) {
                    Map<Integer, Boolean> hashesPowered = new HashMap<>();
                    for (Map.Entry<WireSystem, Boolean> entry : wireSystems.entrySet()) {
                        WireSystem ws = entry.getKey();
                        if (ws.isPlayerWatching(player) &&
                            (structureChanged || changedSystems.contains(ws) || changedPlayers.contains(player))) {
                            hashesPowered.put(ws.getWiresHashCode(), entry.getValue());
                        }
                    }
                    if (!hashesPowered.isEmpty()) {
                        PacketDistributor.sendToPlayer(player, new PayloadWireSystemsPowered(hashesPowered));
                    }
                }
            }
        }

        if (structureChanged || !changedSystems.isEmpty()) {
            setDirty();
        }
        gatesChanged = false;
        structureChanged = false;
        changedSystems.clear();
        changedPlayers.clear();
    }

    public CompoundTag writeToTag() {
        CompoundTag nbt = new CompoundTag();
        ListTag entriesList = new ListTag();
        wireSystems.forEach((wireSystem, powered) -> {
            CompoundTag entry = new CompoundTag();
            entry.put("wireSystem", wireSystem.writeToNBT());
            entry.putBoolean("powered", powered);
            entriesList.add(entry);
        });
        nbt.put("entries", entriesList);
        return nbt;
    }

    public void readFromTag(CompoundTag nbt) {
        wireSystems.clear();
        this.elementsToWireSystemsIndex.clear();

        ListTag entriesList = nbt.getListOrEmpty("entries");
        for (int i = 0; i < entriesList.size(); i++) {
            Tag tag = entriesList.get(i);
            if (tag instanceof CompoundTag entry) {
                CompoundTag wsTag = entry.getCompound("wireSystem").orElse(new CompoundTag());
                this.addWireSystem(new WireSystem(wsTag), entry.getBooleanOr("powered", false));
            }
        }
    }

    public static SavedDataWireSystems get(Level world) {
        if (world.isClientSide()) {
            throw new UnsupportedOperationException("Attempted to get SavedDataWireSystems on the client!");
        }
        if (world instanceof ServerLevel serverLevel) {
            SavedDataWireSystems instance = serverLevel.getDataStorage().computeIfAbsent(TYPE);
            instance.world = world;
            return instance;
        }
        throw new IllegalArgumentException("World is not a ServerLevel!");
    }
}
