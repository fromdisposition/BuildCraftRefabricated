package buildcraft.core;

import buildcraft.fabric.BCCoreFabricClient;

public final class BCCoreClient {
   private static boolean initialized;

   private BCCoreClient() {
   }

   public static void init() {
      if (!initialized) {
         initialized = true;
         BCCoreFabricClient.init();
      }
   }
}
