package buildcraft.lib.engine;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.IMjRedstoneReceiver;
import javax.annotation.Nonnull;

public class EngineConnector implements IMjConnector {
   public final boolean redstoneOnly;

   public EngineConnector(boolean redstoneOnly) {
      this.redstoneOnly = redstoneOnly;
   }

   @Override
   public boolean canConnect(@Nonnull IMjConnector other) {
      if (!(other instanceof IMjReceiver) || !((IMjReceiver)other).canReceive()) {
         return false;
      } else {
         return this.redstoneOnly ? other instanceof IMjRedstoneReceiver : true;
      }
   }
}
