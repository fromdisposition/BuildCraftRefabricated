package buildcraft.lib.client.fluid;

import buildcraft.fabric.fluid.BcFluidClientAppearance;
import buildcraft.fabric.fluid.BcFluidUtil;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;

/**
 * Vanilla {@link FogEnvironment} hook for all BuildCraft world fluids (oil, fuel, gas tiers).
 * Registered ahead of {@link net.minecraft.client.renderer.fog.environment.WaterFogEnvironment}.
 */
public final class BcFluidFogEnvironment extends FogEnvironment {
   @Override
   public void setupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker) {
      BcFluidClientAppearance appearance = appearanceAt(camera, level);
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

      return appearanceAt(entity, level) != null;
   }

   @Override
   public int getBaseColor(ClientLevel level, Camera camera, int renderDistance, float partialTicks) {
      BcFluidClientAppearance appearance = appearanceAt(camera, level);
      if (appearance == null) {
         return -1;
      }

      return ARGB.colorFromFloat(appearance.fogAlpha(), appearance.fogRed(), appearance.fogGreen(), appearance.fogBlue());
   }

   @Nullable
   private static BcFluidClientAppearance appearanceAt(Camera camera, ClientLevel level) {
      FluidState fluidState = level.getFluidState(camera.blockPosition());
      if (camera.position().y > camera.blockPosition().getY() + fluidState.getHeight(level, camera.blockPosition())) {
         fluidState = level.getFluidState(BlockPos.containing(camera.entity().getX(), camera.entity().getEyeY(), camera.entity().getZ()));
      }

      return BcFluidUtil.clientAppearance(fluidState);
   }

   @Nullable
   private static BcFluidClientAppearance appearanceAt(Entity entity, ClientLevel level) {
      FluidState fluidState = level.getFluidState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
      return BcFluidUtil.clientAppearance(fluidState);
   }
}
