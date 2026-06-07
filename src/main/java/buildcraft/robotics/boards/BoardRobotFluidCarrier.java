package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoadFluids;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class BoardRobotFluidCarrier extends RedstoneBoardRobot {
   public BoardRobotFluidCarrier(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("fluidCarrier");
   }

   @Override
   public void update() {
      if (!this.robotHasFluid()) {
         this.startDelegateAI(new AIRobotGotoStationAndLoadFluids(this.robot, buildcraft.robotics.path.PassThroughFluidFilter.INSTANCE));
      } else {
         this.startDelegateAI(new AIRobotGotoStationAndUnloadFluids(this.robot));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if ((ai instanceof AIRobotGotoStationAndLoadFluids || ai instanceof AIRobotGotoStationAndUnloadFluids) && !ai.success()) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }

   private boolean robotHasFluid() {
      for (StorageView<FluidVariant> view : this.robot.getFluidStorage()) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return true;
         }
      }

      return false;
   }
}
