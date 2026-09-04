package buildcraft.fabric.fluid;

//? if >= 26.1 {
import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
//?}

public final class BcFluidEntityInteractions {
   private BcFluidEntityInteractions() {
   }

   public static void register() {
      //? if >= 26.1 {
      // simple() rather than WATER_LIKE: no swimming, sprinting, boats or ridden-mob floating, matching the
      // mixin physics on the older nodes.
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
