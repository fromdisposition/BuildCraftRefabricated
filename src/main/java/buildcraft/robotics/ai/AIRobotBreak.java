/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.robotics.entity.EntityRobot;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class AIRobotBreak extends AIRobotGoto {
   private BlockPos blockToBreak;
   private long energyNeeded;
   private long energySpent;

   public AIRobotBreak(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotBreak(EntityRobotBase robot, BlockPos blockToBreak) {
      this(robot);
      this.blockToBreak = blockToBreak;
   }

   @Override
   public void start() {
      if (this.blockToBreak == null) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      this.robot.aimItemAt(this.blockToBreak);
      this.robot.setItemActive(true);
      this.energyNeeded = BlockUtil.computeBlockBreakPower(this.robot.level(), this.blockToBreak);
   }

   @Override
   public void update() {
      if (this.robot.level().getBlockState(this.blockToBreak).isAir()) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      long want = Math.max(1L, this.energyNeeded / 30L);
      long extracted = this.robot.getBattery().extractPower(1L, want);
      this.energySpent += extracted;

      int stage = this.energyNeeded <= 0L ? 9 : (int)(this.energySpent * 9L / this.energyNeeded);
      this.robot.level().destroyBlockProgress(this.robot.getId(), this.blockToBreak, Math.min(9, stage));

      if (this.energySpent >= this.energyNeeded) {
         this.harvest();
         this.terminate();
      }
   }

   private void harvest() {
      if (!(this.robot.level() instanceof ServerLevel serverLevel) || !(this.robot instanceof EntityRobot entityRobot)) {
         this.setSuccess(false);
         return;
      }

      ItemStack tool = entityRobot.getItemInUse();
      List<ItemStack> drops = BlockUtil.breakBlockAndGetDrops(serverLevel, this.blockToBreak, tool, this.robot.getOwner()).orElse(null);
      if (drops == null) {
         this.setSuccess(false);
         return;
      }

      for (ItemStack drop : drops) {
         ItemStack remaining = entityRobot.receiveItem(null, drop);
         if (!remaining.isEmpty()) {
            ItemEntity entity = new ItemEntity(serverLevel, this.robot.getX(), this.robot.getY(), this.robot.getZ(), remaining);
            serverLevel.addFreshEntity(entity);
         }
      }

      this.setSuccess(true);
   }

   @Override
   public void end() {
      this.robot.setItemActive(false);
      if (this.blockToBreak != null) {
         this.robot.level().destroyBlockProgress(this.robot.getId(), this.blockToBreak, -1);
      }
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.blockToBreak != null) {
         nbt.putIntArray("blockToBreak", new int[]{this.blockToBreak.getX(), this.blockToBreak.getY(), this.blockToBreak.getZ()});
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = BcNbt.getIntArray(nbt, "blockToBreak");
      if (arr.length == 3) {
         this.blockToBreak = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }
}
