package buildcraft.robotics.statement;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.RobotUtils;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RobotsActionProvider implements IActionProvider {
   @Override
   public void addInternalActions(Collection<IActionInternal> actions, IStatementContainer container) {
      IPipeHolder holder = RobotUtils.getPipeHolder(container);
      if (holder == null) {
         return;
      }

      List<DockingStation> stations = RobotUtils.getStations(holder);
      if (stations.isEmpty()) {
         return;
      }

      actions.add(BCRoboticsStatements.ACTION_ROBOT_WAKEUP);

      IPipe pipe = holder.getPipe();
      boolean itemPipe = pipe != null && pipe.getFlow() instanceof IFlowItems;
      boolean fluidPipe = pipe != null && pipe.getFlow() instanceof IFlowFluid;

      actions.add(BCRoboticsStatements.ACTION_ROBOT_FILTER);
      actions.add(BCRoboticsStatements.ACTION_ROBOT_WORK_IN_AREA);
      actions.add(BCRoboticsStatements.ACTION_ROBOT_LOAD_UNLOAD_AREA);

      if (itemPipe) {
         actions.add(BCRoboticsStatements.ACTION_STATION_REQUEST_ITEMS);
         actions.add(BCRoboticsStatements.ACTION_STATION_ACCEPT_ITEMS);
      }

      if (fluidPipe) {
         actions.add(BCRoboticsStatements.ACTION_STATION_ACCEPT_FLUIDS);
      }

      for (DockingStation station : stations) {
         if (station.getItemInput() != null) {
            actions.add(BCRoboticsStatements.ACTION_STATION_PROVIDE_ITEMS);
         }

         if (station.getFluidInput() != null) {
            actions.add(BCRoboticsStatements.ACTION_STATION_PROVIDE_FLUIDS);
         }

         if (station.getRequestProvider() != null) {
            actions.add(BCRoboticsStatements.ACTION_STATION_MACHINE_REQUEST);
         }
      }
   }

   @Override
   public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, @Nonnull Direction side) {
   }

   @Override
   public void addExternalActions(Collection<IActionExternal> actions, @Nonnull Direction side, BlockEntity tile) {
   }
}
