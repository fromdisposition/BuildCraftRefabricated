package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/** Tries to unload the robot's items at a station; if none is reachable, drops them into the world. */
public class AIRobotDisposeItems extends AIRobot {
   public AIRobotDisposeItems(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotGotoStationAndUnload(this.robot));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationAndUnload) {
         if (ai.success() && this.robot.containsItems()) {
            this.startDelegateAI(new AIRobotGotoStationAndUnload(this.robot));
         } else if (!ai.success()) {
            this.dropAll();
            this.terminate();
         } else {
            this.terminate();
         }
      }
   }

   private void dropAll() {
      if (this.robot instanceof EntityRobot entityRobot) {
         for (int slot = 0; slot < EntityRobot.NB_ITEMS_SLOTS; slot++) {
            ItemStack stack = entityRobot.getStackInSlot(slot);
            if (!stack.isEmpty()) {
               ItemEntity entity = new ItemEntity(entityRobot.level(), entityRobot.getX(), entityRobot.getY(), entityRobot.getZ(), stack.copy());
               entityRobot.level().addFreshEntity(entity);
               entityRobot.setStackInSlot(slot, ItemStack.EMPTY);
            }
         }
      }
   }
}
