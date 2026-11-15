/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.silicon.plug.PluggableGate;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import buildcraft.lib.compat.BcSavedDataType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import buildcraft.lib.nbt.BcProfiler;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;

public class SavedDataWireSystems extends SavedData {
   public static final BcSavedDataType<SavedDataWireSystems> TYPE = new BcSavedDataType<>(
      "buildcrafttransport", "wire_systems", () -> new SavedDataWireSystems(null), makeCodec(null), DataFixTypes.SAVED_DATA_MAP_DATA
   );
   public Level world;
   public final Map<WireSystem, Boolean> wireSystems = new HashMap<>();
   public boolean structureChanged = true;
   public final Set<WireSystem> changedSystems = new HashSet<>();
   private final Set<WireSystem> systemsNeedingPowerUpdate = new HashSet<>();
   private final Map<WireSystem.WireElement, Set<DyeColor>> emittedColours = new HashMap<>();
   private final Map<WireSystem.WireElement, List<WireSystem>> elementsToWireSystemsIndex = new HashMap<>();
   private final Map<ChunkPos, Set<WireSystem>> chunkToWireSystems = new HashMap<>();
   private final Set<Integer> removedNetworkIds = new HashSet<>();
   private final Set<BlockPos> pendingRebuilds = new HashSet<>();
   private final Map<UUID, Set<Integer>> sentTopology = new HashMap<>();

   public SavedDataWireSystems(@Nullable ServerLevel level) {
      this.world = level;
   }

   private static Codec<SavedDataWireSystems> makeCodec(@Nullable ServerLevel level) {
      return CompoundTag.CODEC.flatXmap(tag -> {
         SavedDataWireSystems data = new SavedDataWireSystems(level);
         data.readFromTag(tag);
         return DataResult.success(data);
      }, data -> DataResult.success(data.writeToTag()));
   }

   public void markStructureChanged() {
      this.structureChanged = true;
   }

   public void markEmitterDirty(WireSystem.WireElement emitter) {
      if (emitter.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
         this.recordEmittedColours(emitter);

         for (WireSystem wireSystem : this.getWireSystemsWithElementAsReadOnlyList(emitter)) {
            this.systemsNeedingPowerUpdate.add(wireSystem);
         }
      }
   }

   private void recordEmittedColours(WireSystem.WireElement element) {
      if (this.world.isLoaded(element.blockPos)) {
         IWireEmitter emitter = this.getEmitter(element);
         Set<DyeColor> colours = EnumSet.noneOf(DyeColor.class);
         if (emitter != null) {
            for (DyeColor colour : DyeColor.values()) {
               if (emitter.isEmitting(colour)) {
                  colours.add(colour);
               }
            }
         }

         if (colours.isEmpty()) {
            this.emittedColours.remove(element);
         } else {
            this.emittedColours.put(element, colours);
         }
      }
   }

   private void wakeGatesForWireSystem(WireSystem wireSystem) {
      Set<BlockPos> visited = new HashSet<>();

      for (WireSystem.WireElement element : wireSystem.elements) {
         BlockPos pos = element.blockPos;
         if (!visited.add(pos) || !this.world.isLoaded(pos) || !(this.world.getBlockEntity(pos) instanceof IPipeHolder holder)) {
            continue;
         }

         holder.wakePipe();

         for (Direction side : Direction.values()) {
            PipePluggable plug = holder.getPluggable(side);
            if (plug instanceof PluggableGate gate) {
               gate.logic.markResolveDirty();
            }
         }
      }
   }

   private void indexWireSystem(WireSystem wireSystem) {
      for (ChunkPos chunkPos : wireSystem.getChunkPoses()) {
         this.chunkToWireSystems.computeIfAbsent(chunkPos, unused -> new HashSet<>()).add(wireSystem);
      }
   }

   private void unindexWireSystem(WireSystem wireSystem) {
      for (ChunkPos chunkPos : wireSystem.getChunkPoses()) {
         this.chunkToWireSystems.computeIfPresent(chunkPos, (unused, systems) -> {
            systems.remove(wireSystem);
            return systems.isEmpty() ? null : systems;
         });
      }
   }

   private void rebuildChunkIndex() {
      this.chunkToWireSystems.clear();

      for (WireSystem wireSystem : this.wireSystems.keySet()) {
         this.indexWireSystem(wireSystem);
      }
   }

   public void sendTo(ServerPlayer player) {
      if (this.world instanceof ServerLevel serverLevel) {
         Set<WireSystem> watchedSystems = new HashSet<>();

         for (WireSystem wireSystem : this.wireSystems.keySet()) {
            if (wireSystem.isPlayerWatching(player)) {
               watchedSystems.add(wireSystem);
            }
         }

         PayloadWireSync payload = this.buildSyncPayload(watchedSystems, watchedSystems, this.wireSystems.keySet(), null);
         this.sentTopology.put(player.getUUID(), networkIds(watchedSystems));
         sendWireSync(player, payload);
      }
   }

   private Map<ServerPlayer, Set<WireSystem>> collectPlayersForSystems(ServerLevel serverLevel, Iterable<WireSystem> systems, boolean fullStructureSync) {
      if (fullStructureSync) {
         Map<ServerPlayer, Set<WireSystem>> playerToWires = new HashMap<>();

         for (Entry<ChunkPos, Set<WireSystem>> chunkEntry : this.chunkToWireSystems.entrySet()) {
            for (ServerPlayer player : PlayerLookup.tracking(serverLevel, chunkEntry.getKey().getWorldPosition())) {
               for (WireSystem wireSystem : chunkEntry.getValue()) {
                  if (wireSystem.isPlayerWatching(player)) {
                     playerToWires.computeIfAbsent(player, unused -> new HashSet<>()).add(wireSystem);
                  }
               }
            }
         }

         return playerToWires;
      } else {
         Map<ServerPlayer, Set<WireSystem>> playerToWires = new HashMap<>();

         for (WireSystem wireSystem : systems) {
            for (ChunkPos chunkPos : wireSystem.getChunkPoses()) {
               for (ServerPlayer player : PlayerLookup.tracking(serverLevel, chunkPos.getWorldPosition())) {
                  if (wireSystem.isPlayerWatching(player)) {
                     playerToWires.computeIfAbsent(player, unused -> new HashSet<>()).add(wireSystem);
                  }
               }
            }
         }

         return playerToWires;
      }
   }

   private static void sendWireSync(ServerPlayer player, PayloadWireSync payload) {
      for (PayloadWireSync packet : WireSyncSplitter.split(payload)) {
         if (packet.topology() != null || packet.powered() != null || packet.removedIds() != null) {
            BcPacketDistributor.sendToPlayer(player, packet);
         }
      }
   }

   private static Set<Integer> networkIds(Set<WireSystem> systems) {
      Set<Integer> ids = new HashSet<>();

      for (WireSystem wireSystem : systems) {
         ids.add(wireSystem.getNetworkId());
      }

      return ids;
   }

   private PayloadWireSync buildSyncPayload(Set<WireSystem> visible, Set<WireSystem> withTopology, Set<WireSystem> poweredSystems, int[] removed) {
      Map<Integer, WireSystem> topology = null;
      if (!withTopology.isEmpty()) {
         topology = new HashMap<>();

         for (WireSystem ws : withTopology) {
            topology.put(ws.getNetworkId(), ws);
         }
      }

      Map<Integer, Boolean> powered = null;
      if (poweredSystems != null && !poweredSystems.isEmpty()) {
         powered = new HashMap<>();

         for (WireSystem ws : visible) {
            if (poweredSystems.contains(ws)) {
               powered.put(ws.getNetworkId(), this.wireSystems.get(ws));
            }
         }

         if (powered.isEmpty()) {
            powered = null;
         }
      }

      return topology == null && powered == null && removed == null ? new PayloadWireSync(null, null, null) : new PayloadWireSync(topology, powered, removed);
   }

   public List<WireSystem> getWireSystemsWithElement(WireSystem.WireElement element) {
      List<WireSystem> wireSystemsWithElement = this.elementsToWireSystemsIndex.get(element);
      return wireSystemsWithElement != null ? new ArrayList<>(wireSystemsWithElement) : Collections.emptyList();
   }

   public List<WireSystem> getWireSystemsWithElementAsReadOnlyList(WireSystem.WireElement element) {
      return this.elementsToWireSystemsIndex.getOrDefault(element, Collections.emptyList());
   }

   public void removeWireSystem(WireSystem wireSystem) {
      this.removedNetworkIds.add(wireSystem.getNetworkId());
      this.unindexWireSystem(wireSystem);
      this.wireSystems.remove(wireSystem);
      wireSystem.elements.forEach(elementIn -> this.elementsToWireSystemsIndex.computeIfPresent(elementIn, (element, systems) -> {
         systems.remove(wireSystem);
         return systems.isEmpty() ? null : systems;
      }));
      this.markStructureChanged();
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
         this.indexWireSystem(wireSystem);
      }
   }

   public void buildAndAddWireSystem(WireSystem.WireElement element) {
      Set<WireSystem> replaced = new HashSet<>();
      WireSystem wireSystem = new WireSystem(this, element, replaced);
      Set<WireSystem.WireElement> taken = new HashSet<>(wireSystem.elements);

      for (WireSystem old : replaced) {
         this.removeWireSystem(old);
         WireSystem rest = remainderOf(old, taken);
         if (!rest.isEmpty()) {
            this.addWireSystem(rest, rest.update(this));
         }
      }

      if (!wireSystem.isEmpty()) {
         for (WireSystem.WireElement systemElement : wireSystem.elements) {
            if (systemElement.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
               this.recordEmittedColours(systemElement);
            }
         }

         this.addWireSystem(wireSystem, false);
         this.wireSystems.put(wireSystem, wireSystem.update(this));
      }

      this.markStructureChanged();
   }

   private static WireSystem remainderOf(WireSystem old, Set<WireSystem.WireElement> taken) {
      Set<BlockPos> wirePositions = new HashSet<>();

      for (WireSystem.WireElement element : old.elements) {
         if (element.type == WireSystem.WireElement.Type.WIRE_PART && !taken.contains(element)) {
            wirePositions.add(element.blockPos);
         }
      }

      return new WireSystem(
         old.elements
            .stream()
            .filter(element -> element.type == WireSystem.WireElement.Type.WIRE_PART ? !taken.contains(element) : wirePositions.contains(element.blockPos))
            .collect(ImmutableList.toImmutableList()),
         old.color
      );
   }

   public void rebuildWireSystemsAround(IPipeHolder holder) {
      if (!(holder.getWireManager() instanceof WireManager wireManager) || wireManager.parts.isEmpty()) {
         return;
      }

      wireManager.parts
         .keySet()
         .stream()
         .flatMap(part -> WireSystem.getConnectedElementsOfElement(this.world, new WireSystem.WireElement(holder.getPipePos(), part)).stream())
         .distinct()
         .forEach(this::buildAndAddWireSystem);
   }

   public void scheduleWireRebuild(IPipeHolder holder) {
      if (holder.getWireManager() instanceof WireManager wireManager && !wireManager.parts.isEmpty()) {
         this.pendingRebuilds.add(holder.getPipePos().immutable());
      }
   }

   private void flushPendingRebuilds() {
      if (this.pendingRebuilds.isEmpty()) {
         return;
      }

      List<BlockPos> toRebuild = new ArrayList<>(this.pendingRebuilds);
      this.pendingRebuilds.clear();

      for (BlockPos pos : toRebuild) {
         if (this.world != null && this.world.isLoaded(pos) && this.world.getBlockEntity(pos) instanceof IPipeHolder holder) {
            this.rebuildWireSystemsAround(holder);
         }
      }
   }

   public IWireEmitter getEmitter(WireSystem.WireElement element) {
      return element.type == WireSystem.WireElement.Type.EMITTER_SIDE
            && this.world.isLoaded(element.blockPos)
            && this.world.getBlockEntity(element.blockPos) instanceof IPipeHolder holder
            && holder.getPluggable(element.emitterSide) instanceof IWireEmitter emitter
         ? emitter
         : null;
   }

   public boolean isEmitterEmitting(WireSystem.WireElement element, DyeColor color) {
      if (this.world.isLoaded(element.blockPos)) {
         IWireEmitter emitter = this.getEmitter(element);
         return emitter != null && emitter.isEmitting(color);
      }

      Set<DyeColor> colours = this.emittedColours.get(element);
      return colours != null && colours.contains(color);
   }

   public void tick() {
      this.flushPendingRebuilds();
      if (this.structureChanged || !this.changedSystems.isEmpty() || !this.systemsNeedingPowerUpdate.isEmpty()) {
         BcProfiler.get().push("buildcraft:wire_sync");

         try {
            if (!this.systemsNeedingPowerUpdate.isEmpty()) {
               for (WireSystem wireSystem : this.systemsNeedingPowerUpdate) {
                  if (wireSystem.hasEmitters) {
                     Boolean oldPowered = this.wireSystems.get(wireSystem);
                     if (oldPowered != null) {
                        boolean newPowered = wireSystem.update(this);
                        if (oldPowered != newPowered) {
                           this.changedSystems.add(wireSystem);
                           this.wakeGatesForWireSystem(wireSystem);
                        }

                        this.wireSystems.put(wireSystem, newPowered);
                     }
                  }
               }

               this.systemsNeedingPowerUpdate.clear();
            }

            boolean needsSync = this.structureChanged || !this.changedSystems.isEmpty();
            if (needsSync && this.world instanceof ServerLevel serverLevel) {
               Iterable<WireSystem> systemsToSync = this.structureChanged ? this.wireSystems.keySet() : this.changedSystems;
               Map<ServerPlayer, Set<WireSystem>> playerToWires = this.collectPlayersForSystems(serverLevel, systemsToSync, this.structureChanged);
               int[] removed = null;
               if (this.structureChanged && !this.removedNetworkIds.isEmpty()) {
                  removed = this.removedNetworkIds.stream().mapToInt(Integer::intValue).toArray();
               }

               Set<WireSystem> poweredSystems = this.structureChanged ? this.wireSystems.keySet() : this.changedSystems;

               if (this.structureChanged) {
                  this.sentTopology.keySet().removeIf(id -> serverLevel.getPlayerByUUID(id) == null);
               }

               for (Entry<ServerPlayer, Set<WireSystem>> entry : playerToWires.entrySet()) {
                  Set<Integer> known = this.sentTopology.computeIfAbsent(entry.getKey().getUUID(), unused -> new HashSet<>());
                  Set<WireSystem> topology = new HashSet<>();

                  for (WireSystem wireSystem : entry.getValue()) {
                     if (this.structureChanged || !known.contains(wireSystem.getNetworkId())) {
                        topology.add(wireSystem);
                     }
                  }

                  if (removed != null) {
                     for (int id : removed) {
                        known.remove(id);
                     }
                  }

                  known.addAll(networkIds(topology));
                  PayloadWireSync payload = this.buildSyncPayload(entry.getValue(), topology, poweredSystems, removed);
                  sendWireSync(entry.getKey(), payload);
               }
            }

            if (this.structureChanged || !this.changedSystems.isEmpty()) {
               this.setDirty();
            }

            if (this.structureChanged) {
               this.removedNetworkIds.clear();
            }

            this.structureChanged = false;
            this.changedSystems.clear();
         } finally {
            BcProfiler.get().pop();
         }
      }
   }

   public CompoundTag writeToTag() {
      CompoundTag nbt = new CompoundTag();
      ListTag entriesList = new ListTag();
      this.wireSystems.forEach((wireSystem, powered) -> {
         CompoundTag entry = new CompoundTag();
         entry.put("wireSystem", wireSystem.writeToNBT());
         entry.putBoolean("powered", powered);
         entriesList.add(entry);
      });
      nbt.put("entries", entriesList);
      ListTag emittersList = new ListTag();
      this.emittedColours.forEach((element, colours) -> {
         if (this.elementsToWireSystemsIndex.containsKey(element)) {
            CompoundTag entry = new CompoundTag();
            entry.put("element", element.writeToNBT());
            entry.putIntArray("colours", colours.stream().mapToInt(DyeColor::getId).toArray());
            emittersList.add(entry);
         }
      });
      nbt.put("emitters", emittersList);
      return nbt;
   }

   public void readFromTag(CompoundTag nbt) {
      this.wireSystems.clear();
      this.elementsToWireSystemsIndex.clear();
      this.emittedColours.clear();
      ListTag entriesList = BcNbt.getList(nbt, "entries");

      for (int i = 0; i < entriesList.size(); i++) {
         if (entriesList.get(i) instanceof CompoundTag entry) {
            CompoundTag wsTag = BcNbt.getCompound(entry, "wireSystem");
            this.addWireSystem(new WireSystem(wsTag), BcNbt.getBoolean(entry, "powered", false));
         }
      }

      ListTag emittersList = BcNbt.getList(nbt, "emitters");

      for (int i = 0; i < emittersList.size(); i++) {
         if (emittersList.get(i) instanceof CompoundTag entry) {
            Set<DyeColor> colours = EnumSet.noneOf(DyeColor.class);

            for (int id : BcNbt.getIntArray(entry, "colours")) {
               colours.add(DyeColor.byId(id));
            }

            if (!colours.isEmpty()) {
               this.emittedColours.put(new WireSystem.WireElement(BcNbt.getCompound(entry, "element")), colours);
            }
         }
      }

      this.rebuildChunkIndex();
   }

   public static SavedDataWireSystems get(Level world) {
      if (world.isClientSide()) {
         throw new UnsupportedOperationException("Attempted to get SavedDataWireSystems on the client!");
      } else if (world instanceof ServerLevel) {
         SavedDataWireSystems instance = TYPE.getOrCreate(world, () -> new SavedDataWireSystems(null));
         instance.world = world;
         return instance;
      } else {
         throw new IllegalArgumentException("World is not a ServerLevel!");
      }
   }

   //? if < 1.21.10 {
   /*@Override
   public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
      return BcSavedDataType.encode(TYPE.codec, this, tag, provider);
   }
   *///?}
}
