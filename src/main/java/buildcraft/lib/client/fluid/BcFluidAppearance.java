package buildcraft.lib.client.fluid;

import buildcraft.fabric.BCEnergyFluidsFabric;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public record BcFluidAppearance(
   float fogRed,
   float fogGreen,
   float fogBlue,
   float fogAlpha,
   float environmentalStart,
   float environmentalEnd,
   float overlayAlpha,
   TextureAtlasSprite sprite,
   int tint,
   boolean translucent
) {
   public static BcFluidAppearance fromEntry(
      BCEnergyFluidsFabric.FluidEntry entry,
      TextureAtlasSprite sprite,
      int tint,
      boolean translucent
   ) {
      FogFields fog = computeFog(entry.baseName(), entry.heat(), entry.gaseous(), entry.texLight(), entry.texDark());
      return new BcFluidAppearance(
         fog.fogRed(),
         fog.fogGreen(),
         fog.fogBlue(),
         fog.fogAlpha(),
         fog.environmentalStart(),
         fog.environmentalEnd(),
         fog.overlayAlpha(),
         sprite,
         tint,
         translucent
      );
   }

   public static BcFluidAppearance vanilla(TextureAtlasSprite sprite, int tint, boolean translucent) {
      return new BcFluidAppearance(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, sprite, tint, translucent);
   }

   private static FogFields computeFog(String baseName, int heat, boolean gaseous, int texLight, int texDark) {
      int r = ((texLight >> 16 & 0xFF) + (texDark >> 16 & 0xFF)) / 2;
      int g = ((texLight >> 8 & 0xFF) + (texDark >> 8 & 0xFF)) / 2;
      int b = ((texLight & 0xFF) + (texDark & 0xFF)) / 2;
      float fogRed = r / 255.0F;
      float fogGreen = g / 255.0F;
      float fogBlue = b / 255.0F;
      BcFluidFogProfiles.Profile profile = BcFluidFogProfiles.resolve(baseName, gaseous);
      float heatClarity = 1.0F + heat * BcFluidFogProfiles.heatClarityMultiplier(baseName, gaseous);
      float environmentalStart = profile.environmentalStart();
      float environmentalEnd = profile.environmentalEnd() * heatClarity;
      float fogAlpha = gaseous ? profile.gaseousAlpha() : profile.liquidAlpha();
      float overlayAlpha = profile.overlayAlpha();
      return new FogFields(fogRed, fogGreen, fogBlue, fogAlpha, environmentalStart, environmentalEnd, overlayAlpha);
   }

   private record FogFields(
      float fogRed,
      float fogGreen,
      float fogBlue,
      float fogAlpha,
      float environmentalStart,
      float environmentalEnd,
      float overlayAlpha
   ) {
   }
}
