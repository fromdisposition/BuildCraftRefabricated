/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.silicon.plug.PluggableGate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

public class SavedDataWireSystems extends SavedData {
   public static final SavedDataType<SavedDataWireSystems> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath("buildcrafttransport", "wire_systems"), () -> new SavedDataWireSystems(null), makeCodec(null), DataFixTypes.SAVED_DATA_MAP_DATA
   );
   public Level world;
   public final Map<WireSystem, Boolean> wireSystems = new HashMap<>();
   public boolean structureChanged = true;
   public final Set<WireSystem> changedSystems = new HashSet<>();
   private final Set<WireSystem> systemsNeedingPowerUpdate = new HashSet<>();
   public final Map<WireSystem.WireElement, IWireEmitter> emittersCache = new HashMap<>();
   private final Map<WireSystem.WireElement, List<WireSystem>> elementsToWireSystemsIndex = new HashMap<>();
   private final Map<ChunkPos, Set<WireSystem>> chunkToWireSystems = new HashMap<>();
   private final Set<Integer> removedNetworkIds = new HashSet<>();

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
      this.emittersCache.clear();
   }

   public void markEmitterDirty(WireSystem.WireElement emitter) {
      if (emitter.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
         for (WireSystem wireSystem : this.getWireSystemsWithElementAsReadOnlyList(emitter)) {
            this.systemsNeedingPowerUpdate.add(wireSystem);
         }
      }
   }

   public void markSystemPowerDirty(WireSystem wireSystem) {
      this.systemsNeedingPowerUpdate.add(wireSystem);
   }

   private void wakeGatesForWireSystem(WireSystem wireSystem) {
      Set<BlockPos> visited = new HashSet<>();

      for (WireSystem.WireElement element : wireSystem.elements) {
         BlockPos pos = element.blockPos;
         if (!visited.add(pos) || !(this.world.getBlockEntity(pos) instanceof IPipeHolder holder)) {
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

         PayloadWireSync payload = this.buildSyncPayload(watchedSystems, true, this.wireSystems.keySet(), null);
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

   private PayloadWireSync buildSyncPayload(Set<WireSystem> visible, boolean sendTopology, Set<WireSystem> poweredSystems, int[] removed) {
      Map<Integer, WireSystem> topology = null;
      if (sendTopology && !visible.isEmpty()) {
         topology = new HashMap<>();

         for (WireSystem ws : visible) {
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
      WireSystem wireSystem = new WireSystem(this, element);
      if (!wireSystem.isEmpty()) {
         this.addWireSystem(wireSystem, false);
         this.wireSystems.put(wireSystem, wireSystem.update(this));
      }

      this.markStructureChanged();
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

   public IWireEmitter getEmitter(WireSystem.WireElement element) {
      if (element.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
         if (!this.emittersCache.containsKey(element)
            && this.world.getBlockEntity(element.blockPos) instanceof IPipeHolder holder
            && holder.getPluggable(element.emitterSide) instanceof IWireEmitter emitter) {
            this.emittersCache.put(element, emitter);
         }

         return this.emittersCache.get(element);
      } else {
         return null;
      }
   }

   public boolean isEmitterEmitting(WireSystem.WireElement element, DyeColor color) {
      IWireEmitter emitter = this.getEmitter(element);
      return emitter != null && emitter.isEmitting(color);
   }

   public void tick() {
      if (this.structureChanged || !this.changedSystems.isEmpty() || !this.systemsNeedingPowerUpdate.isEmpty()) {
         Profiler.get().push("buildcraft:wire_sync");

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

               for (Entry<ServerPlayer, Set<WireSystem>> entry : playerToWires.entrySet()) {
                  PayloadWireSync payload = this.buildSyncPayload(entry.getValue(), this.structureChanged, poweredSystems, removed);
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
            Profiler.get().pop();
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
      return nbt;
   }

   public void readFromTag(CompoundTag nbt) {
      this.wireSystems.clear();
      this.elementsToWireSystemsIndex.clear();
      ListTag entriesList = nbt.getListOrEmpty("entries");

      for (int i = 0; i < entriesList.size(); i++) {
         if (entriesList.get(i) instanceof CompoundTag entry) {
            CompoundTag wsTag = entry.getCompound("wireSystem").orElse(new CompoundTag());
            this.addWireSystem(new WireSystem(wsTag), entry.getBooleanOr("powered", false));
         }
      }

      this.rebuildChunkIndex();
   }

   public static SavedDataWireSystems get(Level world) {
      if (world.isClientSide()) {
         throw new UnsupportedOperationException("Attempted to get SavedDataWireSystems on the client!");
      } else if (world instanceof ServerLevel serverLevel) {
         SavedDataWireSystems instance = (SavedDataWireSystems)serverLevel.getDataStorage().computeIfAbsent(TYPE);
         instance.world = world;
         return instance;
      } else {
         throw new IllegalArgumentException("World is not a ServerLevel!");
      }
   }
}
