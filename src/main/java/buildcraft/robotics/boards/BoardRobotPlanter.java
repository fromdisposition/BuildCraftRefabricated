/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.crops.CropManager;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotPlant;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.path.IBlockFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoardRobotPlanter extends RedstoneBoardRobot {
   private BlockPos blockFound;
   private final IStackFilter filter = CropManager::isSeed;

   public BoardRobotPlanter(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("planter");
   }

   @Override
   public void update() {
      if (this.robot.getHeldItem().isEmpty()) {
         this.startDelegateAI(new AIRobotFetchAndEquipItemStack(this.robot, this.filter));
      } else {
         ItemStack itemStack = this.robot.getHeldItem();
         IBlockFilter blockFilter = new IBlockFilter() {
            @Override
            public boolean matches(Level world, BlockPos pos) {
               return !BuildCraftAPI.getWorldProperty("replaceable").get(world, pos)
                  && CropManager.canSustainPlant(world, itemStack, pos)
                  && !BoardRobotPlanter.this.robot.getRegistry().isTaken(new ResourceIdBlock(pos));
            }
         };
         this.startDelegateAI(new AIRobotSearchAndGotoBlock(this.robot, true, blockFilter, 1.0));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoBlock search) {
         if (ai.success()) {
            this.blockFound = search.getBlockFound();
            this.startDelegateAI(new AIRobotPlant(this.robot, this.blockFound));
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotPlant) {
         this.releaseBlockFound();
      } else if (ai instanceof AIRobotFetchAndEquipItemStack) {
         if (!ai.success()) {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      }
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
      int[] arr = BcNbt.getIntArray(nbt, "blockFound");
      if (arr.length == 3) {
         this.blockFound = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }
}
