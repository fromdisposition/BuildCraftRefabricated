/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotRunErrand;
import buildcraft.robotics.ai.StationErrand;

/**
 * The cycle for a board that only moves cargo between stations: fill up where something is offered, empty out
 * where something is accepted, rest when neither is possible. Subclasses say what the cargo is.
 */
public abstract class BoardRobotGenericHauler extends RedstoneBoardRobot {
   public BoardRobotGenericHauler(EntityRobotBase robot) {
      super(robot);
   }

   /** Whether the robot is currently loaded. */
   protected abstract boolean carrying();

   protected abstract StationErrand loadErrand();

   protected abstract StationErrand unloadErrand();

   @Override
   public final void update() {
      this.startDelegateAI(new AIRobotRunErrand(this.robot, this.carrying() ? this.unloadErrand() : this.loadErrand()));
   }

   @Override
   public final void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotRunErrand && !ai.success()) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }
}
