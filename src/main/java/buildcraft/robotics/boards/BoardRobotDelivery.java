package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.StackRequest;
import buildcraft.robotics.ai.AIRobotDeliverRequested;
import buildcraft.robotics.ai.AIRobotDisposeItems;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.ai.AIRobotSearchStackRequest;
import buildcraft.robotics.statement.StationActions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class BoardRobotDelivery extends RedstoneBoardRobot {
   private final List<ItemStack> deliveryBlacklist = new ArrayList<>();
   private StackRequest currentRequest;

   public BoardRobotDelivery(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("delivery");
   }

   @Override
   public void update() {
      if (this.robot.containsItems()) {
         this.startDelegateAI(new AIRobotDisposeItems(this.robot));
      } else if (this.currentRequest == null) {
         this.startDelegateAI(new AIRobotSearchStackRequest(this.robot, StationActions.getGateFilter(this.robot.getLinkedStation()), this.deliveryBlacklist));
      } else {
         IStackFilter filter = stack -> this.currentRequest != null && StackUtil.isMatchingItemOrList(this.currentRequest.getStack(), stack);
         this.startDelegateAI(new AIRobotGotoStationAndLoad(this.robot, filter, this.currentRequest.getStack().getCount()));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchStackRequest request) {
         if (!ai.success()) {
            this.deliveryBlacklist.clear();
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         } else {
            this.currentRequest = request.request;
            if (!this.currentRequest.getStation(this.robot.level()).take(this.robot)) {
               this.releaseCurrentRequest();
            }
         }
      } else if (ai instanceof AIRobotGotoStationAndLoad) {
         if (!ai.success()) {
            this.deliveryBlacklist.add(this.currentRequest.getStack());
            this.releaseCurrentRequest();
         } else {
            this.startDelegateAI(new AIRobotDeliverRequested(this.robot, this.currentRequest));
         }
      } else if (ai instanceof AIRobotDeliverRequested) {
         this.releaseCurrentRequest();
      }
   }

   private void releaseCurrentRequest() {
      if (this.currentRequest != null) {
         this.robot.getRegistry().release(this.currentRequest.getResourceId(this.robot.level()));
         this.currentRequest.getStation(this.robot.level()).release(this.robot);
         this.currentRequest = null;
      }
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.currentRequest != null) {
         CompoundTag requestNBT = new CompoundTag();
         this.currentRequest.writeToNBT(requestNBT);
         nbt.put("currentRequest", requestNBT);
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      if (nbt.contains("currentRequest")) {
         this.currentRequest = StackRequest.loadFromNBT(nbt.getCompound("currentRequest").orElse(new CompoundTag()));
      }
   }
}
