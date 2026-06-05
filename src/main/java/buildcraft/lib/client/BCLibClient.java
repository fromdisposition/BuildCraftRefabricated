package buildcraft.lib.client;

public final class BCLibClient {
    private static boolean initialized;

    private BCLibClient() {}

    public static void initClient(Object unusedModEventBus) {
        if (initialized) {
            return;
        }
        initialized = true;
        buildcraft.fabric.BCLibFabricClient.init();
    }

    public static void openGuideScreen(String bookName) {
        buildcraft.lib.fabric.BCLibClientBridge.openGuideScreen(bookName);
    }
}
