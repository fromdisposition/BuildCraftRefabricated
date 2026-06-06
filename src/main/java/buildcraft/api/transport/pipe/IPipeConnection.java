package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

@Deprecated
public interface IPipeConnection {
   IPipeConnection.ConnectOverride overridePipeConnection(Object var1, Direction var2);

   enum ConnectOverride {
      CONNECT,
      DISCONNECT,
      DEFAULT;
   }
}
