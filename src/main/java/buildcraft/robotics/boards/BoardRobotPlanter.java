/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.crops.CropManager;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BoardRobotPlanter extends BoardRobotGenericSearchBlock {
   /** One trip to the chest per stack of seed, not per seed planted. */
   private static final int SEEDS_CARRIED = 64;

   public BoardRobotPlanter(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("planter");
   }

   @Override
   protected IStackFilter toolFilter() {
      return CropManager::isSeed;
   }

   @Override
   protected int toolAmount() {
      return SEEDS_CARRIED;
   }

   @Override
   protected boolean randomSearch() {
      return true;
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      return !BuildCraftAPI.getWorldProperty("replaceable").get(world, pos)
         && CropManager.canSustainPlant(world, this.robot.getHeldItem(), pos);
   }

   @Override
   protected void startWorkOn(BlockPos pos) {
      this.startDelegateAI(new AIRobotPlant(this.robot, pos));
   }
}
