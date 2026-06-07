package buildcraft.robotics.boards;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotBreak;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import net.minecraft.world.item.ItemStack;

/**
 * Base board that mines blocks matching {@link #isExpectedBlock} with a suitable tool, fetching/equipping a tool when
 * needed and unloading at a station when the tool wears out. Port of BuildCraft 7.1.x {@code BoardRobotGenericBreakBlock}.
 */
public abstract class BoardRobotGenericBreakBlock extends BoardRobotGenericSearchBlock {
   public BoardRobotGenericBreakBlock(EntityRobotBase robot) {
      super(robot);
   }

   public abstract boolean isExpectedTool(ItemStack stack);

   @Override
   public final void update() {
      ItemStack held = this.robot.getHeldItem();
      if (!this.isExpectedTool(ItemStack.EMPTY) && held.isEmpty()) {
         this.startDelegateAI(new AIRobotFetchAndEquipItemStack(this.robot, new IStackFilter() {
            @Override
            public boolean matches(ItemStack stack) {
               return !stack.isEmpty()
                  && (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage())
                  && BoardRobotGenericBreakBlock.this.isExpectedTool(stack);
            }
         }));
      } else if (!held.isEmpty() && held.isDamageableItem() && held.getDamageValue() >= held.getMaxDamage()) {
         this.startDelegateAI(new AIRobotGotoStationAndUnload(this.robot));
      } else if (this.blockFound() != null) {
         this.startDelegateAI(new AIRobotBreak(this.robot, this.blockFound()));
      } else {
         super.update();
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotFetchAndEquipItemStack || ai instanceof AIRobotGotoStationAndUnload) {
         if (!ai.success()) {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotBreak) {
         this.releaseBlockFound(ai.success());
      }

      super.delegateAIEnded(ai);
   }
}
