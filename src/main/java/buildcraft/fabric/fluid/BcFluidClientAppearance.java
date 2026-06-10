package buildcraft.fabric.fluid;

public record BcFluidClientAppearance(
   float fogRed,
   float fogGreen,
   float fogBlue,
   float fogAlpha,
   float environmentalStart,
   float environmentalEnd,
   float overlayAlpha
) {
   public static BcFluidClientAppearance compute(String baseName, int heat, boolean gaseous, int texLight, int texDark) {
      int r = ((texLight >> 16 & 0xFF) + (texDark >> 16 & 0xFF)) / 2;
      int g = ((texLight >> 8 & 0xFF) + (texDark >> 8 & 0xFF)) / 2;
      int b = ((texLight & 0xFF) + (texDark & 0xFF)) / 2;
      float fogRed = r / 255.0F;
      float fogGreen = g / 255.0F;
      float fogBlue = b / 255.0F;
      FogProfile profile = gaseous ? gaseousProfile(baseName) : liquidProfile(baseName);
      float heatClarity = 1.0F + heat * heatClarityMultiplier(baseName, gaseous);
      float environmentalStart = profile.environmentalStart();
      float environmentalEnd = profile.environmentalEnd() * heatClarity;
      float fogAlpha = gaseous ? profile.gaseousAlpha() : profile.liquidAlpha();
      float overlayAlpha = profile.overlayAlpha();
      return new BcFluidClientAppearance(fogRed, fogGreen, fogBlue, fogAlpha, environmentalStart, environmentalEnd, overlayAlpha);
   }

   private static FogProfile liquidProfile(String baseName) {
      return switch (baseName) {
         case "oil_residue" -> new FogProfile(0.25F, 1.0F, 0.97F, 0.55F, 0.10F);
         case "oil_heavy" -> new FogProfile(0.22F, 1.2F, 0.96F, 0.55F, 0.10F);
         case "oil_dense" -> new FogProfile(0.20F, 1.3F, 0.96F, 0.55F, 0.10F);
         case "oil" -> new FogProfile(0.18F, 1.5F, 0.95F, 0.55F, 0.10F);
         case "oil_distilled" -> new FogProfile(0.10F, 3.0F, 0.90F, 0.55F, 0.10F);
         case "fuel_dense" -> new FogProfile(0.08F, 4.0F, 0.85F, 0.55F, 0.09F);
         case "fuel_mixed_heavy" -> new FogProfile(0.09F, 3.5F, 0.86F, 0.55F, 0.09F);
         case "fuel_light" -> new FogProfile(0.05F, 8.0F, 0.75F, 0.55F, 0.08F);
         case "fuel_mixed_light" -> new FogProfile(0.06F, 6.0F, 0.78F, 0.55F, 0.08F);
         case "fuel_gaseous" -> new FogProfile(0.06F, 5.5F, 0.80F, 0.55F, 0.08F);
         default -> new FogProfile(0.18F, 1.5F, 0.95F, 0.55F, 0.10F);
      };
   }

   private static FogProfile gaseousProfile(String baseName) {
      return switch (baseName) {
         case "fuel_dense" -> new FogProfile(0.25F, 1.0F, 0.55F, 0.58F, 0.10F);
         case "fuel_mixed_heavy" -> new FogProfile(0.24F, 1.1F, 0.55F, 0.58F, 0.10F);
         case "fuel_gaseous" -> new FogProfile(0.20F, 1.5F, 0.55F, 0.55F, 0.10F);
         case "fuel_light" -> new FogProfile(0.22F, 1.8F, 0.55F, 0.55F, 0.10F);
         case "fuel_mixed_light" -> new FogProfile(0.20F, 1.6F, 0.55F, 0.58F, 0.10F);
         case "oil_distilled" -> new FogProfile(0.21F, 1.4F, 0.55F, 0.58F, 0.10F);
         default -> new FogProfile(0.20F, 1.5F, 0.55F, 0.58F, 0.10F);
      };
   }

   private static float heatClarityMultiplier(String baseName, boolean gaseous) {
      if (gaseous) {
         return 0.03F;
      }

      return switch (baseName) {
         case "oil_residue", "oil_heavy", "oil_dense" -> 0.04F;
         case "oil" -> 0.06F;
         case "oil_distilled" -> 0.08F;
         case "fuel_dense", "fuel_mixed_heavy" -> 0.09F;
         case "fuel_light", "fuel_mixed_light", "fuel_gaseous" -> 0.10F;
         default -> 0.06F;
      };
   }

   private record FogProfile(float environmentalStart, float environmentalEnd, float liquidAlpha, float gaseousAlpha, float overlayAlpha) {
   }
}
