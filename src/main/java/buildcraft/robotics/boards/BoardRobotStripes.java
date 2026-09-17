/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotStripesHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BoardRobotStripes extends BoardRobotGenericSearchBlock {
   public BoardRobotStripes(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("stripes");
   }

   @Override
   protected IStackFilter toolFilter() {
      return stack -> !stack.isEmpty();
   }

   @Override
   protected boolean randomSearch() {
      return true;
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      return world.getBlockState(pos).isAir();
   }

   @Override
   protected void startWorkOn(BlockPos pos) {
      this.startDelegateAI(new AIRobotStripesHandler(this.robot, pos));
   }
}
