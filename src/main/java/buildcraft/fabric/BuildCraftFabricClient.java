package buildcraft.fabric;

import net.fabricmc.api.ClientModInitializer;
public class BuildCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        BCClientRegistriesFabric.register();
        BuildCraftFabricNetworking.registerClient();
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
