package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotAttack;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraft.robotics.ai.AIRobotSearchEntity;
import buildcraft.robotics.path.IEntityFilter;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;

public class BoardRobotKnight extends RedstoneBoardRobot {
   public BoardRobotKnight(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("knight");
   }

   @Override
   public final void update() {
      if (this.robot.getHeldItem().isEmpty()) {
         this.startDelegateAI(new AIRobotFetchAndEquipItemStack(this.robot, (IStackFilter)stack -> stack.is(ItemTags.SWORDS)));
      } else if (this.robot.getHeldItem().isDamageableItem() && this.robot.getHeldItem().getDamageValue() >= this.robot.getHeldItem().getMaxDamage()) {
         this.startDelegateAI(new AIRobotGotoStationAndUnload(this.robot));
      } else {
         this.startDelegateAI(new AIRobotSearchEntity(this.robot,
            (IEntityFilter)entity -> entity instanceof Enemy || (entity instanceof NeutralMob neutral && neutral.isAngry()),
            250.0F, this.robot.getZoneToWork()));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotFetchAndEquipItemStack) {
         if (!ai.success()) {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotSearchEntity search) {
         if (ai.success()) {
            this.startDelegateAI(new AIRobotAttack(this.robot, search.target));
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      }
   }
}
