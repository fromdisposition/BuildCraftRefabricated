/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoardRobotMiner extends BoardRobotGenericBreakBlock {
   /** Any ore, whatever tier: the tool decides what it can actually mine. */
   private static IWorldProperty anyOre;

   public BoardRobotMiner(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("miner");
   }

   @Override
   public boolean isExpectedTool(ItemStack stack) {
      return !stack.isEmpty() && stack.is(ItemTags.PICKAXES);
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      if (anyOre == null) {
         anyOre = BuildCraftAPI.getWorldProperty("ore@hardness=3");
      }

      if (!anyOre.get(world, pos)) {
         return false;
      }

      // Ask the tool itself instead of guessing a tier from the item's registry name -- that guess put every
      // modded pickaxe at the lowest tier, so a robot holding one could only mine coal.
      ItemStack tool = this.robot.getHeldItem();
      return !tool.isEmpty() && tool.isCorrectToolForDrops(world.getBlockState(pos));
   }
}
