package buildcraft.lib.client;

import buildcraft.fabric.BCLibFabricClient;
import buildcraft.lib.fabric.BCLibClientBridge;

public final class BCLibClient {
   private static boolean initialized;

   private BCLibClient() {
   }

   public static void initClient() {
      if (!initialized) {
         initialized = true;
         BCLibFabricClient.init();
      }
   }

   public static void openGuideScreen(String bookName) {
      BCLibClientBridge.openGuideScreen(bookName);
   }
}
