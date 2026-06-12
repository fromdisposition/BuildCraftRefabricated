package buildcraft.lib.client.fluid;

import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

/**
 * Vanilla {@link FogEnvironment} hook for BuildCraft world fluids (oil, fuel, gas tiers).
 */
public final class BcFluidFogEnvironment extends FogEnvironment {
   @Override
   public void setupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker) {
      BcFluidAppearance appearance = FluidWorldRenderer.appearanceAtCamera(camera, level);
      if (appearance == null) {
         return;
      }

      fog.environmentalStart = appearance.environmentalStart();
      fog.environmentalEnd = appearance.environmentalEnd();
      fog.skyEnd = appearance.environmentalEnd();
      fog.cloudEnd = appearance.environmentalEnd();
   }

   @Override
   public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
      if (fogType != FogType.WATER || !(entity.level() instanceof ClientLevel level)) {
         return false;
      }

      return FluidWorldRenderer.appearanceAtEye(entity, level) != null;
   }

   @Override
   public int getBaseColor(ClientLevel level, Camera camera, int renderDistance, float partialTicks) {
      BcFluidAppearance appearance = FluidWorldRenderer.appearanceAtCamera(camera, level);
      if (appearance == null) {
         return -1;
      }

      return ARGB.colorFromFloat(appearance.fogAlpha(), appearance.fogRed(), appearance.fogGreen(), appearance.fogBlue());
   }
}
