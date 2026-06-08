/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGotoBlock;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.robotics.ai.AIRobotLoad;
import buildcraft.robotics.ai.AIRobotSearchRandomGroundBlock;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.filter.ArrayStackFilter;
import buildcraft.robotics.path.IBlockFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BoardRobotBomber extends RedstoneBoardRobot {
   private static final IStackFilter TNT_FILTER = new ArrayStackFilter(new ItemStack(Blocks.TNT));
   private final int flyingHeight = 20;

   public BoardRobotBomber(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("bomber");
   }

   @Override
   public final void update() {
      if (!this.robot.containsItems()) {
         this.startDelegateAI(new AIRobotGotoStationAndLoad(this.robot, TNT_FILTER, AIRobotLoad.ANY_QUANTITY));
      } else {
         this.startDelegateAI(new AIRobotSearchRandomGroundBlock(this.robot, 100, new IBlockFilter() {
            @Override
            public boolean matches(Level world, BlockPos pos) {
               return pos.getY() < world.getMaxY() - BoardRobotBomber.this.flyingHeight && !world.getBlockState(pos).isAir();
            }
         }, this.robot.getZoneToWork()));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationAndLoad) {
         if (!ai.success()) {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotSearchRandomGroundBlock find) {
         if (ai.success()) {
            this.startDelegateAI(new AIRobotGotoBlock(this.robot,
               find.blockFound.getX(), find.blockFound.getY() + this.flyingHeight, find.blockFound.getZ()));
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotGotoBlock) {
         if (ai.success()) {
            this.dropBomb();
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      }
   }

   private void dropBomb() {
      if (!(this.robot instanceof EntityRobot entityRobot) || !(this.robot.level() instanceof ServerLevel serverLevel)) {
         return;
      }

      for (int slot = 0; slot < EntityRobot.NB_ITEMS_SLOTS; slot++) {
         ItemStack stack = entityRobot.getStackInSlot(slot);
         if (!stack.isEmpty() && TNT_FILTER.matches(stack)) {
            stack.shrink(1);
            if (stack.isEmpty()) {
               entityRobot.setStackInSlot(slot, ItemStack.EMPTY);
            }

            PrimedTnt tnt = new PrimedTnt(serverLevel, this.robot.getX() + 0.25, this.robot.getY() - 1.0, this.robot.getZ() + 0.25, this.robot);
            tnt.setFuse(37);
            serverLevel.addFreshEntity(tnt);
            serverLevel.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
         }
      }
   }
}
