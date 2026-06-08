/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class PipeBehaviourDiamondItem extends PipeBehaviourDiamond {
   public PipeBehaviourDiamondItem(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourDiamondItem(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @PipeEventHandler
   public void sideCheck(PipeEventItem.SideCheck sideCheck) {
      ItemStack toCompare = sideCheck.stack;

      for (Direction face : Direction.values()) {
         if (sideCheck.isAllowed(face) && this.pipe.isConnected(face)) {
            int offset = 9 * face.ordinal();
            boolean sideAllowed = false;
            boolean foundItem = false;

            for (int i = 0; i < 9; i++) {
               ItemStack compareTo = this.filters.getStackInSlot(offset + i);
               if (!compareTo.isEmpty()) {
                  foundItem = true;
                  if (StackUtil.isMatchingItemOrList(compareTo, toCompare)) {
                     sideAllowed = true;
                     break;
                  }
               }
            }

            if (foundItem) {
               if (sideAllowed) {
                  sideCheck.increasePriority(face, 12);
               } else {
                  sideCheck.disallow(face);
               }
            }
         }
      }
   }

   @PipeEventHandler
   public void split(PipeEventItem.Split split) {
      Direction[] allSides = split.getAllPossibleDestinations().toArray(new Direction[0]);
      if (allSides.length != 0 && allSides.length != 1) {
         PipeEventItem.ItemEntry[] items = split.items.toArray(new PipeEventItem.ItemEntry[0]);
         split.items.clear();

         for (PipeEventItem.ItemEntry item : items) {
            int[] countPerSide = new int[allSides.length];
            int totalCount = 0;

            for (int s = 0; s < allSides.length; s++) {
               int offset = 9 * allSides[s].ordinal();

               for (int i = 0; i < 9; i++) {
                  ItemStack compareTo = this.filters.getStackInSlot(offset + i);
                  if (!compareTo.isEmpty() && StackUtil.isMatchingItemOrList(compareTo, item.stack)) {
                     int count = compareTo.getCount();
                     totalCount += count;
                     countPerSide[s] += count;
                  }
               }
            }

            if (totalCount == 0) {
               totalCount = allSides.length;
               Arrays.fill(countPerSide, 1);
            } else {
               int hcf = countPerSide[0];

               for (int c : countPerSide) {
                  hcf = MathUtil.findHighestCommonFactor(hcf, c);
               }

               if (hcf != 1) {
                  totalCount /= hcf;

                  for (int i = 0; i < countPerSide.length; i++) {
                     countPerSide[i] /= hcf;
                  }
               }
            }

            PipeEventItem.ItemEntry[] entries = new PipeEventItem.ItemEntry[allSides.length];
            ItemStack toSplit = item.stack;
            if (toSplit.getCount() >= totalCount) {
               int leftOver = toSplit.getCount() % totalCount;
               int multiples = (toSplit.getCount() - leftOver) / totalCount;

               for (int s = 0; s < allSides.length; s++) {
                  ItemStack toSide = toSplit.copy();
                  toSide.setCount(countPerSide[s] * multiples);
                  entries[s] = new PipeEventItem.ItemEntry(item.colour, toSide, item.from);
                  List<Direction> dests = new ArrayList<>(1);
                  dests.add(allSides[s]);
                  entries[s].to = dests;
               }

               toSplit.setCount(leftOver);
            }

            if (!toSplit.isEmpty()) {
               int[] randLookup = new int[totalCount];
               int j = 0;

               for (int s = 0; s < allSides.length; s++) {
                  int len = countPerSide[s];
                  Arrays.fill(randLookup, j, j + len, s);
                  j += len;
               }

               for (; !toSplit.isEmpty(); toSplit.shrink(1)) {
                  int rand = this.pipe.getHolder().getPipeWorld().getRandom().nextInt(totalCount);
                  int face = randLookup[rand];
                  if (entries[face] == null) {
                     ItemStack stack = toSplit.copy();
                     stack.setCount(1);
                     PipeEventItem.ItemEntry entry = new PipeEventItem.ItemEntry(item.colour, stack, item.from);
                     List<Direction> dests = entry.to = new ArrayList<>(1);
                     dests.add(allSides[face]);
                     entries[face] = entry;
                  } else {
                     entries[face].stack.grow(1);
                  }
               }
            }

            for (int s = 0; s < allSides.length; s++) {
               PipeEventItem.ItemEntry entry = entries[s];
               if (entry != null) {
                  split.items.add(entry);
               }
            }
         }
      }
   }
}
