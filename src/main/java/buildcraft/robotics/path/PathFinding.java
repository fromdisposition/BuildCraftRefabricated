/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PathFinding {
   public static final int PATH_ITERATIONS = 1000;

   private final Level world;
   private final BlockPos end;
   private final double maxDistanceToEndSq;
   private final float maxTotalDistanceSq;

   private final Map<BlockPos, Node> openList = new HashMap<>();
   private final Map<BlockPos, Node> closedList = new HashMap<>();

   private Node nextIteration;
   private LinkedList<BlockPos> result;
   private boolean endReached;

   public PathFinding(Level world, BlockPos start, BlockPos end) {
      this(world, start, end, 0.0, 0.0F);
   }

   public PathFinding(Level world, BlockPos start, BlockPos end, double maxDistanceToEnd) {
      this(world, start, end, maxDistanceToEnd, 0.0F);
   }

   public PathFinding(Level world, BlockPos start, BlockPos end, double maxDistanceToEnd, float maxTotalDistance) {
      this.world = world;
      this.end = end;
      this.maxDistanceToEndSq = maxDistanceToEnd * maxDistanceToEnd;
      this.maxTotalDistanceSq = maxTotalDistance * maxTotalDistance;

      Node startNode = new Node();
      startNode.parent = null;
      startNode.movementCost = 0.0;
      startNode.destinationCost = distanceSq(start, end);
      startNode.totalWeight = startNode.movementCost + startNode.destinationCost;
      startNode.index = start;
      this.openList.put(start, startNode);
      this.nextIteration = startNode;
   }

   public static boolean isSoftBlock(Level world, BlockPos pos) {
      if (pos.getY() < world.getMinY() || pos.getY() > world.getMaxY()) {
         return false;
      }

      BlockState state = world.getBlockState(pos);
      return state.isAir() || state.getCollisionShape(world, pos).isEmpty();
   }

   public void iterate(int itNumber) {
      for (int i = 0; i < itNumber; i++) {
         if (this.nextIteration == null) {
            return;
         }

         if (this.endReached) {
            this.result = new LinkedList<>();

            while (this.nextIteration != null) {
               this.result.addFirst(this.nextIteration.index);
               this.nextIteration = this.nextIteration.parent;
            }

            return;
         }

         this.nextIteration = this.iterate(this.nextIteration);
      }
   }

   public boolean isDone() {
      return this.nextIteration == null;
   }

   public LinkedList<BlockPos> getResult() {
      return this.result != null ? this.result : new LinkedList<>();
   }

   public BlockPos end() {
      return this.end;
   }

   private Node iterate(Node from) {
      this.openList.remove(from.index);
      this.closedList.put(from.index, from);

      ArrayList<Node> nodes = new ArrayList<>();
      byte[][][] resultMoves = this.movements(from);

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (resultMoves[dx + 1][dy + 1][dz + 1] == 0) {
                  continue;
               }

               BlockPos index = from.index.offset(dx, dy, dz);
               Node nextNode = new Node();
               nextNode.parent = from;
               nextNode.index = index;

               if (resultMoves[dx + 1][dy + 1][dz + 1] == 2) {
                  this.endReached = true;
                  return nextNode;
               }

               nextNode.movementCost = from.movementCost + distanceSq(index, from.index);
               nextNode.destinationCost = distanceSq(index, this.end);
               nextNode.totalWeight = nextNode.movementCost + nextNode.destinationCost;

               if (this.maxTotalDistanceSq > 0.0F && nextNode.totalWeight > this.maxTotalDistanceSq) {
                  this.closedList.putIfAbsent(index, nextNode);
                  continue;
               }

               if (this.closedList.containsKey(index)) {
                  continue;
               } else if (this.openList.containsKey(index)) {
                  Node tentative = this.openList.get(index);
                  if (tentative.movementCost < nextNode.movementCost) {
                     nextNode = tentative;
                  } else {
                     this.openList.put(index, nextNode);
                  }
               } else {
                  this.openList.put(index, nextNode);
               }

               nodes.add(nextNode);
            }
         }
      }

      nodes.addAll(this.openList.values());
      return findSmallerWeight(nodes);
   }

   private static Node findSmallerWeight(Collection<Node> collection) {
      Node found = null;

      for (Node n : collection) {
         if (found == null || n.totalWeight < found.totalWeight) {
            found = n;
         }
      }

      return found;
   }

   private boolean endReached(BlockPos pos) {
      if (this.maxDistanceToEndSq == 0.0) {
         return this.end.equals(pos);
      }

      return isSoftBlock(this.world, pos) && distanceSq(pos, this.end) <= this.maxDistanceToEndSq;
   }

   private byte[][][] movements(Node from) {
      byte[][][] resultMoves = new byte[3][3][3];

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               BlockPos pos = from.index.offset(dx, dy, dz);
               if (pos.getY() < this.world.getMinY()) {
                  resultMoves[dx + 1][dy + 1][dz + 1] = 0;
               } else if (this.endReached(pos)) {
                  resultMoves[dx + 1][dy + 1][dz + 1] = 2;
               } else if (!isSoftBlock(this.world, pos)) {
                  resultMoves[dx + 1][dy + 1][dz + 1] = 0;
               } else {
                  resultMoves[dx + 1][dy + 1][dz + 1] = 1;
               }
            }
         }
      }

      resultMoves[1][1][1] = 0;
      pruneDiagonals(resultMoves);
      return resultMoves;
   }

   private static void pruneDiagonals(byte[][][] m) {
      if (m[0][1][1] == 0) {
         for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
               m[0][i][j] = 0;
            }
         }
      }

      if (m[2][1][1] == 0) {
         for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
               m[2][i][j] = 0;
            }
         }
      }

      if (m[1][0][1] == 0) {
         for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
               m[i][0][j] = 0;
            }
         }
      }

      if (m[1][2][1] == 0) {
         for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
               m[i][2][j] = 0;
            }
         }
      }

      if (m[1][1][0] == 0) {
         for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
               m[i][j][0] = 0;
            }
         }
      }

      if (m[1][1][2] == 0) {
         for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
               m[i][j][2] = 0;
            }
         }
      }

      pruneEdge(m, 0, 0, 1, new int[][]{{0, 0, 0}, {0, 0, 2}});
      pruneEdge(m, 0, 2, 1, new int[][]{{0, 2, 0}, {0, 2, 2}});
      pruneEdge(m, 2, 0, 1, new int[][]{{2, 0, 0}, {2, 0, 2}});
      pruneEdge(m, 2, 2, 1, new int[][]{{2, 2, 0}, {2, 2, 2}});
      pruneEdge(m, 0, 1, 0, new int[][]{{0, 0, 0}, {0, 2, 0}});
      pruneEdge(m, 0, 1, 2, new int[][]{{0, 0, 2}, {0, 2, 2}});
      pruneEdge(m, 2, 1, 0, new int[][]{{2, 0, 0}, {2, 2, 0}});
      pruneEdge(m, 2, 1, 2, new int[][]{{2, 0, 2}, {2, 2, 2}});
      pruneEdge(m, 1, 0, 0, new int[][]{{0, 0, 0}, {2, 0, 0}});
      pruneEdge(m, 1, 0, 2, new int[][]{{0, 0, 2}, {2, 0, 2}});
      pruneEdge(m, 1, 2, 0, new int[][]{{0, 2, 0}, {2, 2, 0}});
      pruneEdge(m, 1, 2, 2, new int[][]{{0, 2, 2}, {2, 2, 2}});
   }

   private static void pruneEdge(byte[][][] m, int a, int b, int c, int[][] targets) {
      if (m[a][b][c] == 0) {
         for (int[] t : targets) {
            m[t[0]][t[1]][t[2]] = 0;
         }
      }
   }

   private static double distanceSq(BlockPos a, BlockPos b) {
      double dx = (double)a.getX() - (double)b.getX();
      double dy = (double)a.getY() - (double)b.getY();
      double dz = (double)a.getZ() - (double)b.getZ();
      return dx * dx + dy * dy + dz * dz;
   }

   private static final class Node {
      Node parent;
      double movementCost;
      double destinationCost;
      double totalWeight;
      BlockPos index;
   }
}
