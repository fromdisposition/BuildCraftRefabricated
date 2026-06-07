package buildcraft.robotics.path;

import net.minecraft.world.level.material.Fluid;

public interface IFluidFilter {
   boolean matches(Fluid fluid);
}
