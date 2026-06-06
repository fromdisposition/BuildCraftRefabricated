package buildcraft.fabric;

public final class BCFluidBucketAssets {
   private BCFluidBucketAssets() {
   }

   public static String stillSpriteId(String fluidRegName) {
      return fluidRegName.contains("_heat_") ? fluidRegName + "_still" : fluidRegName + "_heat_0_still";
   }

   public static String stillSpriteId(BCEnergyFluidsFabric.FluidEntry entry) {
      return stillSpriteId(entry.name());
   }

   public static String bucketItemId(String fluidRegName) {
      return fluidRegName + "_bucket";
   }

   public static String bucketModelId(String bucketItemId) {
      return "item/fluid_buckets/" + bucketItemId;
   }
}
