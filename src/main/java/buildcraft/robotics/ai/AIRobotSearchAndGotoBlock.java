/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.path.IBlockFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class AIRobotSearchAndGotoBlock extends AIRobot {
   private BlockPos blockFound;
   private IBlockFilter filter;
   private boolean random;
   private double maxDistanceToEnd;

   public AIRobotSearchAndGotoBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchAndGotoBlock(EntityRobotBase robot, boolean random, IBlockFilter filter) {
      this(robot, random, filter, 0.0);
   }

   public AIRobotSearchAndGotoBlock(EntityRobotBase robot, boolean random, IBlockFilter filter, double maxDistanceToEnd) {
      this(robot);
      this.random = random;
      this.filter = filter;
      this.maxDistanceToEnd = maxDistanceToEnd;
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotSearchBlock(this.robot, this.random, this.filter, this.maxDistanceToEnd));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchBlock search) {
         if (ai.success() && search.takeResource()) {
            this.blockFound = search.blockFound;
            this.startDelegateAI(new AIRobotGotoBlock(this.robot, this.blockFound.getX(), this.blockFound.getY(), this.blockFound.getZ(), 1.5));
         } else {
            this.terminate();
         }
      } else if (ai instanceof AIRobotGotoBlock) {
         if (!ai.success()) {
            this.releaseBlockFound();
         }

         this.terminate();
      }
   }

   @Override
   public boolean success() {
      return this.blockFound != null;
   }

   public BlockPos getBlockFound() {
      return this.blockFound;
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
