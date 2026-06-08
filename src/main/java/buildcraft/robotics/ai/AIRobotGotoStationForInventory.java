/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

public abstract class AIRobotGotoStationForInventory extends AIRobot {
   private final boolean beginOnUpdate;

   protected AIRobotGotoStationForInventory(EntityRobotBase robot, boolean beginOnUpdate) {
      super(robot);
      this.beginOnUpdate = beginOnUpdate;
   }

   protected abstract IStationFilter createStationFilter();

   @Override
   public void start() {
      if (!this.beginOnUpdate) {
         this.beginSearch();
      }
   }

   @Override
   public void update() {
      if (this.beginOnUpdate) {
         this.beginSearch();
      }
   }

   private void beginSearch() {
      this.startDelegateAI(new AIRobotSearchAndGotoStation(this.robot, this.createStationFilter(), this.robot.getZoneToLoadUnload()));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoStation) {
         this.setSuccess(ai.success());
         this.terminate();
      }
   }
}
