/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.path;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;

/**
 * Incremental A* over block cells for a flying robot. A cell is passable when it has no collision shape at all, and
 * a diagonal step is allowed only when every cell the robot's box sweeps through is passable, so a path never cuts a
 * corner through a block. The search is budgeted per tick, per robot and across all robots, and gives up after a
 * fixed number of expansions so an enclosed target costs a bounded amount of work.
 */
public final class PathFinding {
   public static final int MAX_NODES = 16384;
   private static final int NODES_PER_TICK = 512;
   private static final int NODES_PER_TICK_ALL_ROBOTS = 2048;
   private static final int SMOOTH_LOOKAHEAD = 16;
   private static final float[] STEP_COST = {0.0F, 1.0F, 1.4142135F, 1.7320508F};
   private static final byte SOFT = 1;
   private static final byte HARD = 2;

   private static long budgetTick = Long.MIN_VALUE;
   private static int budgetLeft;

   private final Level level;
   private final BlockPos end;
   private final double maxDistanceToEndSq;
   private final BinaryHeap open = new BinaryHeap();
   private final Long2ObjectOpenHashMap<Node> nodes = new Long2ObjectOpenHashMap<>();
   private final Long2ByteOpenHashMap softness = new Long2ByteOpenHashMap();
   private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
   private final boolean[] neighbourhood = new boolean[27];

   private int expanded;
   private boolean done;
   private List<BlockPos> result;

   public PathFinding(Level level, BlockPos start, BlockPos end, double maxDistanceToEnd) {
      this.level = level;
      this.end = end;
      double distSq = maxDistanceToEnd * maxDistanceToEnd;
      if (distSq == 0.0 && !this.isSoft(end.getX(), end.getY(), end.getZ())) {
         distSq = 3.0;
      }

      this.maxDistanceToEndSq = distSq;

      if (this.isGoal(start.getX(), start.getY(), start.getZ())) {
         this.finish(List.of(start));
         return;
      }

      if (this.isGoal(end.getX(), end.getY(), end.getZ()) && FlightSweep.isClear(this::isSoft, feet(start), feet(end))) {
         this.finish(List.of(end));
         return;
      }

      Node first = new Node(start.getX(), start.getY(), start.getZ());
      first.g = 0.0F;
      first.h = this.heuristic(first);
      first.f = first.h;
      this.nodes.put(BlockPos.asLong(start.getX(), start.getY(), start.getZ()), first);
      this.open.insert(first);
   }

   public static boolean isSoftBlock(Level level, BlockPos pos) {
      return isSoftBlock(level, pos.getX(), pos.getY(), pos.getZ());
   }

   public static boolean isSoftBlock(Level level, int x, int y, int z) {
      if (y < level.getMinY() || y > level.getMaxY() || !level.hasChunk(x >> 4, z >> 4)) {
         return false;
      }

      BlockPos pos = new BlockPos(x, y, z);
      BlockState state = level.getBlockState(pos);
      return state.isAir() || state.getCollisionShape(level, pos).isEmpty();
   }

   public static Vec3 feet(BlockPos cell) {
      return new Vec3(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5);
   }

   public boolean isSoft(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      byte known = this.softness.get(key);
      if (known == 0) {
         boolean soft = y >= this.level.getMinY() && y <= this.level.getMaxY() && this.level.hasChunk(x >> 4, z >> 4) && this.readSoft(x, y, z);
         known = soft ? SOFT : HARD;
         this.softness.put(key, known);
      }

      return known == SOFT;
   }

   private boolean readSoft(int x, int y, int z) {
      this.cursor.set(x, y, z);
      BlockState state = this.level.getBlockState(this.cursor);
      return state.isAir() || state.getCollisionShape(this.level, this.cursor).isEmpty();
   }

   public void iterate() {
      if (this.done) {
         return;
      }

      int budget = takeBudget(this.level);
      while (budget-- > 0) {
         if (this.open.isEmpty()) {
            this.finish(null);
            return;
         }

         Node current = this.open.pop();
         current.closed = true;
         if (this.isGoal(current.x, current.y, current.z)) {
            this.finish(this.reconstruct(current));
            return;
         }

         if (++this.expanded > MAX_NODES) {
            this.finish(null);
            return;
         }

         this.expand(current);
      }
   }

   public boolean isDone() {
      return this.done;
   }

   public List<BlockPos> getResult() {
      return this.result;
   }

   public BlockPos end() {
      return this.end;
   }

   private static int takeBudget(Level level) {
      MinecraftServer server = level.getServer();
      long tick = server != null ? server.getTickCount() : level.getGameTime();
      if (tick != budgetTick) {
         budgetTick = tick;
         budgetLeft = NODES_PER_TICK_ALL_ROBOTS;
      }

      int taken = Math.min(NODES_PER_TICK, budgetLeft);
      budgetLeft -= taken;
      return taken;
   }

   private void expand(Node from) {
      boolean[] soft = this.neighbourhood;
      for (int i = 0; i < 27; i++) {
         soft[i] = this.isSoft(from.x + i / 9 - 1, from.y + i / 3 % 3 - 1, from.z + i % 3 - 1);
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if ((dx == 0 && dy == 0 && dz == 0) || !sweptCellsSoft(soft, dx, dy, dz)) {
                  continue;
               }

               int x = from.x + dx;
               int y = from.y + dy;
               int z = from.z + dz;
               float g = from.g + STEP_COST[Math.abs(dx) + Math.abs(dy) + Math.abs(dz)];
               long key = BlockPos.asLong(x, y, z);
               Node next = this.nodes.get(key);
               if (next == null) {
                  next = new Node(x, y, z);
                  next.g = g;
                  next.h = this.heuristic(next);
                  next.f = g + next.h;
                  next.cameFrom = from;
                  this.nodes.put(key, next);
                  this.open.insert(next);
               } else if (!next.closed && g < next.g) {
                  next.g = g;
                  next.cameFrom = from;
                  this.open.changeCost(next, g + next.h);
               }
            }
         }
      }
   }

   /** Every cell of the box spanned by the step, except the origin, must be passable: that is exactly what the robot's box sweeps. */
   private static boolean sweptCellsSoft(boolean[] soft, int dx, int dy, int dz) {
      for (int ix = 0; ix <= Math.abs(dx); ix++) {
         for (int iy = 0; iy <= Math.abs(dy); iy++) {
            for (int iz = 0; iz <= Math.abs(dz); iz++) {
               if ((ix != 0 || iy != 0 || iz != 0) && !soft[(ix * dx + 1) * 9 + (iy * dy + 1) * 3 + iz * dz + 1]) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private boolean isGoal(int x, int y, int z) {
      if (this.maxDistanceToEndSq == 0.0) {
         return x == this.end.getX() && y == this.end.getY() && z == this.end.getZ();
      }

      return this.end.distSqr(this.cursor.set(x, y, z)) <= this.maxDistanceToEndSq && this.isSoft(x, y, z);
   }

   private float heuristic(Node node) {
      float dx = node.x - this.end.getX();
      float dy = node.y - this.end.getY();
      float dz = node.z - this.end.getZ();
      return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   private List<BlockPos> reconstruct(Node goal) {
      List<BlockPos> cells = new ArrayList<>();
      for (Node node = goal; node != null; node = node.cameFrom) {
         cells.add(node.asBlockPos());
      }

      Collections.reverse(cells);
      return this.smooth(cells);
   }

   private List<BlockPos> smooth(List<BlockPos> cells) {
      if (cells.size() < 3) {
         return cells;
      }

      List<BlockPos> smoothed = new ArrayList<>();
      int i = 0;
      smoothed.add(cells.get(0));
      while (i < cells.size() - 1) {
         int reach = i + 1;
         int limit = Math.min(cells.size() - 1, i + SMOOTH_LOOKAHEAD);
         for (int j = i + 2; j <= limit; j++) {
            if (!FlightSweep.isClear(this::isSoft, feet(cells.get(i)), feet(cells.get(j)))) {
               break;
            }

            reach = j;
         }

         smoothed.add(cells.get(reach));
         i = reach;
      }

      return smoothed;
   }

   private void finish(List<BlockPos> path) {
      this.done = true;
      this.result = path;
      this.open.clear();
      this.nodes.clear();
   }
}
