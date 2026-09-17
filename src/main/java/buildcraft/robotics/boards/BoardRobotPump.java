/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.robotics.ai.AIRobotPumpBlock;
import buildcraft.robotics.ai.StationErrand;
import buildcraft.robotics.path.IBlockFilter;
import buildcraft.robotics.path.IFluidFilter;
import buildcraft.robotics.path.PassThroughFluidFilter;
import buildcraft.robotics.statement.StationActions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BoardRobotPump extends BoardRobotGenericSearchBlock {
   public BoardRobotPump(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("pump");
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      return BuildCraftAPI.getWorldProperty("fluidSource").get(world, pos);
   }

   @Override
   protected IBlockFilter gateFilter() {
      IFluidFilter filter = StationActions.getGateFluidFilter(this.robot.getLinkedStation());
      if (filter instanceof PassThroughFluidFilter) {
         return (world, pos) -> true;
      }

      return (world, pos) -> filter.matches(BlockUtil.getFluid(world, pos));
   }

   /** The tank holds one draw at a time, so any fluid at all means the next stop is a station. */
   @Override
   protected boolean hasCargo() {
      return this.robot.hasFluid();
   }

   @Override
   protected boolean cargoFull() {
      return this.robot.hasFluid();
   }

   @Override
   protected StationErrand deliveryErrand() {
      return StationErrand.unloadFluids();
   }

   @Override
   protected void startWorkOn(BlockPos pos) {
      this.startDelegateAI(new AIRobotPumpBlock(this.robot, pos));
   }
}
