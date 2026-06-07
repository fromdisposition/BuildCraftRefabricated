package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

/** Reserves a docking station, flies adjacent to it via A*, then settles onto it and docks. */
public class AIRobotGotoStation extends AIRobot {
   private BlockPos stationIndex;
   private Direction stationSide;

   public AIRobotGotoStation(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoStation(EntityRobotBase robot, DockingStation station) {
      this(robot);
      this.stationIndex = station.index();
      this.stationSide = station.side();
      this.setSuccess(false);
   }

   @Override
   public void start() {
      DockingStation station = this.getStation();
      if (station == null) {
         this.terminate();
      } else if (station == this.robot.getDockingStation()) {
         this.setSuccess(true);
         this.terminate();
      } else if (station.take(this.robot)) {
         this.startDelegateAI(new AIRobotGotoBlock(this.robot,
            this.stationIndex.getX() + this.stationSide.getStepX(),
            this.stationIndex.getY() + this.stationSide.getStepY(),
            this.stationIndex.getZ() + this.stationSide.getStepZ()));
      } else {
         this.terminate();
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      DockingStation station = this.getStation();
      if (station == null) {
         this.terminate();
      } else if (ai instanceof AIRobotGotoBlock) {
         if (ai.success()) {
            this.startDelegateAI(new AIRobotStraightMoveTo(this.robot,
               this.stationIndex.getX() + 0.5 + this.stationSide.getStepX() * 0.5,
               this.stationIndex.getY() + 0.5 + this.stationSide.getStepY() * 0.5,
               this.stationIndex.getZ() + 0.5 + this.stationSide.getStepZ() * 0.5));
         } else {
            this.terminate();
         }
      } else if (!ai.success()) {
         this.terminate();
      } else {
         this.setSuccess(true);
         if (this.stationSide.getStepY() == 0) {
            this.robot.aimItemAt(new BlockPos(
               this.stationIndex.getX() + 2 * this.stationSide.getStepX(),
               this.stationIndex.getY(),
               this.stationIndex.getZ() + 2 * this.stationSide.getStepZ()));
         } else {
            this.robot.aimItemAt((float)(Math.floor(this.robot.getAimYaw() / 90.0F) * 90.0 + 180.0), this.robot.getAimPitch());
         }

         this.robot.dock(station);
         this.terminate();
      }
   }

   private DockingStation getStation() {
      return this.stationIndex == null ? null : this.robot.getRegistry().getStation(this.stationIndex, this.stationSide);
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.stationIndex != null) {
         nbt.putIntArray("stationIndex", new int[]{this.stationIndex.getX(), this.stationIndex.getY(), this.stationIndex.getZ()});
         nbt.putByte("stationSide", (byte)this.stationSide.ordinal());
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = nbt.getIntArray("stationIndex").orElse(new int[0]);
      if (arr.length == 3) {
         this.stationIndex = new BlockPos(arr[0], arr[1], arr[2]);
         this.stationSide = Direction.values()[nbt.getByte("stationSide").orElse((byte)0)];
      }
   }
}
