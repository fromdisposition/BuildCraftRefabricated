package buildcraft.fabric.fluid;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;
import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
import org.junit.jupiter.api.Test;

class BcFluidEntityInteractionsTest {
   @Test
   void registersBcLiquidsAsWaterLike() {
      BcFluidEntityInteractions.register();

      FluidBehavior behavior = EntityFluidInteractionRegistry.getFluidBehavior(BcFluidTags.BC_LIQUIDS);
      assertNotNull(behavior);
      assertSame(FluidBehavior.WATER_LIKE, behavior);
      assertTrue(EntityFluidInteractionRegistry.getCustomInteractableFluids().contains(BcFluidTags.BC_LIQUIDS));
   }

   @Test
   void doesNotRegisterBcFluidsTag() {
      BcFluidEntityInteractions.register();
      assertFalse(EntityFluidInteractionRegistry.getCustomInteractableFluids().contains(BcFluidTags.BC_FLUIDS));
   }
}
