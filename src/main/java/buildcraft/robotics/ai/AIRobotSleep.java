package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class AIRobotSleep extends AIRobot {
   public AIRobotSleep(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.robot.setItemActive(false);
   }

   @Override
   public void update() {
      DockingStation station = this.robot.getDockingStation();
      if (station != null && this.robot instanceof EntityRobot entityRobot) {
         BlockPos pos = station.getPos();
         entityRobot.destination = Vec3.atCenterOf(pos)
            .add(station.side().getStepX() * 0.5, station.side().getStepY() * 0.5, station.side().getStepZ() * 0.5);
      }
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }
}
