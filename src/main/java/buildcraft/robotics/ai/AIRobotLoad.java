/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.statement.StationActions;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

public class AIRobotLoad extends AIRobot {
   public static final int ANY_QUANTITY = -1;

   private IStackFilter filter;
   private int quantity;
   private int waitedCycles;

   public AIRobotLoad(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
      super(robot);
      this.filter = filter;
      this.quantity = quantity;
   }

   @Override
   public void update() {
      if (this.filter == null) {
         this.terminate();
         return;
      }

      this.waitedCycles++;
      if (this.waitedCycles > 40) {
         this.setSuccess(load(this.robot, this.robot.getDockingStation(), this.filter, this.quantity, true));
         this.terminate();
      }
   }

   public static boolean load(EntityRobotBase robot, DockingStation station, IStackFilter filter, int quantity, boolean doLoad) {
      if (station == null || !(robot instanceof EntityRobot entityRobot)) {
         return false;
      }

      Container input = station.getItemInput();
      if (input == null) {
         return false;
      }

      if (!StationActions.canInteractWithItem(station, filter, StationActions.PROVIDE_ITEMS)) {
         return false;
      }

      Direction face = station.getItemInputSide().face;
      WorldlyContainer sided = input instanceof WorldlyContainer wc ? wc : null;
      int[] slots = slotsFor(input, sided, face);
      int loaded = 0;

      for (int slot : slots) {
         ItemStack stack = input.getItem(slot);
         if (stack.isEmpty() || !filter.matches(stack)) {
            continue;
         }

         if (sided != null && face != null && !sided.canTakeItemThroughFace(slot, stack, face)) {
            continue;
         }

         if (!StationActions.canExtractItem(station, stack)) {
            continue;
         }

         int want = quantity == ANY_QUANTITY ? stack.getCount() : Math.min(stack.getCount(), quantity - loaded);
         if (want <= 0) {
            continue;
         }

         ItemStack toAdd = stack.copy();
         toAdd.setCount(want);
         // The simulate path must never touch the robot's inventory: station search probes EVERY candidate
         // station with doLoad=false, and mutating here put the items into the robot while the chest kept its
         // stack -- a straight dupe (a stock Carrier robot minted every matching stack per search cycle).
         int moved;
         if (doLoad) {
            ItemStack remaining = entityRobot.receiveItem(null, toAdd);
            moved = want - remaining.getCount();
         } else {
            moved = entityRobot.roomFor(toAdd);
         }

         if (moved > 0) {
            if (doLoad) {
               input.removeItem(slot, moved);
               input.setChanged();
            }

            loaded += moved;
            if (quantity == ANY_QUANTITY) {
               return true;
            }

            if (quantity - loaded <= 0) {
               return true;
            }
         }
      }

      return loaded > 0;
   }

   
   public static ItemStack takeSingle(DockingStation station, IStackFilter filter, boolean doTake) {
      if (station == null) {
         return ItemStack.EMPTY;
      }

      Container input = station.getItemInput();
      if (input == null || !StationActions.canInteractWithItem(station, filter, StationActions.PROVIDE_ITEMS)) {
         return ItemStack.EMPTY;
      }

      Direction face = station.getItemInputSide().face;
      WorldlyContainer sided = input instanceof WorldlyContainer wc ? wc : null;
      int[] slots = slotsFor(input, sided, face);

      for (int slot : slots) {
         ItemStack stack = input.getItem(slot);
         if (stack.isEmpty() || !filter.matches(stack)) {
            continue;
         }

         if (sided != null && face != null && !sided.canTakeItemThroughFace(slot, stack, face)) {
            continue;
         }

         if (!StationActions.canExtractItem(station, stack)) {
            continue;
         }

         ItemStack single = stack.copy();
         single.setCount(1);
         if (doTake) {
            input.removeItem(slot, 1);
            input.setChanged();
         }

         return single;
      }

      return ItemStack.EMPTY;
   }

   private static int[] slotsFor(Container input, WorldlyContainer sided, Direction face) {
      if (sided != null && face != null) {
         return sided.getSlotsForFace(face);
      }

      int[] all = new int[input.getContainerSize()];
      for (int i = 0; i < all.length; i++) {
         all[i] = i;
      }

      return all;
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ * 8L / 10L;
   }
}
