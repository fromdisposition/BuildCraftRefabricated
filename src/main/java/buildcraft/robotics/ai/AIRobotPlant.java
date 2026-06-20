/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.crops.CropManager;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class AIRobotPlant extends AIRobotGoto {
   private BlockPos blockFound;
   private int delay;

   public AIRobotPlant(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotPlant(EntityRobotBase robot, BlockPos blockFound) {
      super(robot);
      this.blockFound = blockFound;
   }

   @Override
   public void start() {
      if (this.blockFound != null) {
         this.robot.aimItemAt(this.blockFound);
         this.robot.setItemActive(true);
      }
   }

   @Override
   public void update() {
      if (this.blockFound == null) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      if (this.delay++ <= 40) {
         return;
      }

      if (this.robot.level() instanceof ServerLevel serverLevel) {
         if (!BlockUtil.canMachinePlace(serverLevel, this.blockFound, this.robot.getOwner(), this.robot.blockPosition())) {
            this.setSuccess(false);
            this.terminate();
            return;
         }

         ServerPlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer(serverLevel, this.robot.getOwner(), this.blockFound);
         ItemStack seed = this.robot.getHeldItem();
         if (!CropManager.plantCrop(serverLevel, player, seed, this.blockFound)) {
            this.setSuccess(false);
         }

         ItemStack held = this.robot.getHeldItem();
         if (!held.isEmpty()) {
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.robot.getX(), this.robot.getY(), this.robot.getZ(), held.copy()));
         }
      }

      this.robot.setItemInUse(ItemStack.EMPTY);
      this.terminate();
   }

   @Override
   public void end() {
      this.robot.setItemActive(false);
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

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 2L;
   }
}
