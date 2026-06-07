package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.world.entity.Entity;

/** Pursues a target entity and strikes it with the robot's held weapon. */
public class AIRobotAttack extends AIRobot {
   private Entity target;
   private int delay = 10;

   public AIRobotAttack(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotAttack(EntityRobotBase robot, Entity target) {
      super(robot);
      this.target = target;
   }

   @Override
   public void preempt(AIRobot ai) {
      if (ai instanceof AIRobotGotoBlock && this.target != null && this.robot.distanceTo(this.target) <= 2.0F) {
         this.abortDelegateAI();
         this.robot.setItemActive(true);
      }
   }

   @Override
   public void update() {
      if (this.target == null || !this.target.isAlive()) {
         this.terminate();
         return;
      }

      if (this.robot.distanceTo(this.target) > 2.0F) {
         this.startDelegateAI(new AIRobotGotoBlock(this.robot,
            (int)Math.floor(this.target.getX()), (int)Math.floor(this.target.getY()), (int)Math.floor(this.target.getZ())));
         this.robot.setItemActive(false);
         return;
      }

      if (this.delay++ > 20) {
         this.delay = 0;
         if (this.robot instanceof EntityRobot entityRobot) {
            entityRobot.attackTargetEntityWithCurrentItem(this.target);
         }

         this.robot.aimItemAt(this.target.blockPosition());
      }
   }

   @Override
   public void end() {
      this.robot.setItemActive(false);
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoBlock) {
         if (!ai.success()) {
            this.robot.unreachableEntityDetected(this.target);
         }

         this.terminate();
      }
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ;
   }
}
