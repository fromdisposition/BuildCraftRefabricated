package buildcraft.fabric.fluid;

import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;

/**
 * Registers BC liquid entity physics through Fabric's {@link EntityFluidInteractionRegistry}.
 * Gaseous variants stay out of {@link BcFluidTags#BC_LIQUIDS} and therefore behave like air.
 */
public final class BcFluidEntityInteractions {
   private BcFluidEntityInteractions() {
   }

   public static void register() {
      EntityFluidInteractionRegistry.register(BcFluidTags.BC_LIQUIDS, FluidBehavior.WATER_LIKE);
   }
}
