/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.StackRequest;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class AIRobotDeliverRequested extends AIRobot {
   private StackRequest requested;

   public AIRobotDeliverRequested(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotDeliverRequested(EntityRobotBase robot, StackRequest request) {
      this(robot);
      this.requested = request;
   }

   @Override
   public void start() {
      if (this.requested != null) {
         this.startDelegateAI(new AIRobotGotoStation(this.robot, this.requested.getStation(this.robot.level())));
      } else {
         this.setSuccess(false);
         this.terminate();
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (!(ai instanceof AIRobotGotoStation)) {
         return;
      }

      if (!ai.success()) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      IRequestProvider requester = this.requested.getRequester(this.robot.level());
      if (requester == null || !(this.robot instanceof EntityRobot entityRobot)) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      int count = 0;
      ItemStack wanted = this.requested.getStack();

      for (int slot = 0; slot < EntityRobot.NB_ITEMS_SLOTS; slot++) {
         ItemStack stack = entityRobot.getStackInSlot(slot);
         if (stack.isEmpty() || !StackUtil.isMatchingItemOrList(wanted, stack)) {
            continue;
         }

         int before = stack.getCount();
         ItemStack remaining = requester.offerItem(this.requested.getSlot(), stack.copy());
         if (remaining.isEmpty()) {
            entityRobot.setStackInSlot(slot, ItemStack.EMPTY);
            count += before;
         } else if (remaining.getCount() != before) {
            entityRobot.setStackInSlot(slot, remaining);
            count += before - remaining.getCount();
         }
      }

      this.setSuccess(count > 0);
      this.terminate();
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.requested != null) {
         CompoundTag requestNBT = new CompoundTag();
         this.requested.writeToNBT(requestNBT);
         nbt.put("currentRequest", requestNBT);
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      if (nbt.contains("currentRequest")) {
         this.requested = StackRequest.loadFromNBT(BcNbt.getCompound(nbt, "currentRequest"));
      }
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ;
   }
}
