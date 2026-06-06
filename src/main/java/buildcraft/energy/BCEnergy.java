package buildcraft.energy;

import buildcraft.fabric.BCEnergyFabric;

public final class BCEnergy {
   public static final String MODID = "buildcraftenergy";

   private BCEnergy() {
   }

   public static void init() {
      BCEnergyFabric.register();
   }
}
