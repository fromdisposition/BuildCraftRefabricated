package buildcraft.fabric;

import buildcraft.builders.platform.BCBuildersFabricClient;
import buildcraft.core.platform.BCCoreFabricClient;
import buildcraft.energy.platform.BCEnergyFabricClient;
import buildcraft.fabric.network.BCNetworkingRegistry;
import buildcraft.factory.platform.BCFactoryFabricClient;
import buildcraft.lib.fabric.client.BCClientBlockEntityLifecycleEvents;
import buildcraft.robotics.platform.BCRoboticsFabricClient;
import buildcraft.silicon.platform.BCSiliconFabricClient;
import buildcraft.transport.platform.BCTransportFabricClient;
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
