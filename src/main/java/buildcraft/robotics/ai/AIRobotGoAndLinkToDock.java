package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

/** Takes a station as the robot's main station and links to it (used to bind a robot to its home dock). */
public class AIRobotGoAndLinkToDock extends AIRobot {
   private DockingStation station;

   public AIRobotGoAndLinkToDock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGoAndLinkToDock(EntityRobotBase robot, DockingStation station) {
      this(robot);
      this.station = station;
   }

   @Override
   public void start() {
      if (this.station == this.robot.getLinkedStation() && this.station == this.robot.getDockingStation()) {
         this.terminate();
      } else if (this.station != null && this.station.takeAsMain(this.robot)) {
         this.startDelegateAI(new AIRobotGotoBlock(this.robot,
            this.station.index().getX() + this.station.side().getStepX() * 2,
            this.station.index().getY() + this.station.side().getStepY() * 2,
            this.station.index().getZ() + this.station.side().getStepZ() * 2));
      } else {
         this.setSuccess(false);
         this.terminate();
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoBlock) {
         if (ai.success()) {
            this.startDelegateAI(new AIRobotStraightMoveTo(this.robot,
               this.station.index().getX() + 0.5 + this.station.side().getStepX() * 0.5,
               this.station.index().getY() + 0.5 + this.station.side().getStepY() * 0.5,
               this.station.index().getZ() + 0.5 + this.station.side().getStepZ() * 0.5));
         } else {
            this.terminate();
         }
      } else if (ai instanceof AIRobotStraightMoveTo) {
         if (ai.success()) {
            this.robot.dock(this.station);
         }

         this.terminate();
      }
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.station != null && this.station.index() != null) {
         nbt.putIntArray("stationIndex", new int[]{this.station.index().getX(), this.station.index().getY(), this.station.index().getZ()});
         nbt.putByte("stationSide", (byte)this.station.side().ordinal());
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = nbt.getIntArray("stationIndex").orElse(new int[0]);
      if (arr.length == 3) {
         BlockPos index = new BlockPos(arr[0], arr[1], arr[2]);
         Direction side = Direction.values()[nbt.getByte("stationSide").orElse((byte)0)];
         this.station = this.robot.getRegistry().getStation(index, side);
      } else {
         this.station = this.robot.getLinkedStation();
      }
   }
}
