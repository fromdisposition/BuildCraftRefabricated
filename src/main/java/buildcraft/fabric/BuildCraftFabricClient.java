package buildcraft.fabric;

import buildcraft.fabric.network.BCNetworkingRegistry;
import net.fabricmc.api.ClientModInitializer;

public class BuildCraftFabricClient implements ClientModInitializer {
   public void onInitializeClient() {
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
