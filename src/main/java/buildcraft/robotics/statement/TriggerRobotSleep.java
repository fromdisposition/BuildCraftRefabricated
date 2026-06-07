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
         if (station.robotTaking() != null) {
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
