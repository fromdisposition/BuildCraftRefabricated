package buildcraft.core;

import buildcraft.transport.client.model.PipeBaseModelGenStandard;

public final class BCUnifiedClientConfig {
   private BCUnifiedClientConfig() {
   }

   public static void onDisplayConfigReloaded() {
      PipeBaseModelGenStandard.clearSpriteCaches();
      PipeBaseModelGenStandard.onColorBlindToggle();
   }
}
