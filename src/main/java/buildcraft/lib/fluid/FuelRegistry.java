package buildcraft.lib.fluid;

import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public enum FuelRegistry implements IFuelManager {
   INSTANCE;

   private final List<IFuel> fuels = new LinkedList<>();

   @Override
   public <F extends IFuel> F addFuel(F fuel) {
      this.fuels.add(fuel);
      return fuel;
   }

   @Override
   public IFuel addFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
      return this.addFuel(new FuelRegistry.Fuel(fluid, powerPerCycle, totalBurningTime));
   }

   @Override
   public IFuelManager.IDirtyFuel addDirtyFuel(FluidStack fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
      return this.addFuel(new FuelRegistry.DirtyFuel(fuel, powerPerCycle, totalBurningTime, residue));
   }

   @Override
   public Collection<IFuel> getFuels() {
      return this.fuels;
   }

   @Override
   public IFuel getFuel(FluidStack fluid) {
      if (fluid != null && !fluid.isEmpty()) {
         for (IFuel fuel : this.fuels) {
            if (FluidStack.isSameFluid(fuel.getFluid(), fluid) || FluidUtilBC.areFluidsEqual(fuel.getFluid().getFluid(), fluid.getFluid())) {
               return fuel;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static class DirtyFuel extends FuelRegistry.Fuel implements IFuelManager.IDirtyFuel {
      private final FluidStack residue;

      public DirtyFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime, FluidStack residue) {
         super(fluid, powerPerCycle, totalBurningTime);
         this.residue = residue;
      }

      @Override
      public FluidStack getResidue() {
         return this.residue;
      }
   }

   public static class Fuel implements IFuel {
      private final FluidStack fluid;
      private final long powerPerCycle;
      private final int totalBurningTime;

      public Fuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
         this.fluid = fluid;
         this.powerPerCycle = powerPerCycle;
         this.totalBurningTime = totalBurningTime;
      }

      @Override
      public FluidStack getFluid() {
         return this.fluid;
      }

      @Override
      public long getPowerPerCycle() {
         return this.powerPerCycle;
      }

      @Override
      public int getTotalBurningTime() {
         return this.totalBurningTime;
      }
   }
}
