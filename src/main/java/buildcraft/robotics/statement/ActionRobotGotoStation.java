package buildcraft.robotics.statement;

import buildcraft.api.items.IMapLocation;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.robotics.BCRoboticsSprites;
import buildcraft.robotics.RobotUtils;
import buildcraft.robotics.ai.AIRobotGoAndLinkToDock;
import buildcraft.robotics.ai.AIRobotMain;
import buildcraft.robotics.entity.EntityRobot;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class ActionRobotGotoStation extends ActionStation {
   public ActionRobotGotoStation() {
      super("buildcraft:robot.goto_station", "gate.action.robot.goto_station", BCRoboticsSprites.ACTION_ROBOT_GOTO_STATION, 1, true);
   }

   @Override
   public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
      IRobotRegistry registry = RobotManager.registryProvider.getRegistry(container.getTile().getLevel());
      List<DockingStation> stations = RobotUtils.getStations(RobotUtils.getPipeHolder(container));

      for (DockingStation station : stations) {
         if (station.robotTaking() == null || !(station.robotTaking() instanceof EntityRobot robot)) {
            continue;
         }

         AIRobot overriding = robot.getMainAI() instanceof AIRobotMain main ? main.getOverridingAI() : null;
         if (overriding != null) {
            continue;
         }

         DockingStation target = station;
         if (parameters.length > 0 && parameters[0] instanceof StatementParameterItemStack stackParam) {
            DockingStation mapped = this.getStation(stackParam, registry);
            if (mapped != null) {
               target = mapped;
            }
         }

         robot.overrideAI(new AIRobotGoAndLinkToDock(robot, target));
      }
   }

   private DockingStation getStation(StatementParameterItemStack stackParam, IRobotRegistry registry) {
      ItemStack item = stackParam.getItemStack();
      if (item.isEmpty() || !(item.getItem() instanceof IMapLocation map)) {
         return null;
      }

      BlockPos index = map.getPoint(item);
      if (index == null) {
         return null;
      }

      Direction side = map.getPointSide(item);
      return registry.getStation(index, side);
   }
}
