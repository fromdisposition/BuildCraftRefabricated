/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.statement;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.robotics.BCRoboticsSprites;
import buildcraft.robotics.RobotUtils;
import buildcraft.robotics.ai.AIRobotSleep;
import buildcraft.robotics.entity.EntityRobot;
import java.util.List;

public class TriggerRobotSleep extends BCStatement implements ITriggerInternal {
   public TriggerRobotSleep() {
      super("buildcraft:robot.sleep");
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.robot.sleep");
   }

   @Override
   public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
      IPipeHolder holder = RobotUtils.getPipeHolder(container);
      List<DockingStation> stations = RobotUtils.getStations(holder);

      for (DockingStation station : stations) {
         // "A robot is linked here" is what the Station Linked trigger already reports. This one is about the
         // robot having run out of work, which is only visible in what its AI is actually doing.
         if (station.robotTaking() instanceof EntityRobot robot
            && robot.getMainAI() != null
            && robot.getMainAI().getActiveAI() instanceof AIRobotSleep) {
            return true;
         }
      }

      return false;
   }

   @Override
   public ISprite getSprite() {
      return BCRoboticsSprites.TRIGGER_ROBOT_SLEEP;
   }
}
