package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.world.phys.Vec3;

/** Base class for all movement AIs. Drives the destination-based mover on {@link EntityRobot}. */
public abstract class AIRobotGoto extends AIRobot {
   public AIRobotGoto(EntityRobotBase robot) {
      super(robot);
   }

   protected void setDestination(EntityRobotBase robot, double x, double y, double z) {
      if (robot instanceof EntityRobot entityRobot) {
         entityRobot.destination = new Vec3(x, y, z);
      }
   }

   protected void clearDestination(EntityRobotBase robot) {
      if (robot instanceof EntityRobot entityRobot) {
         entityRobot.destination = null;
         entityRobot.setDeltaMovement(Vec3.ZERO);
      }
   }
}
