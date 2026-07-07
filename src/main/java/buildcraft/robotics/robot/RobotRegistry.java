/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.robot;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.BCLog;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.ResourceId;
import buildcraft.api.robots.RobotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import buildcraft.lib.compat.BcSavedDataType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;

public class RobotRegistry extends SavedData implements IRobotRegistry {
   public static final BcSavedDataType<RobotRegistry> TYPE = new BcSavedDataType<>(
      "buildcraftrobotics", "robot_registry",
      () -> new RobotRegistry(null),
      makeCodec(null),
      DataFixTypes.SAVED_DATA_MAP_DATA
   );

   public Level world;
   private final Map<StationIndex, DockingStation> stations = new HashMap<>();
   private long nextRobotID = Long.MIN_VALUE;
   private final Map<Long, EntityRobotBase> robotsLoaded = new HashMap<>();
   private final Map<ResourceId, Long> resourcesTaken = new HashMap<>();
   private final Map<Long, Set<ResourceId>> resourcesTakenByRobot = new HashMap<>();
   private final Map<Long, Set<StationIndex>> stationsTakenByRobot = new HashMap<>();
   private boolean stationsWorldBound;

   public RobotRegistry(@Nullable ServerLevel level) {
      this.world = level;
   }

   private static Codec<RobotRegistry> makeCodec(@Nullable ServerLevel level) {
      return CompoundTag.CODEC.flatXmap(tag -> {
         RobotRegistry data = new RobotRegistry(level);
         data.readFromNBT(tag);
         return DataResult.success(data);
      }, data -> {
         CompoundTag tag = new CompoundTag();
         data.writeToNBT(tag);
         return DataResult.success(tag);
      });
   }

   @Override
   public synchronized long getNextRobotId() {
      long result = this.nextRobotID;
      this.nextRobotID++;
      this.setDirty();
      return result;
   }

   @Override
   public synchronized void registerRobot(EntityRobotBase robot) {
      this.setDirty();
      if (robot.getRobotId() == EntityRobotBase.NULL_ROBOT_ID) {
         robot.setUniqueRobotId(this.getNextRobotId());
      }

      if (this.robotsLoaded.containsKey(robot.getRobotId())) {
         BCLog.logger.warn("Robot with id " + robot.getRobotId() + " was not unregistered properly");
      }

      this.robotsLoaded.put(robot.getRobotId(), robot);
   }

   @Override
   public synchronized void killRobot(EntityRobotBase robot) {
      this.setDirty();
      this.releaseResources(robot, true, false);
      this.robotsLoaded.remove(robot.getRobotId());
   }

   @Override
   public synchronized void unloadRobot(EntityRobotBase robot) {
      this.setDirty();
      this.releaseResources(robot, false, true);
      this.robotsLoaded.remove(robot.getRobotId());
   }

   @Override
   public synchronized EntityRobotBase getLoadedRobot(long id) {
      return this.robotsLoaded.get(id);
   }

   @Override
   public synchronized boolean isTaken(ResourceId resourceId) {
      return this.robotIdTaking(resourceId) != EntityRobotBase.NULL_ROBOT_ID;
   }

   @Override
   public synchronized long robotIdTaking(ResourceId resourceId) {
      if (!this.resourcesTaken.containsKey(resourceId)) {
         return EntityRobotBase.NULL_ROBOT_ID;
      }

      long robotId = this.resourcesTaken.get(resourceId);
      EntityRobotBase robot = this.robotsLoaded.get(robotId);
      if (robot != null && !robot.isRemoved()) {
         return robotId;
      } else {
         this.release(resourceId);
         return EntityRobotBase.NULL_ROBOT_ID;
      }
   }

   @Override
   public synchronized EntityRobotBase robotTaking(ResourceId resourceId) {
      long robotId = this.robotIdTaking(resourceId);
      return robotId == EntityRobotBase.NULL_ROBOT_ID ? null : this.robotsLoaded.get(robotId);
   }

   @Override
   public synchronized boolean take(ResourceId resourceId, EntityRobotBase robot) {
      this.setDirty();
      return this.take(resourceId, robot.getRobotId());
   }

   @Override
   public synchronized boolean take(ResourceId resourceId, long robotId) {
      if (resourceId == null) {
         return false;
      }

      this.setDirty();
      if (!this.resourcesTaken.containsKey(resourceId)) {
         this.resourcesTaken.put(resourceId, robotId);
         this.resourcesTakenByRobot.computeIfAbsent(robotId, k -> new HashSet<>()).add(resourceId);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public synchronized void release(ResourceId resourceId) {
      if (resourceId == null) {
         return;
      }

      this.setDirty();
      if (this.resourcesTaken.containsKey(resourceId)) {
         long robotId = this.resourcesTaken.get(resourceId);
         Set<ResourceId> taken = this.resourcesTakenByRobot.get(robotId);
         if (taken != null) {
            taken.remove(resourceId);
         }

         this.resourcesTaken.remove(resourceId);
      }
   }

   @Override
   public synchronized void releaseResources(EntityRobotBase robot) {
      this.releaseResources(robot, false, false);
   }

   private synchronized void releaseResources(EntityRobotBase robot, boolean forceAll, boolean resetEntities) {
      this.setDirty();
      long robotId = robot.getRobotId();
      Set<ResourceId> resourceSet = this.resourcesTakenByRobot.get(robotId);
      if (resourceSet != null) {
         for (ResourceId id : new HashSet<>(resourceSet)) {
            this.release(id);
         }

         this.resourcesTakenByRobot.remove(robotId);
      }

      Set<StationIndex> stationSet = this.stationsTakenByRobot.get(robotId);
      if (stationSet != null) {
         for (StationIndex s : new HashSet<>(stationSet)) {
            DockingStation d = this.stations.get(s);
            if (d != null) {
               if (!d.canRelease()) {
                  if (forceAll) {
                     d.unsafeRelease(robot);
                  } else if (resetEntities && d.robotIdTaking() == robotId) {
                     d.invalidateRobotTakingEntity();
                  }
               } else {
                  d.unsafeRelease(robot);
               }
            }
         }

         if (forceAll) {
            this.stationsTakenByRobot.remove(robotId);
         }
      }
   }

   @Override
   public synchronized DockingStation getStation(BlockPos pos, Direction side) {
      return this.stations.get(new StationIndex(pos, side));
   }

   @Override
   public synchronized Collection<DockingStation> getStations() {
      // Defensive copy: callers iterate this collection while methods invoked inside the loop -- station
      // validation on a DockingStationPipe, ghost-station cleanup -- can removeStation() from the live map.
      // Iterating the live values() view then throws ConcurrentModificationException (masked until now by the
      // earlier null-world NPE that aborted the search first).
      return new ArrayList<>(this.stations.values());
   }

   @Override
   public synchronized void registerStation(DockingStation station) {
      this.setDirty();
      StationIndex index = new StationIndex(station);
      if (this.stations.containsKey(index)) {
         BCLog.logger.warn("Station " + index + " already registered");
      } else {
         this.stations.put(index, station);
      }
   }

   @Override
   public synchronized void removeStation(DockingStation station) {
      this.setDirty();
      StationIndex index = new StationIndex(station);
      if (this.stations.containsKey(index)) {
         if (station.robotTaking() != null) {
            if (!station.isMainStation()) {
               station.robotTaking().undock();
            } else {
               station.robotTaking().setMainStation(null);
            }
         } else if (station.robotIdTaking() != EntityRobotBase.NULL_ROBOT_ID) {
            Set<StationIndex> taken = this.stationsTakenByRobot.get(station.robotIdTaking());
            if (taken != null) {
               taken.remove(index);
            }
         }

         this.stations.remove(index);
      }
   }

   @Override
   public synchronized void take(DockingStation station, long robotId) {
      this.setDirty();
      this.stationsTakenByRobot.computeIfAbsent(robotId, k -> new HashSet<>()).add(new StationIndex(station));
   }

   @Override
   public synchronized void release(DockingStation station, long robotId) {
      this.setDirty();
      Set<StationIndex> taken = this.stationsTakenByRobot.get(robotId);
      if (taken != null) {
         taken.remove(new StationIndex(station));
      }
   }

   @Override
   public synchronized void writeToNBT(CompoundTag nbt) {
      nbt.putLong("nextRobotID", this.nextRobotID);

      ListTag resourceList = new ListTag();
      for (Map.Entry<ResourceId, Long> e : this.resourcesTaken.entrySet()) {
         CompoundTag cpt = new CompoundTag();
         CompoundTag resourceId = new CompoundTag();
         e.getKey().writeToNBT(resourceId);
         cpt.put("resourceId", resourceId);
         cpt.putLong("robotId", e.getValue());
         resourceList.add(cpt);
      }

      nbt.put("resourceList", resourceList);

      ListTag stationList = new ListTag();
      for (Map.Entry<StationIndex, DockingStation> e : this.stations.entrySet()) {
         CompoundTag cpt = new CompoundTag();
         e.getValue().writeToNBT(cpt);
         String type = RobotManager.getDockingStationName(e.getValue().getClass());
         if (type != null) {
            cpt.putString("stationType", type);
         }

         stationList.add(cpt);
      }

      nbt.put("stationList", stationList);
   }

   @Override
   public synchronized void readFromNBT(CompoundTag nbt) {
      this.nextRobotID = BcNbt.getLong(nbt, "nextRobotID", Long.MIN_VALUE);

      ListTag resourceList = BcNbt.getList(nbt, "resourceList");
      for (int i = 0; i < resourceList.size(); i++) {
         if (resourceList.get(i) instanceof CompoundTag cpt) {
            ResourceId resourceId = ResourceId.load(BcNbt.getCompound(cpt, "resourceId"));
            long robotId = BcNbt.getLong(cpt, "robotId", 0L);
            this.take(resourceId, robotId);
         }
      }

      ListTag stationList = BcNbt.getList(nbt, "stationList");
      for (int i = 0; i < stationList.size(); i++) {
         if (!(stationList.get(i) instanceof CompoundTag cpt)) {
            continue;
         }

         Class<? extends DockingStation> cls = cpt.contains("stationType")
            ? RobotManager.getDockingStationByName(BcNbt.getString(cpt, "stationType", ""))
            : null;
         if (cls == null) {
            BCLog.logger.error("Could not load docking station of unknown type");
            continue;
         }

         try {
            DockingStation station = cls.getDeclaredConstructor().newInstance();
            station.world = this.world;
            station.readFromNBT(cpt);
            this.registerStation(station);
            if (station.linkedId() != EntityRobotBase.NULL_ROBOT_ID) {
               this.take(station, station.linkedId());
            }
         } catch (Exception e) {
            BCLog.logger.error("Could not load docking station", e);
         }
      }
   }

   @Override
   public void registryMarkDirty() {
      this.setDirty();
   }

   public List<DockingStation> getStationsSnapshot() {
      return new ArrayList<>(this.stations.values());
   }

   public static RobotRegistry get(Level world) {
      if (world.isClientSide()) {
         throw new UnsupportedOperationException("Attempted to get RobotRegistry on the client!");
      } else if (world instanceof ServerLevel) {
         RobotRegistry instance = TYPE.getOrCreate(world, () -> new RobotRegistry(null));
         instance.world = world;
         if (!instance.stationsWorldBound) {
            // Stations are decoded from the SavedData NBT before this level is attached, so loadStations() bound
            // them to a null world. Now that the ServerLevel is known, bind them once: otherwise every station
            // lookup (robotTaking / isTaken / take) NPEs on world.isClientSide() and any robot that searches for a
            // station -- deposit inventory, recharge, dock -- crashes its AI every tick after a world reload.
            for (DockingStation station : instance.stations.values()) {
               if (station.world == null) {
                  station.world = world;
               }
            }

            instance.stationsWorldBound = true;
         }

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
