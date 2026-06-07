package buildcraft.robotics.robot;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjBattery;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BlueprintBuilder;
import buildcraft.builders.snapshot.ITileForBlueprintBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.lib.fabric.transfer.MultiFluidTankStorage;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.robotics.entity.EntityRobot;
import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Adapter that lets an {@link EntityRobot} act as the host of a real {@link BlueprintBuilder}. All resources the builder
 * needs (items, fluids, energy) are sourced directly from the robot, while the structure layout and owner come from a
 * construction marker. The robot therefore reuses the entire snapshot build pipeline instead of a bespoke per-slot API.
 */
public class RobotBlueprintContext implements ITileForBlueprintBuilder {
   private final EntityRobot robot;
   private final BlockPos markerPos;
   private final Blueprint.BuildingInfo buildingInfo;
   @Nullable
   private final GameProfile owner;
   private final GatedMjBattery battery;
   private final RobotItemTransactor invResources;
   private final MultiFluidTankStorage fluidTanks;
   private final BlueprintBuilder builder;

   public RobotBlueprintContext(EntityRobot robot, BlockPos markerPos, Blueprint.BuildingInfo buildingInfo, @Nullable GameProfile owner) {
      this.robot = robot;
      this.markerPos = markerPos.immutable();
      this.buildingInfo = buildingInfo;
      this.owner = owner;
      this.battery = new GatedMjBattery(robot.getBattery());
      this.invResources = new RobotItemTransactor(robot);
      this.fluidTanks = new MultiFluidTankStorage(new SingleFluidTank[]{robot.getFluidStorage()});
      this.builder = new BlueprintBuilder(this);
   }

   public BlueprintBuilder getBlueprintBuilder() {
      return this.builder;
   }

   public BlockPos getMarkerPos() {
      return this.markerPos;
   }

   public void setInRange(boolean inRange) {
      this.battery.inRange = inRange;
   }

   @Override
   public Level getWorldBC() {
      return this.robot.level();
   }

   @Override
   public MjBattery getBattery() {
      return this.battery;
   }

   @Override
   public BlockPos getBuilderPos() {
      return this.markerPos;
   }

   @Override
   public boolean canExcavate() {
      return true;
   }

   @Override
   public SnapshotBuilder<?> getBuilder() {
      return this.builder;
   }

   @Override
   public GameProfile getOwner() {
      return this.owner;
   }

   @Override
   public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
      return this.buildingInfo;
   }

   @Override
   public IItemTransactor getInvResources() {
      return this.invResources;
   }

   @Override
   public MultiFluidTankStorage getFluidTanks() {
      return this.fluidTanks;
   }
}
