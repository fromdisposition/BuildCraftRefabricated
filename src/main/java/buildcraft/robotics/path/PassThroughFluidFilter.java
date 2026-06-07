package buildcraft.robotics.path;

import net.minecraft.world.level.material.Fluid;

/** A fluid filter that accepts any non-empty fluid. */
public class PassThroughFluidFilter implements IFluidFilter {
   public static final PassThroughFluidFilter INSTANCE = new PassThroughFluidFilter();

   @Override
   public boolean matches(Fluid fluid) {
      return fluid != null;
   }
}
