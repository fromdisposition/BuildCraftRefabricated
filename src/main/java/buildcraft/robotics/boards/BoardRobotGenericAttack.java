/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotAttack;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotRunErrand;
import buildcraft.robotics.ai.AIRobotSearchEntity;
import buildcraft.robotics.ai.StationErrand;
import buildcraft.robotics.path.IEntityFilter;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * The cycle for a board whose work is a living target: equip a sword, find one target inside the leash, close in
 * and strike. Subclasses only say what counts as a target.
 */
public abstract class BoardRobotGenericAttack extends RedstoneBoardRobot {
   public BoardRobotGenericAttack(EntityRobotBase robot) {
      super(robot);
   }

   /** What this board is willing to attack. */
   protected abstract boolean isTarget(Entity entity);

   @Override
   public final void update() {
      ItemStack held = this.robot.getHeldItem();
      if (held.isEmpty()) {
         this.startDelegateAI(new AIRobotFetchAndEquipItemStack(this.robot, (IStackFilter) stack -> stack.is(ItemTags.SWORDS)));
         return;
      }

      if (held.isDamageableItem() && held.getDamageValue() >= held.getMaxDamage()) {
         this.startDelegateAI(new AIRobotRunErrand(this.robot, StationErrand.unloadItems()));
         return;
      }

      this.startDelegateAI(new AIRobotSearchEntity(
         this.robot, (IEntityFilter) this::isTarget, EntityRobotBase.DEFAULT_SEARCH_RANGE, this.robot.getZoneToWork()));
   }

   @Override
   public final void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotFetchAndEquipItemStack || ai instanceof AIRobotRunErrand) {
         if (!ai.success()) {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }

         return;
      }

      if (ai instanceof AIRobotSearchEntity search) {
         if (search.success()) {
            this.startDelegateAI(new AIRobotAttack(this.robot, search.target));
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      }
   }
}
