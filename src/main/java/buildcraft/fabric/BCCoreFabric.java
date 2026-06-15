package buildcraft.fabric;

import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreBlockEntities;

public final class BCCoreFabric {
   private BCCoreFabric() {
   }

   public static void register() {
      FabricModuleBootstrap.registerCapabilities(BCCoreFabric::registerMjCapabilities, () -> {});
   }

   private static void registerMjCapabilities() {
      if (BCCoreBlockEntities.ENGINE_REDSTONE != null) {
         MjAPI.CAP_CONNECTOR.registerForBlockEntity((engine, direction) -> engine.getMjConnector(), BCCoreBlockEntities.ENGINE_REDSTONE);
      }

      if (BCCoreBlockEntities.ENGINE_CREATIVE != null) {
         MjAPI.CAP_CONNECTOR.registerForBlockEntity((engine, direction) -> engine.getMjConnector(), BCCoreBlockEntities.ENGINE_CREATIVE);
      }

      if (BCCoreBlockEntities.POWER_TESTER != null) {
         MjAPI.CAP_RECEIVER.registerForBlockEntity((tester, direction) -> tester, BCCoreBlockEntities.POWER_TESTER);
      }
   }
}
