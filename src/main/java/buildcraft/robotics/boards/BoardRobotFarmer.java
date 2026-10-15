/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotUseToolOnBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.Level;

public class BoardRobotFarmer extends BoardRobotGenericSearchBlock {
   public BoardRobotFarmer(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("farmer");
   }

   @Override
   protected IStackFilter toolFilter() {
      return stack -> !stack.isEmpty() && stack.is(ItemTags.HOES);
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      return BuildCraftAPI.getWorldProperty("dirt").get(world, pos) && world.getBlockState(pos.above()).isAir();
   }

   @Override
   protected void startWorkOn(BlockPos pos) {
      this.startDelegateAI(new AIRobotUseToolOnBlock(this.robot, pos));
   }
}
