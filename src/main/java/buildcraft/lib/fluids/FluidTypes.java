package buildcraft.lib.fluids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidTypes {
   private static final Map<Fluid, FluidType> CACHE = new ConcurrentHashMap<>();

   private FluidTypes() {
   }

   public static void register(Fluid fluid, int viscosity, int density) {
      if (fluid != null && !fluid.isSame(Fluids.EMPTY)) {
         CACHE.put(fluid, new FluidType(fluid, viscosity, density));
      }
   }

   public static FluidType of(Fluid fluid) {
      return fluid != null && !fluid.isSame(Fluids.EMPTY) ? CACHE.computeIfAbsent(fluid, FluidType::new) : FluidType.EMPTY;
   }

   public static FluidType of(Holder<Fluid> holder) {
      return of((Fluid)holder.value());
   }
}
