package buildcraft.fabric.fluid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import buildcraft.lib.client.fluid.BcFluidFogProfiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BcFluidFogProfilesTest {
   @BeforeEach
   void loadProfiles() {
      BcFluidFogProfiles.loadFromClasspath();
   }

   @Test
   void resolvesKnownOilLiquidProfile() {
      BcFluidFogProfiles.Profile profile = BcFluidFogProfiles.resolve("oil", false);
      assertEquals(0.18F, profile.environmentalStart(), 0.001F);
      assertEquals(1.5F, profile.environmentalEnd(), 0.001F);
      assertEquals(0.95F, profile.liquidAlpha(), 0.001F);
      assertEquals(0.10F, profile.overlayAlpha(), 0.001F);
   }

   @Test
   void resolvesGaseousFuelProfile() {
      BcFluidFogProfiles.Profile profile = BcFluidFogProfiles.resolve("fuel_gaseous", true);
      assertEquals(0.20F, profile.environmentalStart(), 0.001F);
      assertEquals(1.5F, profile.environmentalEnd(), 0.001F);
      assertEquals(0.55F, profile.gaseousAlpha(), 0.001F);
   }

   @Test
   void heatClarityUsesFluidOverride() {
      assertEquals(0.04F, BcFluidFogProfiles.heatClarityMultiplier("oil_dense", false), 0.001F);
      assertEquals(0.10F, BcFluidFogProfiles.heatClarityMultiplier("fuel_light", false), 0.001F);
      assertEquals(0.03F, BcFluidFogProfiles.heatClarityMultiplier("fuel_light", true), 0.001F);
   }

   @Test
   void unknownFluidFallsBackToDefaults() {
      BcFluidFogProfiles.Profile liquid = BcFluidFogProfiles.resolve("unknown_fluid", false);
      assertTrue(liquid.environmentalEnd() > 0.0F);
      assertEquals(0.06F, BcFluidFogProfiles.heatClarityMultiplier("unknown_fluid", false), 0.001F);
   }
}
