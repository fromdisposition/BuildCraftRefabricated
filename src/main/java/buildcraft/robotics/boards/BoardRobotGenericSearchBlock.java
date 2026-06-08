/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.path.IBlockFilter;
import buildcraft.robotics.statement.StationActions;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class BoardRobotGenericSearchBlock extends RedstoneBoardRobot {
   private BlockPos blockFound;

   public BoardRobotGenericSearchBlock(EntityRobotBase robot) {
      super(robot);
   }

   public abstract boolean isExpectedBlock(Level world, BlockPos pos);

   @Override
   public void update() {
      this.startDelegateAI(new AIRobotSearchAndGotoBlock(this.robot, false, new IBlockFilter() {
         @Override
         public boolean matches(Level world, BlockPos pos) {
            return BoardRobotGenericSearchBlock.this.isExpectedBlock(world, pos)
               && !BoardRobotGenericSearchBlock.this.robot.getRegistry().isTaken(new ResourceIdBlock(pos))
               && BoardRobotGenericSearchBlock.this.matchesGateFilter(world, pos);
         }
      }));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoBlock search) {
         if (ai.success()) {
            this.blockFound = search.getBlockFound();
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      }
   }

   @Override
   public void end() {
      this.releaseBlockFound(true);
   }

   protected BlockPos blockFound() {
      return this.blockFound;
   }

   protected void releaseBlockFound(boolean success) {
      if (this.blockFound != null) {
         this.robot.getRegistry().release(new ResourceIdBlock(this.blockFound));
         this.blockFound = null;
      }
   }

   protected boolean matchesGateFilter(Level world, BlockPos pos) {
      List<ItemStack> filter = StationActions.getGateFilterStacks(this.robot.getLinkedStation());
      if (filter.isEmpty()) {
         return true;
      }

      ItemStack blockItem = new ItemStack(world.getBlockState(pos).getBlock());
      if (blockItem.isEmpty()) {
         return false;
      }

      for (ItemStack ref : filter) {
         if (ItemStack.isSameItem(ref, blockItem)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.blockFound != null) {
         nbt.putIntArray("indexStored", new int[]{this.blockFound.getX(), this.blockFound.getY(), this.blockFound.getZ()});
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = nbt.getIntArray("indexStored").orElse(new int[0]);
      if (arr.length == 3) {
         this.blockFound = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }
}
