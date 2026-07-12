/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AIRobotUseToolOnBlock extends AIRobot {
   private BlockPos useToBlock;
   private int useCycles;

   public AIRobotUseToolOnBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotUseToolOnBlock(EntityRobotBase robot, BlockPos index) {
      this(robot);
      this.useToBlock = index;
   }

   @Override
   public void start() {
      if (this.useToBlock == null) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      this.robot.aimItemAt(this.useToBlock);
      this.robot.setItemActive(true);
   }

   @Override
   public void update() {
      this.useCycles++;
      if (this.useCycles <= 40) {
         return;
      }

      if (!(this.robot.level() instanceof ServerLevel serverLevel) || !(this.robot instanceof EntityRobot entityRobot)) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      ItemStack stack = entityRobot.getItemInUse();
      ServerPlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(serverLevel, this.robot.getOwner(), this.useToBlock);
      fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);

      Vec3 hit = Vec3.atCenterOf(this.useToBlock);
      BlockHitResult hitResult = new BlockHitResult(hit, Direction.UP, this.useToBlock, false);
      UseOnContext context = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, hitResult);
      InteractionResult result = stack.useOn(context);

      this.setSuccess(result != InteractionResult.FAIL && result != InteractionResult.PASS);
      this.terminate();
   }

   @Override
   public void end() {
      this.robot.setItemActive(false);
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ * 8L / 10L;
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.useToBlock != null) {
         nbt.putIntArray("blockFound", new int[]{this.useToBlock.getX(), this.useToBlock.getY(), this.useToBlock.getZ()});
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = BcNbt.getIntArray(nbt, "blockFound");
      if (arr.length == 3) {
         this.useToBlock = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }
}
