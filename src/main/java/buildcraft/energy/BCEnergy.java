package buildcraft.energy;

import buildcraft.core.BCCore;
import buildcraft.fabric.BCEnergyFabric;

public final class BCEnergy {
    public static final String MODID = "buildcraftenergy";
    private BCEnergy() {}

    public static void init() {
        BCEnergyFabric.register();
    }

    public static void init(Object unusedModEventBus) {
        init();
    }
}
