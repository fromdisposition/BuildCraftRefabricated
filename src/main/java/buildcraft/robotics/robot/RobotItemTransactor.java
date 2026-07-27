/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.robot;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.inventory.AbstractInvItemTransactor;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.entity.EntityRobot;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class RobotItemTransactor extends AbstractInvItemTransactor {
   private final EntityRobot robot;

   public RobotItemTransactor(EntityRobot robot) {
      this.robot = robot;
   }

   @Nonnull
   @Override
   protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
      ItemStack current = this.robot.getStackInSlot(slot);
      if (current.isEmpty()) {
         int max = stack.getMaxStackSize();
         ItemStack split = stack.split(max);
         if (!simulate) {
            this.robot.setStackInSlot(slot, split);
         }

         return stack.isEmpty() ? StackUtil.EMPTY : stack;
      } else if (StackUtil.canMerge(current, stack)) {
         ItemStack merged = current.copy();
         merged.setCount(merged.getCount() + stack.getCount());
         int size = merged.getMaxStackSize();
         if (merged.getCount() > size) {
            stack.setCount(stack.getCount() - (merged.getCount() - size));
            merged.setCount(size);
            if (!simulate) {
               this.robot.setStackInSlot(slot, merged);
            }

            return stack;
         } else {
            if (!simulate) {
               this.robot.setStackInSlot(slot, merged);
            }

            return StackUtil.EMPTY;
         }
      } else {
         return stack;
      }
   }

   @Nonnull
   @Override
   protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
      ItemStack current = this.robot.getStackInSlot(slot);
      if (current.isEmpty()) {
         return StackUtil.EMPTY;
      }

      if (filter.matches(current.copy())) {
         if (current.getCount() < min) {
            return StackUtil.EMPTY;
         }

         int size = Math.min(current.getCount(), max);
         current = current.copy();
         ItemStack other = current.split(size);
         if (!simulate) {
            if (current.getCount() <= 0) {
               current = StackUtil.EMPTY;
            }

            this.robot.setStackInSlot(slot, current);
         }

         return other;
      } else {
         return StackUtil.EMPTY;
      }
   }

   @Override
   protected int size() {
      return EntityRobot.NB_ITEMS_SLOTS;
   }

   @Override
   protected boolean isEmpty(int slot) {
      return this.robot.getStackInSlot(slot).isEmpty();
   }
}
