/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BlueprintBuilder;
import buildcraft.builders.snapshot.ConstructionMarkerRegistry;
import buildcraft.builders.tile.TileConstructionMarker;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.robotics.entity.EntityRobot;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BoardRobotBuilder extends BoardRobotBC {
   private static final double REACH = 5.0;

   @Nullable
   private buildcraft.robotics.robot.RobotBlueprintContext context;
   @Nullable
   private BlockPos markerPos;
   private boolean materialsUnavailable;
   @Nullable
   private BlockPos pendingMarkerPos;
   @Nullable
   private CompoundTag pendingBuilderNbt;

   public BoardRobotBuilder(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "builder";
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("builder");
   }

   @Override
   public void update() {
      if (!(this.robot instanceof EntityRobot entityRobot)) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         return;
      }

      if (this.context == null && !this.tryRestorePendingContext(entityRobot) && !this.acquireMarker(entityRobot)) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         return;
      }

      BlueprintBuilder builder = this.context.getBlueprintBuilder();

      ItemStack missing = this.firstMissingRequired(builder);
      if (missing != null && this.robot.hasFreeSlot() && !this.materialsUnavailable) {
         IStackFilter filter = stack -> StackUtil.canMerge(missing, stack);
         this.startDelegateAI(new AIRobotGotoStationAndLoad(this.robot, filter, missing.getCount()));
         return;
      }

      Vec3 centroid = this.activeCentroid(builder);
      if (centroid != null) {
         entityRobot.destination = centroid.add(0.0, 2.0, 0.0);
         this.robot.aimItemAt(BlockPos.containing(centroid));
         this.context.setInRange(this.robot.position().distanceTo(centroid) < REACH);
      } else {
         entityRobot.destination = null;
         this.context.setInRange(false);
      }

      boolean done = builder.tick();
      if (done) {
         this.completeMarker();
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         return;
      }

      
      
      
      if (centroid == null && missing != null) {
         this.materialsUnavailable = false;
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationAndLoad) {
         this.materialsUnavailable = !ai.success();
      }
   }

   @Override
   public void end() {
      this.releaseMarker();
   }

   private boolean tryRestorePendingContext(EntityRobot entityRobot) {
      if (this.pendingMarkerPos == null) {
         return false;
      }

      BlockPos pos = this.pendingMarkerPos;
      this.pendingMarkerPos = null;
      CompoundTag builderNbt = this.pendingBuilderNbt;
      this.pendingBuilderNbt = null;
      Level level = entityRobot.level();
      if (level.getBlockEntity(pos) instanceof TileConstructionMarker marker && marker.getBlueprintBuildingInfo() != null) {
         this.context = new buildcraft.robotics.robot.RobotBlueprintContext(entityRobot, pos, marker.getBlueprintBuildingInfo(), marker.getOwner());
         this.context.getBlueprintBuilder().updateSnapshot();
         if (builderNbt != null) {
            this.context.getBlueprintBuilder().deserializeNBT(builderNbt);
         }

         this.markerPos = pos;
         this.robot.getRegistry().take(new ResourceIdBlock(pos), this.robot);
         return true;
      }

      return false;
   }

   private boolean acquireMarker(EntityRobot entityRobot) {
      Level level = entityRobot.level();
      IZone zone = this.robot.getZoneToWork();
      BlockPos best = null;
      double bestDistSq = Double.MAX_VALUE;

      for (BlockPos pos : ConstructionMarkerRegistry.getMarkers(level)) {
         if (zone != null && !zone.contains(Vec3.atCenterOf(pos))) {
            continue;
         }

         if (this.robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
            continue;
         }

         if (!(level.getBlockEntity(pos) instanceof TileConstructionMarker marker) || marker.getBlueprintBuildingInfo() == null) {
            continue;
         }

         double distSq = this.robot.position().distanceToSqr(Vec3.atCenterOf(pos));
         if (distSq < bestDistSq) {
            bestDistSq = distSq;
            best = pos;
         }
      }

      if (best == null || !this.robot.getRegistry().take(new ResourceIdBlock(best), this.robot)) {
         return false;
      }

      TileConstructionMarker marker = (TileConstructionMarker) level.getBlockEntity(best);
      Blueprint.BuildingInfo info = marker.getBlueprintBuildingInfo();
      if (info == null) {
         this.robot.getRegistry().release(new ResourceIdBlock(best));
         return false;
      }

      this.context = new buildcraft.robotics.robot.RobotBlueprintContext(entityRobot, best, info, marker.getOwner());
      this.context.getBlueprintBuilder().updateSnapshot();
      this.markerPos = best;
      this.materialsUnavailable = false;
      return true;
   }

   private void completeMarker() {
      if (this.markerPos != null && this.robot.level().getBlockEntity(this.markerPos) instanceof TileConstructionMarker marker) {
         marker.markBuilt();
      }

      this.releaseMarker();
   }

   private void releaseMarker() {
      if (this.markerPos != null) {
         this.robot.getRegistry().release(new ResourceIdBlock(this.markerPos));
         this.markerPos = null;
      }

      this.context = null;
   }

   @Nullable
   private ItemStack firstMissingRequired(BlueprintBuilder builder) {
      for (ItemStack req : builder.remainingDisplayRequired) {
         if (req.isEmpty() || req.getItem() instanceof BucketItem) {
            continue;
         }

         IStackFilter filter = stack -> StackUtil.canMerge(req, stack);
         ItemStack available = this.context.getInvResources().extract(filter, req.getCount(), req.getCount(), true);
         if (available.getCount() < req.getCount()) {
            return req;
         }
      }

      return null;
   }

   @Nullable
   private Vec3 activeCentroid(BlueprintBuilder builder) {
      double x = 0.0;
      double y = 0.0;
      double z = 0.0;
      int count = 0;

      for (var task : builder.placeTasks) {
         x += task.pos.getX() + 0.5;
         y += task.pos.getY() + 0.5;
         z += task.pos.getZ() + 0.5;
         count++;
      }

      for (var task : builder.breakTasks) {
         x += task.pos.getX() + 0.5;
         y += task.pos.getY() + 0.5;
         z += task.pos.getZ() + 0.5;
         count++;
      }

      return count == 0 ? null : new Vec3(x / count, y / count, z / count);
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.context != null && this.markerPos != null) {
         nbt.putIntArray("markerPos", new int[]{this.markerPos.getX(), this.markerPos.getY(), this.markerPos.getZ()});
         nbt.put("builder", this.context.getBlueprintBuilder().serializeNBT());
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = BcNbt.getIntArray(nbt, "markerPos");
      if (arr.length == 3) {
         this.pendingMarkerPos = new BlockPos(arr[0], arr[1], arr[2]);
         this.pendingBuilderNbt = BcNbt.getCompound(nbt, "builder");
      }
   }
}
