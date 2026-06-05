package buildcraft.factory.client;

import buildcraft.fabric.BCFactoryFabricClient;

public final class BCFactoryClient {
    private BCFactoryClient() {}

    public static void init() {
        BCFactoryFabricClient.init();
    }
}
