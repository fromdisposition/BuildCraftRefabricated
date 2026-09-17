/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotBreak;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** A board whose work is "break the block", with the tool that block needs. */
public abstract class BoardRobotGenericBreakBlock extends BoardRobotGenericSearchBlock {
   public BoardRobotGenericBreakBlock(EntityRobotBase robot) {
      super(robot);
   }

   public abstract boolean isExpectedTool(ItemStack stack);

   @Override
   protected final IStackFilter toolFilter() {
      return stack -> !stack.isEmpty()
         && (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage())
         && this.isExpectedTool(stack);
   }

   @Override
   protected final void startWorkOn(BlockPos pos) {
      this.startDelegateAI(new AIRobotBreak(this.robot, pos));
   }
}
