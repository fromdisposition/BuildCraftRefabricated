package buildcraft.fabric.fluid;

//? if >= 26.1 {
import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
//?}

/**
 * Registers BC liquid entity physics through Fabric's EntityFluidInteractionRegistry.
 * Gaseous variants stay out of {@link BcFluidTags#BC_LIQUIDS} and therefore behave like air.
 *
 * <p>The Fabric registry.fluid API does not exist in the 1.21.x Fabric API, so on that target the
 * registration is a no-op (BC liquids simply lack the water-like swim/drown physics for now).
 */
public final class BcFluidEntityInteractions {
   private BcFluidEntityInteractions() {
   }

   public static void register() {
      //? if >= 26.1 {
      // Like WATER_LIKE but WITHOUT swimming/sprinting/boats/ridden-mob floating, to match the 1.21.x
      // mixin behaviour (push + buoyancy + drowning + dive, no swim crawl). simple() already defaults
      // swimming/sprinting/boats/ridden-float OFF and mobsFloat / push(0.014) / gravity ON.
      EntityFluidInteractionRegistry.register(
         BcFluidTags.BC_LIQUIDS,
         FluidBehavior.simple()
            .enableDrowning(true)
            .allowMovingDown(true)
            .allowSprinting(false)
            .build()
      );
      //?}
   }
}
