package buildcraft.core;

import buildcraft.fabric.BCCoreFabricClient;

public final class BCCoreClient {
    private static boolean initialized;

    private BCCoreClient() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        BCCoreFabricClient.init();
    }

}
