package buildcraft.transport;

import buildcraft.fabric.BCTransportFabric;

public final class BCTransport {
    public static final String MODID = "buildcrafttransport";

    private BCTransport() {}

    public static void init() {
        BCTransportFabric.register();
    }
}

