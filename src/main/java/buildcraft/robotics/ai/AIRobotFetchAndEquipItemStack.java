/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.statement.StationActions;
import net.minecraft.world.item.ItemStack;

public class AIRobotFetchAndEquipItemStack extends AIRobot {
   private IStackFilter filter;
   private int delay;

   public AIRobotFetchAndEquipItemStack(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotFetchAndEquipItemStack(EntityRobotBase robot, IStackFilter filter) {
      this(robot);
      this.filter = StationActions.getGateToolFilter(robot.getLinkedStation()).and(filter);
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotGotoStationToLoad(this.robot, this.filter, 1));
   }

   @Override
   public void update() {
      if (this.robot.getDockingStation() == null) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      if (this.delay++ > 40) {
         if (this.equipItemStack()) {
            this.terminate();
         } else {
            this.delay = 0;
            this.startDelegateAI(new AIRobotGotoStationToLoad(this.robot, this.filter, 1));
         }
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationToLoad) {
         if (this.filter == null) {
            this.abort();
         } else if (!ai.success()) {
            this.setSuccess(false);
            this.terminate();
         }
      }
   }

   private boolean equipItemStack() {
      ItemStack possible = AIRobotLoad.takeSingle(this.robot.getDockingStation(), this.filter, true);
      if (possible.isEmpty()) {
         return false;
      }

      this.robot.setItemInUse(possible);
      return true;
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 5L;
   }
}
