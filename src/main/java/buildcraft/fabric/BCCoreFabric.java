package buildcraft.fabric;

import buildcraft.core.BCCoreBlockEntities;
import buildcraft.lib.mj.MjBlockCapabilities;

public final class BCCoreFabric {
   private BCCoreFabric() {
   }

   public static void register() {
      registerMjCapabilities();
   }

   private static void registerMjCapabilities() {
      if (BCCoreBlockEntities.ENGINE_REDSTONE != null) {
         MjBlockCapabilities.registerConnector(BCCoreBlockEntities.ENGINE_REDSTONE, (engine, direction) -> engine.getMjConnector());
      }

      if (BCCoreBlockEntities.ENGINE_CREATIVE != null) {
         MjBlockCapabilities.registerConnector(BCCoreBlockEntities.ENGINE_CREATIVE, (engine, direction) -> engine.getMjConnector());
      }

      if (BCCoreBlockEntities.POWER_TESTER != null) {
         MjBlockCapabilities.registerReceiver(BCCoreBlockEntities.POWER_TESTER, (tester, direction) -> tester);
      }
   }
}
