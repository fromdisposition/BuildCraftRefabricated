package buildcraft.silicon;

import buildcraft.core.BCCore;
import buildcraft.fabric.BCSiliconFabric;

public final class BCSilicon {
    public static final String MODID = "buildcraftsilicon";

    private BCSilicon() {}

    public static void init() {
        BCSiliconFabric.register();
    }
}
