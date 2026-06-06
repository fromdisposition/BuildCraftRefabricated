package buildcraft.api.transport.pipe;

import buildcraft.lib.fluids.FluidStack;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
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
      public final FluidStack fluid;
      public final int[] fluidLeavingSide;
      public final int[] fluidEnteringCentre;
      private final int[] fluidLeaveCheck;
      private final int[] fluidEnterCheck;

      public OnMoveToCentre(IPipeHolder holder, IFlowFluid flow, FluidStack fluid, int[] fluidLeavingSide, int[] fluidEnteringCentre) {
         super(holder, flow);
         this.fluid = fluid;
         this.fluidLeavingSide = fluidLeavingSide;
         this.fluidEnteringCentre = fluidEnteringCentre;
         this.fluidLeaveCheck = Arrays.copyOf(fluidLeavingSide, fluidLeavingSide.length);
         this.fluidEnterCheck = Arrays.copyOf(fluidEnteringCentre, fluidEnteringCentre.length);
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
      public final FluidStack fluid;
      public final int totalAcceptable;
      public final int[] totalOffered;
      private final int[] totalOfferedCheck;
      public final int[] actuallyOffered;

      public PreMoveToCentre(IPipeHolder holder, IFlowFluid flow, FluidStack fluid, int totalAcceptable, int[] totalOffered, int[] actuallyOffered) {
         super(holder, flow);
         this.fluid = fluid;
         this.totalAcceptable = totalAcceptable;
         this.totalOffered = totalOffered;
         this.totalOfferedCheck = Arrays.copyOf(totalOffered, totalOffered.length);
         this.actuallyOffered = actuallyOffered;
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
      public final FluidStack fluid;
      private final int[] priority = new int[6];
      private final EnumSet<Direction> allowed = EnumSet.allOf(Direction.class);

      public SideCheck(IPipeHolder holder, IFlowFluid flow, FluidStack fluid) {
         super(holder, flow);
         this.fluid = fluid;
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

      public EnumSet<Direction> getOrder() {
         if (this.allowed.isEmpty()) {
            return EnumSet.noneOf(Direction.class);
         }

         if (this.allowed.size() == 1) {
            return this.allowed;
         }

         int val = this.priority[0];

         for (int i = 1; i < this.priority.length; i++) {
            if (this.priority[i] != val) {
               int[] ordered = Arrays.copyOf(this.priority, 6);
               Arrays.sort(ordered);
               i = 0;

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
                        return set;
                     }
                  }
               }

               return EnumSet.noneOf(Direction.class);
            }
         }

         return this.allowed;
      }
   }

   public static class TryInsert extends PipeEventFluid {
      public final Direction from;
      @Nonnull
      public final FluidStack fluid;

      public TryInsert(IPipeHolder holder, IFlowFluid flow, Direction from, @Nonnull FluidStack fluid) {
         super(true, holder, flow);
         this.from = from;
         this.fluid = fluid;
      }
   }
}
