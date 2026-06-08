/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IInjectable;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.lib.inventory.filter.ArrayStackOrListFilter;
import buildcraft.robotics.statement.StationActions;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class AIRobotUnload extends AIRobot {
   private int waitedCycles;

   public AIRobotUnload(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void update() {
      this.waitedCycles++;
      if (this.waitedCycles > 40) {
         if (unload(this.robot, this.robot.getDockingStation(), true)) {
            this.waitedCycles = 0;
         } else {
            this.setSuccess(!this.robot.containsItems());
            this.terminate();
         }
      }
   }

   public static boolean unload(EntityRobotBase robot, DockingStation station, boolean doUnload) {
      if (station == null || !(robot instanceof EntityRobot entityRobot)) {
         return false;
      }

      IInjectable output = station.getItemOutput();
      if (output == null) {
         return false;
      }

      Direction injectSide = station.getItemOutputSide().face;
      if (injectSide == null || !output.canInjectItems(injectSide)) {
         return false;
      }

      for (int slot = 0; slot < EntityRobot.NB_ITEMS_SLOTS; slot++) {
         ItemStack stack = entityRobot.getStackInSlot(slot);
         if (stack.isEmpty()) {
            continue;
         }

         if (!StationActions.canInteractWithItem(station, new ArrayStackOrListFilter(stack), StationActions.ACCEPT_ITEMS)) {
            continue;
         }

         ItemStack remaining = output.injectItem(stack.copy(), doUnload, injectSide, null, 0.0);
         int used = stack.getCount() - remaining.getCount();
         if (used > 0) {
            if (doUnload) {
               ItemStack left = stack.copy();
               left.shrink(used);
               entityRobot.setStackInSlot(slot, left.isEmpty() ? ItemStack.EMPTY : left);
            }

            return true;
         }
      }

      ItemStack held = robot.getHeldItem();
      if (!held.isEmpty()) {
         if (!StationActions.canInteractWithItem(station, new ArrayStackOrListFilter(held), StationActions.ACCEPT_ITEMS)) {
            return false;
         }

         ItemStack remaining = output.injectItem(held.copy(), doUnload, injectSide, null, 0.0);
         int used = held.getCount() - remaining.getCount();
         if (used > 0) {
            if (doUnload) {
               if (remaining.isEmpty()) {
                  robot.setItemInUse(ItemStack.EMPTY);
               } else {
                  robot.setItemInUse(remaining);
               }
            }

            return true;
         }
      }

      return false;
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ * 10L / 10L;
   }
}
