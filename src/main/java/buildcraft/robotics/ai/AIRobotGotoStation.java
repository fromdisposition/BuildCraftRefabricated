package buildcraft.robotics.ai;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;

/** Reserves a docking station, flies adjacent to it via A*, then settles onto it and docks. */
public class AIRobotGotoStation extends AIRobotStationNavigate {
   public AIRobotGotoStation(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoStation(EntityRobotBase robot, DockingStation station) {
      super(robot);
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
         this.beginNavigation(station);
      } else {
         this.terminate();
      }
   }

   @Override
   protected int approachSteps() {
      return 1;
   }

   @Override
   protected void onReachedDock(DockingStation station) {
      this.setSuccess(true);
      if (this.stationSide.getStepY() == 0) {
         this.robot.aimItemAt(
            new BlockPos(
               this.stationIndex.getX() + 2 * this.stationSide.getStepX(),
               this.stationIndex.getY(),
               this.stationIndex.getZ() + 2 * this.stationSide.getStepZ()
            )
         );
      } else {
         this.robot.aimItemAt((float)(Math.floor(this.robot.getAimYaw() / 90.0F) * 90.0 + 180.0), this.robot.getAimPitch());
      }

      this.robot.dock(station);
      this.terminate();
   }
}
