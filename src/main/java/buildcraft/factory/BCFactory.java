package buildcraft.factory;

import buildcraft.fabric.BCFactoryFabric;

public final class BCFactory {
   public static final String MODID = "buildcraftfactory";

   private BCFactory() {
   }

   public static void init() {
      BCFactoryFabric.register();
   }
}
