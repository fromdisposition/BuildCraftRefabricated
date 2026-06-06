package buildcraft.api.fuels;

import buildcraft.lib.fluids.FluidStack;
import java.util.Collection;
import net.minecraft.world.level.material.Fluid;

public interface IFuelManager {
   <F extends IFuel> F addFuel(F var1);

   IFuel addFuel(FluidStack var1, long var2, int var4);

   default IFuel addFuel(Fluid fluid, long powerPerCycle, int totalBurningTime) {
      return this.addFuel(new FluidStack(fluid, 1), powerPerCycle, totalBurningTime);
   }

   IFuelManager.IDirtyFuel addDirtyFuel(FluidStack var1, long var2, int var4, FluidStack var5);

   default IFuelManager.IDirtyFuel addDirtyFuel(Fluid fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
      return this.addDirtyFuel(new FluidStack(fuel, 1), powerPerCycle, totalBurningTime, residue);
   }

   Collection<IFuel> getFuels();

   IFuel getFuel(FluidStack var1);

   interface IDirtyFuel extends IFuel {
      FluidStack getResidue();
   }
}
