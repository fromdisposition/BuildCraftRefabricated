/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import buildcraft.lib.fluids.FluidStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;

public abstract class PipeEventFluid extends PipeEvent {
   public final IFlowFluid flow;

   protected PipeEventFluid(IPipeHolder holder, IFlowFluid flow) {
      super(holder);
      this.flow = flow;
   }

   @Deprecated
   protected PipeEventFluid(boolean canBeCancelled, IPipeHolder holder, IFlowFluid flow) {
      super(canBeCancelled, holder);
      this.flow = flow;
   }

   public static class OnMoveToCentre extends PipeEventFluid {
      public FluidStack fluid;
      public final int[] fluidLeavingSide;
      public final int[] fluidEnteringCentre;
      private final int[] fluidLeaveCheck;
      private final int[] fluidEnterCheck;

      public OnMoveToCentre(IPipeHolder holder, IFlowFluid flow, FluidStack fluid, int[] fluidLeavingSide, int[] fluidEnteringCentre) {
         super(holder, flow);
         this.fluid = fluid;
         this.fluidLeavingSide = fluidLeavingSide;
         this.fluidEnteringCentre = fluidEnteringCentre;
         this.fluidLeaveCheck = new int[fluidLeavingSide.length];
         this.fluidEnterCheck = new int[fluidEnteringCentre.length];
         this.captureChecks();
      }

      public void prepare(FluidStack fluid) {
         this.fluid = fluid;
         this.captureChecks();
      }

      private void captureChecks() {
         System.arraycopy(this.fluidLeavingSide, 0, this.fluidLeaveCheck, 0, this.fluidLeavingSide.length);
         System.arraycopy(this.fluidEnteringCentre, 0, this.fluidEnterCheck, 0, this.fluidEnteringCentre.length);
      }

      @Override
      public String checkStateForErrors() {
         for (int i = 0; i < this.fluidLeavingSide.length; i++) {
            if (this.fluidLeavingSide[i] > this.fluidLeaveCheck[i]) {
               return "fluidLeavingSide["
                  + i
                  + "](="
                  + this.fluidLeavingSide[i]
                  + ") shouldn't be bigger than its original value!(="
                  + this.fluidLeaveCheck[i]
                  + ")";
            }

            if (this.fluidEnteringCentre[i] > this.fluidEnterCheck[i]) {
               return "fluidEnteringCentre["
                  + i
                  + "](="
                  + this.fluidEnteringCentre[i]
                  + ") shouldn't be bigger than its original value!(="
                  + this.fluidEnterCheck[i]
                  + ")";
            }

            if (this.fluidEnteringCentre[i] > this.fluidLeavingSide[i]) {
               return "fluidEnteringCentre["
                  + i
                  + "](="
                  + this.fluidEnteringCentre[i]
                  + ") shouldn't be bigger than fluidLeavingSide["
                  + i
                  + "](="
                  + this.fluidLeavingSide[i]
                  + ")";
            }
         }

         return super.checkStateForErrors();
      }
   }

   public static class PreMoveToCentre extends PipeEventFluid {
      public FluidStack fluid;
      public int totalAcceptable;
      public final int[] totalOffered;
      private final int[] totalOfferedCheck;
      public final int[] actuallyOffered;

      public PreMoveToCentre(IPipeHolder holder, IFlowFluid flow, FluidStack fluid, int totalAcceptable, int[] totalOffered, int[] actuallyOffered) {
         super(holder, flow);
         this.fluid = fluid;
         this.totalAcceptable = totalAcceptable;
         this.totalOffered = totalOffered;
         this.totalOfferedCheck = new int[totalOffered.length];
         this.actuallyOffered = actuallyOffered;
         this.captureChecks();
      }

      public void prepare(FluidStack fluid, int totalAcceptable) {
         this.fluid = fluid;
         this.totalAcceptable = totalAcceptable;
         this.captureChecks();
      }

      private void captureChecks() {
         System.arraycopy(this.totalOffered, 0, this.totalOfferedCheck, 0, this.totalOffered.length);
      }

      @Override
      public String checkStateForErrors() {
         for (int i = 0; i < this.totalOffered.length; i++) {
            if (this.totalOffered[i] != this.totalOfferedCheck[i]) {
               return "Changed totalOffered";
            }

            if (this.actuallyOffered[i] > this.totalOffered[i]) {
               return "actuallyOffered["
                  + i
                  + "](="
                  + this.actuallyOffered[i]
                  + ") shouldn't be greater than totalOffered["
                  + i
                  + "](="
                  + this.totalOffered[i]
                  + ")";
            }
         }

         return super.checkStateForErrors();
      }
   }

   public static class SideCheck extends PipeEventFluid {
      public FluidStack fluid;
      private final int[] priority = new int[6];
      private final EnumSet<Direction> allowed = EnumSet.allOf(Direction.class);

      public SideCheck(IPipeHolder holder, IFlowFluid flow, FluidStack fluid) {
         super(holder, flow);
         this.fluid = fluid;
      }

      public void prepare(FluidStack fluid) {
         this.fluid = fluid;
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

      public void disallowAllExcept(Direction side) {
         if (this.allowed.contains(side)) {
            this.allowed.clear();
            this.allowed.add(side);
         } else {
            this.allowed.clear();
         }
      }

      public void disallowAllExcept(Direction... sides) {
         switch (sides.length) {
            case 0:
               this.allowed.clear();
               return;
            case 1:
               this.disallowAllExcept(sides[0]);
               return;
            case 2:
               this.allowed.retainAll(EnumSet.of(sides[0], sides[1]));
               return;
            case 3:
               this.allowed.retainAll(EnumSet.of(sides[0], sides[1], sides[2]));
               return;
            case 4:
               this.allowed.retainAll(EnumSet.of(sides[0], sides[1], sides[2], sides[3]));
               return;
            default:
               EnumSet<Direction> except = EnumSet.noneOf(Direction.class);

               for (Direction face : sides) {
                  except.add(face);
               }

               this.allowed.retainAll(except);
         }
      }

      public void disallowAllExcept(Collection<Direction> sides) {
         this.allowed.retainAll(sides);
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

      
      public List<Direction> getOrderedDirections() {
         if (this.allowed.isEmpty()) {
            return Collections.emptyList();
         }

         if (this.allowed.size() == 1) {
            return List.of(this.allowed.iterator().next());
         }

         int val = this.priority[0];

         for (int i = 1; i < this.priority.length; i++) {
            if (this.priority[i] != val) {
               int[] ordered = Arrays.copyOf(this.priority, 6);
               Arrays.sort(ordered);
               List<Direction> list = new ArrayList<>();
               int lastPriority = Integer.MIN_VALUE;

               for (int ix = 0; ix < 6; ix++) {
                  int current = ordered[ix];
                  if (ix == 0 || current != lastPriority) {
                     lastPriority = current;

                     for (Direction face : Direction.values()) {
                        if (this.allowed.contains(face) && this.priority[face.ordinal()] == current) {
                           list.add(face);
                        }
                     }
                  }
               }

               return list;
            }
         }

         return new ArrayList<>(this.allowed);
      }

      public EnumSet<Direction> getOrder() {
         EnumSet<Direction> result = EnumSet.noneOf(Direction.class);

         for (Direction direction : this.getOrderedDirections()) {
            result.add(direction);
         }

         return result;
      }
   }

   public static class TryInsert extends PipeEventFluid {
      public Direction from;
      @Nonnull
      public FluidStack fluid;

      public TryInsert(IPipeHolder holder, IFlowFluid flow, Direction from, @Nonnull FluidStack fluid) {
         super(true, holder, flow);
         this.from = from;
         this.fluid = fluid;
      }

      public void prepare(Direction from, @Nonnull FluidStack fluid) {
         this.from = from;
         this.fluid = fluid;
      }
   }
}
