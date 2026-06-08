/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.fabric.BCLibFakePlayerProvider;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class AIRobotStripesHandler extends AIRobot implements IStripesActivator {
   private BlockPos useToBlock;
   private int useCycles;

   public AIRobotStripesHandler(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotStripesHandler(EntityRobotBase robot, BlockPos index) {
      super(robot);
      this.useToBlock = index;
   }

   @Override
   public void start() {
      if (this.useToBlock != null) {
         this.robot.aimItemAt(this.useToBlock);
         this.robot.setItemActive(true);
      }
   }

   @Override
   public void update() {
      if (this.useToBlock == null) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      if (this.useCycles++ <= 60) {
         return;
      }

      if (this.robot.level() instanceof ServerLevel serverLevel && PipeApi.stripeRegistry != null) {
         ItemStack stack = this.robot.getHeldItem();
         ServerPlayer player = this.getFakePlayer(serverLevel);
         if (PipeApi.stripeRegistry.handleItem(serverLevel, this.useToBlock, Direction.NORTH, stack, player, this)) {
            this.robot.setItemInUse(ItemStack.EMPTY);
            this.terminate();
            return;
         }
      }

      this.terminate();
   }

   private ServerPlayer getFakePlayer(ServerLevel serverLevel) {
      ServerPlayer player = buildcraft.api.core.BuildCraftAPI.fakePlayerProvider.getFakePlayer(serverLevel, BCLibFakePlayerProvider.NULL_PROFILE, this.useToBlock);
      player.setXRot(0.0F);
      player.setYRot(180.0F);
      return player;
   }

   @Override
   public void end() {
      this.robot.setItemActive(false);
   }

   @Override
   public boolean sendItem(@Nonnull ItemStack stack, Direction direction) {
      this.dropItem(stack, direction);
      return true;
   }

   @Override
   public void dropItem(@Nonnull ItemStack stack, Direction direction) {
      if (this.robot.level() instanceof ServerLevel serverLevel) {
         serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.robot.getX(), this.robot.getY(), this.robot.getZ(), stack));
      }
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ * 3L / 2L;
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
      int[] arr = nbt.getIntArray("blockFound").orElse(new int[0]);
      if (arr.length == 3) {
         this.useToBlock = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }
}
