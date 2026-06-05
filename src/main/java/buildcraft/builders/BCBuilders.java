package buildcraft.builders;

import buildcraft.core.BCCore;
import buildcraft.fabric.BCBuildersFabric;

public final class BCBuilders {
    public static final String MODID = "buildcraftbuilders";

    private BCBuilders() {}

    public static void init() {
        BCBuildersFabric.register();
    }
}
