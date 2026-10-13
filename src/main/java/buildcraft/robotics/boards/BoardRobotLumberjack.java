/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.nbt.BcNbt;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoardRobotLumberjack extends BoardRobotGenericBreakBlock {
   private static final int MAX_TREE_LOGS = 2048;
   private static final int MAX_TREE_HORIZONTAL_RADIUS = 24;
   private static final int MAX_TREE_VERTICAL_RADIUS = 48;

   private final Set<BlockPos> currentTree = new HashSet<>();
   private BlockPos treeOrigin;

   public BoardRobotLumberjack(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("lumberjack");
   }

   @Override
   public boolean isExpectedTool(ItemStack stack) {
      return !stack.isEmpty() && stack.is(ItemTags.AXES);
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      if (!isLog(world, pos)) {
         return false;
      }

      return this.currentTree.isEmpty() || this.currentTree.contains(pos);
   }

   @Override
   protected void onBlockFound(BlockPos pos) {
      if (this.currentTree.isEmpty()) {
         this.collectConnectedTree(pos);
      }
   }

   @Override
   protected boolean onSearchFailed() {
      if (this.currentTree.isEmpty()) {
         return false;
      }

      this.clearCurrentTree();
      return true;
   }

   @Override
   protected void onWorkEnded(AIRobot ai) {
      this.pruneCurrentTree();
   }

   private void collectConnectedTree(BlockPos root) {
      this.clearCurrentTree();
      Level level = this.robot.level();
      if (!isLog(level, root)) {
         return;
      }

      this.treeOrigin = root.immutable();
      IZone zone = this.robot.getZoneToWork();
      ArrayDeque<BlockPos> open = new ArrayDeque<>();
      Set<BlockPos> visited = new HashSet<>();
      open.add(this.treeOrigin);

      while (!open.isEmpty() && this.currentTree.size() < MAX_TREE_LOGS) {
         BlockPos pos = open.removeFirst();
         if (!visited.add(pos) || !this.isInsideTreeScanBounds(pos) || !isLog(level, pos)) {
            continue;
         }

         if (zone != null && !zone.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
            continue;
         }

         this.currentTree.add(pos);

         for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx == 0 && dy == 0 && dz == 0) {
                     continue;
                  }

                  BlockPos next = pos.offset(dx, dy, dz);
                  if (!visited.contains(next) && this.isInsideTreeScanBounds(next)) {
                     open.addLast(next);
                  }
               }
            }
         }
      }

      if (this.currentTree.isEmpty()) {
         this.treeOrigin = null;
      }
   }

   private void pruneCurrentTree() {
      if (this.currentTree.isEmpty()) {
         this.treeOrigin = null;
         return;
      }

      Level level = this.robot.level();
      this.currentTree.removeIf(pos -> isChunkLoaded(level, pos) && !isLog(level, pos));
      if (this.currentTree.isEmpty()) {
         this.treeOrigin = null;
      }
   }

   private boolean isInsideTreeScanBounds(BlockPos pos) {
      if (this.treeOrigin == null) {
         return false;
      }

      return Math.abs(pos.getX() - this.treeOrigin.getX()) <= MAX_TREE_HORIZONTAL_RADIUS
         && Math.abs(pos.getZ() - this.treeOrigin.getZ()) <= MAX_TREE_HORIZONTAL_RADIUS
         && Math.abs(pos.getY() - this.treeOrigin.getY()) <= MAX_TREE_VERTICAL_RADIUS;
   }

   private void clearCurrentTree() {
      this.currentTree.clear();
      this.treeOrigin = null;
   }

   private static boolean isChunkLoaded(Level level, BlockPos pos) {
      return level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }

   private static boolean isLog(Level level, BlockPos pos) {
      return isChunkLoaded(level, pos) && BuildCraftAPI.getWorldProperty("wood").get(level, pos);
   }

   @Override
   public void end() {
      super.end();
      this.clearCurrentTree();
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.treeOrigin != null) {
         nbt.putIntArray("treeOrigin", new int[]{this.treeOrigin.getX(), this.treeOrigin.getY(), this.treeOrigin.getZ()});
      }

      if (!this.currentTree.isEmpty()) {
         int[] positions = new int[this.currentTree.size() * 3];
         int index = 0;
         for (BlockPos pos : this.currentTree) {
            positions[index++] = pos.getX();
            positions[index++] = pos.getY();
            positions[index++] = pos.getZ();
         }

         nbt.putIntArray("currentTree", positions);
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      this.clearCurrentTree();
      int[] origin = BcNbt.getIntArray(nbt, "treeOrigin");
      if (origin.length == 3) {
         this.treeOrigin = new BlockPos(origin[0], origin[1], origin[2]);
      }

      int[] positions = BcNbt.getIntArray(nbt, "currentTree");
      for (int i = 0; i + 2 < positions.length; i += 3) {
         this.currentTree.add(new BlockPos(positions[i], positions[i + 1], positions[i + 2]));
      }

      if (this.currentTree.isEmpty()) {
         this.treeOrigin = null;
      } else if (this.treeOrigin == null) {
         this.treeOrigin = this.currentTree.iterator().next();
      }
   }
}
