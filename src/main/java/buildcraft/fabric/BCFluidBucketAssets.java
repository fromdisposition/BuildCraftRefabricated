package buildcraft.fabric;

import buildcraft.fabric.BCEnergyFluidsFabric.FluidEntry;

public final class BCFluidBucketAssets {
    private BCFluidBucketAssets() {}

    public static String stillSpriteId(String fluidRegName) {
        if (fluidRegName.contains("_heat_")) {
            return fluidRegName + "_still";
        }
        return fluidRegName + "_heat_0_still";
    }

    public static String stillSpriteId(FluidEntry entry) {
        return stillSpriteId(entry.name());
    }

    public static String bucketItemId(String fluidRegName) {
        return fluidRegName + "_bucket";
    }

    public static String bucketModelId(String bucketItemId) {
        return "item/fluid_buckets/" + bucketItemId;
    }
}
