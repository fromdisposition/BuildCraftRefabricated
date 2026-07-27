package buildcraft.fabric;

import buildcraft.builders.platform.BCBuildersFabricClient;
import buildcraft.core.platform.BCCoreFabricClient;
import buildcraft.energy.platform.BCEnergyFabricClient;
import buildcraft.fabric.network.BCNetworkingRegistryClient;
import buildcraft.factory.platform.BCFactoryFabricClient;
import buildcraft.lib.fabric.client.BCClientBlockEntityLifecycleEvents;
import buildcraft.lib.fabric.client.BlockOutlineRegistration;
import buildcraft.lib.fabric.client.PictureInPictureRegistration;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.robotics.platform.BCRoboticsFabricClient;
import buildcraft.silicon.platform.BCSiliconFabricClient;
import buildcraft.transport.platform.BCTransportFabricClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class BuildCraftFabricClient implements ClientModInitializer {
   public void onInitializeClient() {
      BCClientBlockEntityLifecycleEvents.init();
      // Client twin of the server-side marker world-unload eviction: the server re-sends marker state on the
      // next join, so drop the per-dimension client caches on disconnect to avoid ghost markers/lasers from
      // the previous world or server.
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MarkerCache.onClientDisconnect());
      BCClientRegistriesFabric.register();
      BCNetworkingRegistryClient.registerClient();
      BCCoreFabricClient.init();
      BCEnergyFabricClient.init();
      BCFactoryFabricClient.init();
      BCTransportFabricClient.init();
      BCBuildersFabricClient.init();
      BCSiliconFabricClient.init();
      BCRoboticsFabricClient.init();
      BCLibFabricClient.init();
      // Per-version classes: both APIs differ by name/context across MC versions and are absent on the oldest
      // nodes, which shadow them with no-op stubs. See versions/{1.21.1,_lt_26.1,_ge_1.21.10_lt_26.1,_ge_26.1}.
      PictureInPictureRegistration.register();
      BlockOutlineRegistration.install();
   }
}
