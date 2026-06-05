package buildcraft.energy;

import buildcraft.fabric.BCEnergyWorldGenFabric;

public final class BCEnergyWorldGen {
    private BCEnergyWorldGen() {}

    public static void init() {
        BCEnergyWorldGenFabric.init();
    }
}
