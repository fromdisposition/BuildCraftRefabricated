/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraft.robotics.ai.AIRobotPumpBlock;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.path.IBlockFilter;
import buildcraft.robotics.path.IFluidFilter;
import buildcraft.robotics.path.PassThroughFluidFilter;
import buildcraft.robotics.statement.StationActions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class BoardRobotPump extends RedstoneBoardRobot {
   private BlockPos blockFound;
   private IFluidFilter fluidFilter;

   public BoardRobotPump(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("pump");
   }

   @Override
   public void update() {
      if (this.robot.hasFluid()) {
         this.startDelegateAI(new AIRobotGotoStationAndUnloadFluids(this.robot));
      } else {
         final IWorldProperty isFluidSource = BuildCraftAPI.getWorldProperty("fluidSource");
         this.updateFilter();
         this.startDelegateAI(new AIRobotSearchAndGotoBlock(this.robot, false, new IBlockFilter() {
            @Override
            public boolean matches(Level world, BlockPos pos) {
               return isFluidSource.get(world, pos)
                  && !BoardRobotPump.this.robot.getRegistry().isTaken(new ResourceIdBlock(pos))
                  && BoardRobotPump.this.matchesGateFilter(world, pos);
            }
         }));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoBlock search) {
         if (ai.success()) {
            this.blockFound = search.getBlockFound();
            this.startDelegateAI(new AIRobotPumpBlock(this.robot, this.blockFound));
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotPumpBlock) {
         this.releaseBlockFound();
      } else if (ai instanceof AIRobotGotoStationAndUnloadFluids && !ai.success()) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }

   @Override
   public void end() {
      this.releaseBlockFound();
   }

   private void updateFilter() {
      this.fluidFilter = StationActions.getGateFluidFilter(this.robot.getLinkedStation());
      if (this.fluidFilter instanceof PassThroughFluidFilter) {
         this.fluidFilter = null;
      }
   }

   private boolean matchesGateFilter(Level world, BlockPos pos) {
      if (this.fluidFilter == null) {
         return true;
      }

      Fluid fluid = BlockUtil.getFluid(world, pos);
      return this.fluidFilter.matches(fluid);
   }

   private void releaseBlockFound() {
      if (this.blockFound != null) {
         this.robot.getRegistry().release(new ResourceIdBlock(this.blockFound));
         this.blockFound = null;
      }
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.blockFound != null) {
         nbt.putIntArray("blockFound", new int[]{this.blockFound.getX(), this.blockFound.getY(), this.blockFound.getZ()});
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = nbt.getIntArray("blockFound").orElse(new int[0]);
      if (arr.length == 3) {
         this.blockFound = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }
}
