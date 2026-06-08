/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class PipeEventItem extends PipeEvent {
   public final IFlowItems flow;

   protected PipeEventItem(IPipeHolder holder, IFlowItems flow) {
      super(holder);
      this.flow = flow;
   }

   @Deprecated
   protected PipeEventItem(boolean canBeCancelled, IPipeHolder holder, IFlowItems flow) {
      super(canBeCancelled, holder);
      this.flow = flow;
   }

   public static class Drop extends PipeEventItem {
      private final ItemEntity entity;

      public Drop(IPipeHolder holder, IFlowItems flow, ItemEntity entity) {
         super(holder, flow);
         this.entity = entity;
      }

      @Nonnull
      public ItemStack getStack() {
         ItemStack item = this.entity.getItem();
         return item.isEmpty() ? ItemStack.EMPTY : item;
      }

      public void setStack(ItemStack stack) {
         if (stack == null) {
            throw new NullPointerException("stack");
         }

         if (stack.isEmpty()) {
            this.entity.setItem(ItemStack.EMPTY);
         } else {
            this.entity.setItem(stack);
         }
      }

      public ItemEntity getEntity() {
         return this.entity;
      }
   }

   public abstract static class Ejected extends PipeEventItem {
      public final ItemStack inserted;
      @Nonnull
      private ItemStack excess;
      public final Direction to;

      protected Ejected(IPipeHolder holder, IFlowItems flow, ItemStack inserted, ItemStack excess, Direction to) {
         super(holder, flow);
         this.inserted = inserted;
         this.excess = excess;
         this.to = to;
      }

      @Nonnull
      public ItemStack getExcess() {
         return this.excess;
      }

      public void setExcess(ItemStack stack) {
         if (stack == null) {
            throw new NullPointerException("stack");
         }

         this.excess = stack;
      }

      public static class IntoPipe extends PipeEventItem.Ejected {
         public final IFlowItems otherPipe;

         public IntoPipe(IPipeHolder holder, IFlowItems flow, ItemStack inserted, ItemStack excess, Direction to, IFlowItems otherPipe) {
            super(holder, flow, inserted, excess, to);
            this.otherPipe = otherPipe;
         }
      }

      public static class IntoTile extends PipeEventItem.Ejected {
         public final BlockEntity tile;

         public IntoTile(IPipeHolder holder, IFlowItems flow, ItemStack inserted, ItemStack excess, Direction to, BlockEntity tile) {
            super(holder, flow, inserted, excess, to);
            this.tile = tile;
         }
      }
   }

   public static class FindDest extends PipeEventItem.OrderedEvent {
      public final ImmutableList<PipeEventItem.ItemEntry> items;

      public FindDest(IPipeHolder holder, IFlowItems flow, List<EnumSet<Direction>> orderedDestinations, ImmutableList<PipeEventItem.ItemEntry> items) {
         super(holder, flow, orderedDestinations);
         this.items = items;
      }
   }

   public static class ItemEntry {
      public DyeColor colour;
      @Nonnull
      public ItemStack stack;
      public Direction from;
      @Nullable
      public List<Direction> to;

      public ItemEntry(DyeColor colour, @Nonnull ItemStack stack, Direction from) {
         this.colour = colour;
         this.stack = stack;
         this.from = from;
      }

      public void prepare(DyeColor colour, @Nonnull ItemStack stack, Direction from) {
         this.colour = colour;
         this.stack = stack;
         this.from = from;
         this.to = null;
      }
   }

   public static class ModifySpeed extends PipeEventItem {
      public PipeEventItem.ItemEntry item;
      public double currentSpeed;
      public double targetSpeed = 0.0;
      public double maxSpeedChange = 0.0;

      public ModifySpeed(IPipeHolder holder, IFlowItems flow, PipeEventItem.ItemEntry item, double initSpeed) {
         super(holder, flow);
         this.item = item;
         this.currentSpeed = initSpeed;
      }

      public void prepare(PipeEventItem.ItemEntry item, double initSpeed) {
         this.item = item;
         this.currentSpeed = initSpeed;
         this.targetSpeed = 0.0;
         this.maxSpeedChange = 0.0;
      }

      public void modifyTo(double target, double maxDelta) {
         this.targetSpeed = target;
         this.maxSpeedChange = maxDelta;
      }
   }

   public static class OnInsert extends PipeEventItem.ReachDest {
      public final Direction from;

      public OnInsert(IPipeHolder holder, IFlowItems flow, DyeColor colour, @Nonnull ItemStack stack, Direction from) {
         super(holder, flow, colour, stack);
         this.from = from;
      }
   }

   public abstract static class OrderedEvent extends PipeEventItem {
      public final List<EnumSet<Direction>> orderedDestinations;

      public OrderedEvent(IPipeHolder holder, IFlowItems flow, List<EnumSet<Direction>> orderedDestinations) {
         super(holder, flow);
         this.orderedDestinations = orderedDestinations;
      }

      public EnumSet<Direction> getAllPossibleDestinations() {
         EnumSet<Direction> set = EnumSet.noneOf(Direction.class);

         for (EnumSet<Direction> e : this.orderedDestinations) {
            set.addAll(e);
         }

         return set;
      }

      public ImmutableList<Direction> generateRandomOrder() {
         Builder<Direction> builder = ImmutableList.builder();

         for (EnumSet<Direction> set : this.orderedDestinations) {
            List<Direction> faces = new ArrayList<>(set);
            Collections.shuffle(faces);
            builder.addAll(faces);
         }

         return builder.build();
      }
   }

   public static class ReachCenter extends PipeEventItem.ReachDest {
      public Direction from;

      public ReachCenter(IPipeHolder holder, IFlowItems flow, DyeColor colour, @Nonnull ItemStack stack, Direction from) {
         super(holder, flow, colour, stack);
         this.from = from;
      }

      public void prepare(DyeColor colour, @Nonnull ItemStack stack, Direction from) {
         this.colour = colour;
         this.setStack(stack);
         this.from = from;
      }
   }

   public abstract static class ReachDest extends PipeEventItem {
      public DyeColor colour;
      @Nonnull
      private ItemStack stack;

      public ReachDest(IPipeHolder holder, IFlowItems flow, DyeColor colour, @Nonnull ItemStack stack) {
         super(holder, flow);
         this.colour = colour;
         this.stack = stack;
      }

      @Nonnull
      public ItemStack getStack() {
         return this.stack;
      }

      public void setStack(ItemStack stack) {
         if (stack == null) {
            throw new NullPointerException("stack");
         }

         this.stack = stack;
      }
   }

   public static class ReachEnd extends PipeEventItem.ReachDest {
      public final Direction to;

      public ReachEnd(IPipeHolder holder, IFlowItems flow, DyeColor colour, @Nonnull ItemStack stack, Direction to) {
         super(holder, flow, colour, stack);
         this.to = to;
      }
   }

   public static class SideCheck extends PipeEventItem {
      public DyeColor colour;
      public Direction from;
      @Nonnull
      public ItemStack stack;
      private final int[] priority = new int[6];
      private final EnumSet<Direction> allowed = EnumSet.allOf(Direction.class);

      public SideCheck(IPipeHolder holder, IFlowItems flow, DyeColor colour, Direction from, @Nonnull ItemStack stack) {
         super(holder, flow);
         this.colour = colour;
         this.from = from;
         this.stack = stack;
      }

      public void prepare(DyeColor colour, Direction from, @Nonnull ItemStack stack) {
         this.colour = colour;
         this.from = from;
         this.stack = stack;
         Arrays.fill(this.priority, 0);
         this.allowed.clear();
         this.allowed.addAll(EnumSet.allOf(Direction.class));
      }

      public boolean isAllowed(Direction side) {
         return this.allowed.contains(side);
      }

      public void disallow(Direction... sides) {
         for (Direction side : sides) {
            this.allowed.remove(side);
         }
      }

      public void disallowAll(Collection<Direction> sides) {
         this.allowed.removeAll(sides);
      }

      public void disallowAllExcept(Direction... sides) {
         this.allowed.retainAll(Lists.newArrayList(sides));
      }

      public void disallowAll() {
         this.allowed.clear();
      }

      public void increasePriority(Direction side) {
         this.increasePriority(side, 1);
      }

      public void increasePriority(Direction side, int by) {
         this.priority[side.ordinal()] -= by;
      }

      public void decreasePriority(Direction side) {
         this.decreasePriority(side, 1);
      }

      public void decreasePriority(Direction side, int by) {
         this.increasePriority(side, -by);
      }

      public List<EnumSet<Direction>> getOrder() {
         switch (this.allowed.size()) {
            case 0:
               return ImmutableList.of();
            case 1:
               return ImmutableList.of(this.allowed);
            default:
               int val = this.priority[0];

               for (int i = 1; i < this.priority.length; i++) {
                  if (this.priority[i] != val) {
                     int[] ordered = Arrays.copyOf(this.priority, 6);
                     Arrays.sort(ordered);
                     i = 0;
                     List<EnumSet<Direction>> list = Lists.newArrayList();

                     for (int ix = 0; ix < 6; ix++) {
                        int current = ordered[ix];
                        if (ix == 0 || current != i) {
                           i = current;
                           EnumSet<Direction> set = EnumSet.noneOf(Direction.class);

                           for (Direction face : Direction.values()) {
                              if (this.allowed.contains(face) && this.priority[face.ordinal()] == current) {
                                 set.add(face);
                              }
                           }

                           if (set.size() > 0) {
                              list.add(set);
                           }
                        }
                     }

                     return list;
                  }
               }

               return ImmutableList.of(this.allowed);
         }
      }
   }

   public static class Split extends PipeEventItem.OrderedEvent {
      public final List<PipeEventItem.ItemEntry> items = new ArrayList<>();

      public Split(IPipeHolder holder, IFlowItems flow, List<EnumSet<Direction>> order, PipeEventItem.ItemEntry toSplit) {
         super(holder, flow, order);
         this.items.add(toSplit);
      }
   }

   public static class TryBounce extends PipeEventItem {
      public DyeColor colour;
      public Direction from;
      @Nonnull
      public ItemStack stack;
      public boolean canBounce = false;

      public TryBounce(IPipeHolder holder, IFlowItems flow, DyeColor colour, Direction from, @Nonnull ItemStack stack) {
         super(holder, flow);
         this.colour = colour;
         this.from = from;
         this.stack = stack;
      }

      public void prepare(DyeColor colour, Direction from, @Nonnull ItemStack stack) {
         this.colour = colour;
         this.from = from;
         this.stack = stack;
         this.canBounce = false;
      }
   }

   public static class TryInsert extends PipeEventItem {
      public final DyeColor colour;
      public final Direction from;
      @Nonnull
      public final ItemStack attempting;
      public int accepted;

      public TryInsert(IPipeHolder holder, IFlowItems flow, DyeColor colour, Direction from, @Nonnull ItemStack attempting) {
         super(true, holder, flow);
         this.colour = colour;
         this.from = from;
         this.attempting = attempting;
         this.accepted = attempting.getCount();
      }

      @Override
      public void cancel() {
         super.cancel();
      }
   }
}
