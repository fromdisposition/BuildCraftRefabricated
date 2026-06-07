package buildcraft.robotics.statement;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.robotics.BCRoboticsSprites;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.RobotUtils;
import java.util.List;

public class TriggerRobotLinked extends BCStatement implements ITriggerInternal {
   private final boolean reserved;

   public TriggerRobotLinked(boolean reserved) {
      super("buildcraft:robot." + (reserved ? "reserved" : "linked"));
      this.reserved = reserved;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.robot." + (this.reserved ? "reserved" : "linked"));
   }

   @Override
   public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
      IPipeHolder holder = RobotUtils.getPipeHolder(container);
      List<DockingStation> stations = RobotUtils.getStations(holder);

      for (DockingStation station : stations) {
         if (station.isTaken() && (this.reserved || station.isMainStation())) {
            return true;
         }
      }

      return false;
   }

   @Override
   public IStatement[] getPossible() {
      return BCRoboticsStatements.TRIGGER_ROBOT_LINKED_ALL;
   }

   @Override
   public ISprite getSprite() {
      return this.reserved ? BCRoboticsSprites.TRIGGER_ROBOT_RESERVED : BCRoboticsSprites.TRIGGER_ROBOT_LINKED;
   }
}
