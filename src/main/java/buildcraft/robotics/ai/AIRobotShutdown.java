package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.PathFinding;
import net.minecraft.core.BlockPos;

/** Out-of-power behaviour: undock, stop, and gently sink to the ground so the robot stays reachable. */
public class AIRobotShutdown extends AIRobotGoto {
   public AIRobotShutdown(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.robot.undock();
      this.robot.setItemActive(false);
      this.clearDestination(this.robot);
   }

   @Override
   public void update() {
      double feetY = this.robot.getY() - 0.1;
      BlockPos feet = BlockPos.containing(this.robot.getX(), feetY, this.robot.getZ());
      if (PathFinding.isSoftBlock(this.robot.level(), feet)) {
         this.robot.setPos(this.robot.getX(), this.robot.getY() - 0.075, this.robot.getZ());
      }
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }
}
