/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.path.IBlockFilter;
import buildcraft.robotics.statement.StationActions;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The work cycle every block-working board runs: acquire tool, find one block, travel to it, work it, and
 * <b>deliver what that produced</b>. Subclasses supply only the parameters -- which blocks count, which tool is
 * needed, what "work" means -- and never the control flow, so a board cannot express an incomplete cycle.
 *
 * <p>Delivery in particular is not a per-board decision. It runs whenever the robot is out of room, and again
 * when there is no work left, which is what stops a miner from filling up and then scattering everything it
 * breaks across the work site.
 */
public abstract class BoardRobotGenericSearchBlock extends RedstoneBoardRobot {
   private BlockPos blockFound;

   public BoardRobotGenericSearchBlock(EntityRobotBase robot) {
      super(robot);
   }

   /** Blocks this board treats as work. */
   public abstract boolean isExpectedBlock(Level world, BlockPos pos);

   /** Start the AI that acts on one found block. */
   protected abstract void startWorkOn(BlockPos pos);

   /** The tool this board must hold to work, or {@code null} if it needs none. */
   protected IStackFilter toolFilter() {
      return null;
   }

   @Override
   public final void update() {
      if (this.needsTool()) {
         this.startDelegateAI(new AIRobotFetchAndEquipItemStack(this.robot, this.toolFilter()));
         return;
      }

      if (this.robot.containsItems() && !this.robot.hasFreeSlot()) {
         this.startDelegateAI(new AIRobotGotoStationAndUnload(this.robot));
         return;
      }

      if (this.blockFound != null) {
         this.startWorkOn(this.blockFound);
         return;
      }

      this.startDelegateAI(new AIRobotSearchAndGotoBlock(this.robot, false, this.workFilter()));
   }

   @Override
   public final void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotFetchAndEquipItemStack || ai instanceof AIRobotGotoStationAndUnload) {
         if (!ai.success()) {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }

         return;
      }

      if (ai instanceof AIRobotGotoSleep) {
         return;
      }

      if (ai instanceof AIRobotSearchAndGotoBlock search) {
         if (search.success()) {
            this.blockFound = search.getBlockFound();
         } else {
            this.blockFound = null;
            this.deliverOrRest();
         }

         return;
      }

      this.releaseBlockFound();
      this.onWorkEnded(ai);
   }

   /** Hook for a board that needs to react to its own work AI finishing. The block is already released. */
   protected void onWorkEnded(AIRobot ai) {
   }

   private boolean needsTool() {
      IStackFilter tool = this.toolFilter();
      if (tool == null) {
         return false;
      }

      ItemStack held = this.robot.getHeldItem();
      return held.isEmpty() || (held.isDamageableItem() && held.getDamageValue() >= held.getMaxDamage());
   }

   /** Nothing left to work on: bring the load home before resting, rather than resting on top of it. */
   private void deliverOrRest() {
      if (this.robot.containsItems()) {
         this.startDelegateAI(new AIRobotGotoStationAndUnload(this.robot));
      } else {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }

   private IBlockFilter workFilter() {
      // Resolved once per search, not once per candidate: the scan walks thousands of positions and the gate
      // filter is a fresh list plus an ItemStack per call.
      Set<Item> allowed = gateFilterItems(this.robot);
      return (world, pos) -> BoardRobotGenericSearchBlock.this.isExpectedBlock(world, pos)
         && (allowed == null || allowed.contains(world.getBlockState(pos).getBlock().asItem()));
   }

   /** The items the station's gate filter allows, or {@code null} when the gate sets no filter. */
   private static Set<Item> gateFilterItems(EntityRobotBase robot) {
      List<ItemStack> stacks = StationActions.getGateFilterStacks(robot.getLinkedStation());
      if (stacks.isEmpty()) {
         return null;
      }

      Set<Item> items = new HashSet<>(stacks.size());
      for (ItemStack stack : stacks) {
         items.add(stack.getItem());
      }

      return items;
   }

   protected BlockPos blockFound() {
      return this.blockFound;
   }

   protected void releaseBlockFound() {
      if (this.blockFound != null) {
         this.robot.getRegistry().release(new ResourceIdBlock(this.blockFound));
         this.blockFound = null;
      }
   }

   @Override
   public void end() {
      this.releaseBlockFound();
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.blockFound != null) {
         nbt.putIntArray("indexStored", new int[]{this.blockFound.getX(), this.blockFound.getY(), this.blockFound.getZ()});
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = BcNbt.getIntArray(nbt, "indexStored");
      if (arr.length == 3) {
         this.blockFound = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }
}
