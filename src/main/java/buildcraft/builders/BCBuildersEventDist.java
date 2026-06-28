/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.misc.AdvancementUtil;
import com.mojang.authlib.GameProfile;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public enum BCBuildersEventDist {
   INSTANCE;

   private final Map<Level, Deque<WeakReference<TileQuarry>>> allQuarries = new WeakHashMap<>();
   private final Map<Level, Deque<WeakReference<TileFiller>>> allFillers = new WeakHashMap<>();
   private final Map<Level, Deque<WeakReference<TileArchitectTable>>> allArchitectTables = new WeakHashMap<>();
   private final Map<Level, Deque<WeakReference<TileBuilder>>> allBuilders = new WeakHashMap<>();
   public static final Identifier DESTROYING_THE_WORLD = Identifier.parse("buildcraftbuilders:destroying_the_world");
   public static final long FULL_SPEED_WINDOW_TICKS = 40L;
   static final int SCAN_INTERVAL_TICKS = 20;
   private long serverTickCounter = 0L;

   // Live per-level collections, read by the client-only BCBuildersWorldRenderer. Returning the backing
   // deque (nullable) lets the renderer prune dead weak refs in place. Kept here so the render layer holds
   // no server-side state and this common class names no client types.
   public Deque<WeakReference<TileQuarry>> renderQuarries(Level level) {
      return this.allQuarries.get(level);
   }

   public Deque<WeakReference<TileArchitectTable>> renderArchitectTables(Level level) {
      return this.allArchitectTables.get(level);
   }

   public Deque<WeakReference<TileFiller>> renderFillers(Level level) {
      return this.allFillers.get(level);
   }

   public Deque<WeakReference<TileBuilder>> renderBuilders(Level level) {
      return this.allBuilders.get(level);
   }

   public synchronized void validateArchitectTable(TileArchitectTable table) {
      Deque<WeakReference<TileArchitectTable>> tables = this.allArchitectTables.computeIfAbsent(table.getLevel(), k -> new LinkedList<>());
      tables.add(new WeakReference<>(table));
   }

   public synchronized void invalidateArchitectTable(TileArchitectTable table) {
      Deque<WeakReference<TileArchitectTable>> tables = this.allArchitectTables.get(table.getLevel());
      if (tables != null) {
         Iterator<WeakReference<TileArchitectTable>> iter = tables.iterator();

         while (iter.hasNext()) {
            WeakReference<TileArchitectTable> ref = iter.next();
            TileArchitectTable t = ref.get();
            if (t == null || t == table) {
               iter.remove();
            }
         }
      }
   }

   public synchronized void validateBuilder(TileBuilder builder) {
      Deque<WeakReference<TileBuilder>> builders = this.allBuilders.computeIfAbsent(builder.getLevel(), k -> new LinkedList<>());
      builders.add(new WeakReference<>(builder));
   }

   public synchronized void invalidateBuilder(TileBuilder builder) {
      Deque<WeakReference<TileBuilder>> builders = this.allBuilders.get(builder.getLevel());
      if (builders != null) {
         Iterator<WeakReference<TileBuilder>> iter = builders.iterator();

         while (iter.hasNext()) {
            WeakReference<TileBuilder> ref = iter.next();
            TileBuilder b = ref.get();
            if (b == null || b == builder) {
               iter.remove();
            }
         }
      }
   }

   public synchronized void validateQuarry(TileQuarry quarry) {
      Deque<WeakReference<TileQuarry>> quarries = this.allQuarries.computeIfAbsent(quarry.getLevel(), k -> new LinkedList<>());
      quarries.add(new WeakReference<>(quarry));
   }

   public synchronized void invalidateQuarry(TileQuarry quarry) {
      Deque<WeakReference<TileQuarry>> quarries = this.allQuarries.get(quarry.getLevel());
      if (quarries != null) {
         Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();

         while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry pos = ref.get();
            if (pos == null || pos == quarry) {
               iter.remove();
            }
         }
      }
   }

   public synchronized Set<UUID> findOwnersToAward(Level level, long currentTick) {
      Set<UUID> winners = new HashSet<>();
      if (level != null && !level.isClientSide()) {
         Deque<WeakReference<TileQuarry>> quarries = this.allQuarries.get(level);
         if (quarries != null && quarries.size() >= 2) {
            Map<UUID, Integer> countByOwner = new HashMap<>();
            Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();

            while (iter.hasNext()) {
               TileQuarry q = iter.next().get();
               if (q == null || q.isRemoved()) {
                  iter.remove();
               } else if (q.frameBox.isInitialized()) {
                  int sizeX = q.frameBox.max().getX() - q.frameBox.min().getX() + 1;
                  int sizeZ = q.frameBox.max().getZ() - q.frameBox.min().getZ() + 1;
                  if (sizeX >= 64 && sizeZ >= 64) {
                     long lastFullSpeed = q.getLastFullSpeedTick();
                     if (lastFullSpeed != Long.MIN_VALUE && currentTick - lastFullSpeed <= 40L) {
                        GameProfile owner = q.getOwner();
                        if (owner != null && BcAuth.id(owner) != null) {
                           int next = countByOwner.getOrDefault(BcAuth.id(owner), 0) + 1;
                           countByOwner.put(BcAuth.id(owner), next);
                           if (next >= 2) {
                              winners.add(BcAuth.id(owner));
                           }
                        }
                     }
                  }
               }
            }

            return winners;
         } else {
            return winners;
         }
      } else {
         return winners;
      }
   }

   public synchronized void onServerTick() {
      this.serverTickCounter++;
      if (this.serverTickCounter % 20L == 0L) {
         for (Entry<Level, Deque<WeakReference<TileQuarry>>> entry : this.allQuarries.entrySet()) {
            Level level = entry.getKey();
            Deque<WeakReference<TileQuarry>> quarries = entry.getValue();
            if (quarries != null && quarries.size() >= 2) {
               long now = level.getGameTime();

               for (UUID winner : this.findOwnersToAward(level, now)) {
                  AdvancementUtil.unlockAdvancement(winner, level, DESTROYING_THE_WORLD);
               }
            }
         }
      }
   }

   public synchronized void validateFiller(TileFiller filler) {
      Deque<WeakReference<TileFiller>> fillers = this.allFillers.computeIfAbsent(filler.getLevel(), k -> new LinkedList<>());
      fillers.add(new WeakReference<>(filler));
   }

   public synchronized void invalidateFiller(TileFiller filler) {
      Deque<WeakReference<TileFiller>> fillers = this.allFillers.get(filler.getLevel());
      if (fillers != null) {
         Iterator<WeakReference<TileFiller>> iter = fillers.iterator();

         while (iter.hasNext()) {
            WeakReference<TileFiller> ref = iter.next();
            TileFiller f = ref.get();
            if (f == null || f == filler) {
               iter.remove();
            }
         }
      }
   }
}
