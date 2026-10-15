/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.mixin.client;

import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.FluidWorldRenderer;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.21.1 has no FogEnvironment list (unlike 1.21.5), so BC fluid fog is set directly here, mirroring upstream
// FluidType.setupFog; setting distance directly (not via CameraBcFluidMixin's WATER type, whose getWaterVision() stays ~0) is what makes the fog visible.
@Mixin(FogRenderer.class)
public class FogRendererBcFluidMixin {
   @Shadow
   private static float fogRed;
   @Shadow
   private static float fogGreen;
   @Shadow
   private static float fogBlue;

   @Inject(method = "setupColor", at = @At("RETURN"))
   private static void buildcraft$tintBcFluidFog(
      Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenAmount, CallbackInfo ci
   ) {
      BcFluidAppearance appearance = FluidWorldRenderer.appearanceAtCamera(camera, level);
      if (appearance == null) {
         return;
      }

      fogRed = appearance.fogRed();
      fogGreen = appearance.fogGreen();
      fogBlue = appearance.fogBlue();
      RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 1.0F);
   }

   @Inject(method = "setupFog", at = @At("RETURN"))
   private static void buildcraft$bcFluidFogDistance(
      Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thickFog, float partialTick, CallbackInfo ci
   ) {
      // setupFog has no ClientLevel param; take the client level directly. appearanceAtCamera is the same
      // detector the submerged overlay uses, so it fires whenever the eye is in a BC fluid.
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         return;
      }

      BcFluidAppearance appearance = FluidWorldRenderer.appearanceAtCamera(camera, level);
      if (appearance == null) {
         return;
      }

      // Override the fog start/end with the fluid's own distances (mirrors upstream FluidType.setupFog and the
      // 26.x FogData environmentalStart/End). Done directly so the fog renders regardless of the WATER branch.
      RenderSystem.setShaderFogStart(appearance.environmentalStart());
      RenderSystem.setShaderFogEnd(appearance.environmentalEnd());
      RenderSystem.setShaderFogShape(FogShape.CYLINDER);
   }
}
