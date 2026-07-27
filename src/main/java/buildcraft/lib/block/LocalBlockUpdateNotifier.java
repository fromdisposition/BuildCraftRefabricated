/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LocalBlockUpdateNotifier {
   private static final Map<Level, LocalBlockUpdateNotifier> instanceMap = new WeakHashMap<>();
   private final Set<ILocalBlockUpdateSubscriber> subscriberSet = new HashSet<>();

   private LocalBlockUpdateNotifier(Level world) {
   }

   public static LocalBlockUpdateNotifier instance(Level world) {
      return instanceMap.computeIfAbsent(world, LocalBlockUpdateNotifier::new);
   }

   public void registerSubscriberForUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
      this.subscriberSet.add(subscriber);
   }

   public void removeSubscriberFromUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
      this.subscriberSet.remove(subscriber);
   }

   public void notifySubscribersInRange(Level world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags) {
      for (ILocalBlockUpdateSubscriber subscriber : this.subscriberSet) {
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
