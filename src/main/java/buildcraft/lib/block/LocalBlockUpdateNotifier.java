/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LocalBlockUpdateNotifier {
   // Weak keys so an unloaded level is collected, synchronized because the integrated server and the client
   // thread both resolve notifiers; a plain WeakHashMap corrupts (or spins) under concurrent writes.
   private static final Map<Level, LocalBlockUpdateNotifier> instanceMap = Collections.synchronizedMap(new WeakHashMap<>());
   // Copy-on-write: setLevelUpdated runs arbitrary tile logic that may place or break blocks, re-entering
   // dispatch on this very list, or register/remove a subscriber. Iterating a plain Set there throws
   // ConcurrentModificationException. Writes are rare (tile load/unload), iteration is per block change.
   private final CopyOnWriteArrayList<ILocalBlockUpdateSubscriber> subscribers = new CopyOnWriteArrayList<>();

   private LocalBlockUpdateNotifier(Level world) {
   }

   public static LocalBlockUpdateNotifier instance(Level world) {
      return instanceMap.computeIfAbsent(world, LocalBlockUpdateNotifier::new);
   }

   public void registerSubscriberForUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
      this.subscribers.addIfAbsent(subscriber);
   }

   public void removeSubscriberFromUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
      this.subscribers.remove(subscriber);
   }

   public void notifySubscribersInRange(Level world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags) {
      for (ILocalBlockUpdateSubscriber subscriber : this.subscribers) {
         BlockPos keyPos = subscriber.getSubscriberPos();
         int updateRange = subscriber.getUpdateRange();
         if (Math.abs(keyPos.getX() - eventPos.getX()) <= updateRange
            && Math.abs(keyPos.getY() - eventPos.getY()) <= updateRange
            && Math.abs(keyPos.getZ() - eventPos.getZ()) <= updateRange) {
            subscriber.setLevelUpdated(world, eventPos, oldState, newState, flags);
         }
      }
   }

   public static void registerBreakListener() {
   }

   public static void onLevelBlockStateChanged(Level level, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      if (!level.isClientSide()) {
         dispatch(level, pos, oldState, newState, flags);
      }
   }

   private static void dispatch(Level level, BlockPos pos, BlockState oldState, BlockState newState) {
      dispatch(level, pos, oldState, newState, 0);
   }

   private static void dispatch(Level level, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      LocalBlockUpdateNotifier notifier = instanceMap.get(level);
      if (notifier != null) {
         notifier.notifySubscribersInRange(level, pos, oldState, newState, flags);
      }
   }
}
