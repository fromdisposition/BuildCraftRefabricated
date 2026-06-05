package buildcraft.energy.client;

import buildcraft.fabric.BCEnergyFabricClient;

public final class BCEnergyClient {
    private BCEnergyClient() {}

    public static void init() {
        BCEnergyFabricClient.init();
    }

    public static void initClient(Object unusedModEventBus) {
        init();
    }
}
