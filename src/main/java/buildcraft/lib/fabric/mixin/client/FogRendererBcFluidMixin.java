package buildcraft.lib.fabric.mixin.client;

import buildcraft.lib.client.fluid.BcFluidFogEnvironment;
import buildcraft.lib.client.fluid.FluidWorldRenderer;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererBcFluidMixin {
   @Shadow
   @Final
   @Mutable
   private static List<FogEnvironment> FOG_ENVIRONMENTS;

   @Inject(method = "<clinit>", at = @At("RETURN"))
   private static void buildcraft$registerBcFluidFogEnvironment(CallbackInfo ci) {
      // Replace with a mutable copy first — vanilla may use List.of() or a compact list factory
      // which would throw UnsupportedOperationException on add().
      FOG_ENVIRONMENTS = new java.util.ArrayList<>(FOG_ENVIRONMENTS);

      int waterIndex = -1;
      for (int i = 0; i < FOG_ENVIRONMENTS.size(); i++) {
         if (FOG_ENVIRONMENTS.get(i) instanceof WaterFogEnvironment) {
            waterIndex = i;
            break;
         }
      }

      if (waterIndex >= 0) {
         FOG_ENVIRONMENTS.add(waterIndex, new BcFluidFogEnvironment());
      } else {
         FOG_ENVIRONMENTS.add(new BcFluidFogEnvironment());
      }
   }

   @Redirect(method = "computeFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getWaterVision()F"))
   private float buildcraft$skipWaterVisionForBcFluids(LocalPlayer player) {
      return FluidWorldRenderer.skipWaterVision(player) ? 0.0F : player.getWaterVision();
   }
}
