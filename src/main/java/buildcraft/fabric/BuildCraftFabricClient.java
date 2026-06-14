package buildcraft.fabric;

import buildcraft.fabric.network.BCNetworkingRegistry;
import buildcraft.lib.fabric.client.BCClientBlockEntityLifecycleEvents;
import net.fabricmc.api.ClientModInitializer;

public class BuildCraftFabricClient implements ClientModInitializer {
   public void onInitializeClient() {
      BCClientBlockEntityLifecycleEvents.init();
      BCClientRegistriesFabric.register();
      BCNetworkingRegistry.registerClient();
      BCCoreFabricClient.init();
      BCEnergyFabricClient.init();
      BCFactoryFabricClient.init();
      BCTransportFabricClient.init();
      BCBuildersFabricClient.init();
      BCSiliconFabricClient.init();
      BCRoboticsFabricClient.init();
      BCLibFabricClient.init();
   }
}
