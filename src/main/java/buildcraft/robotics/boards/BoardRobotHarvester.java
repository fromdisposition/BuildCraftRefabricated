package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotHarvest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BoardRobotHarvester extends BoardRobotGenericSearchBlock {
   public BoardRobotHarvester(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("harvester");
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      return BuildCraftAPI.getWorldProperty("harvestable").get(world, pos);
   }

   @Override
   public void update() {
      if (this.blockFound() != null) {
         this.startDelegateAI(new AIRobotHarvest(this.robot, this.blockFound()));
      } else {
         super.update();
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotHarvest) {
         this.releaseBlockFound(ai.success());
      }

      super.delegateAIEnded(ai);
   }
}
