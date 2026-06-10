package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.fluid.BcFluidUtil;
import buildcraft.lib.client.fluid.BcFluidFogEnvironment;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
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
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level != null) {
         FluidState fluidState = minecraft.level.getFluidState(BlockPos.containing(player.getX(), player.getEyeY(), player.getZ()));
         if (BcFluidUtil.clientAppearance(fluidState) != null) {
            return 0.0F;
         }
      }

      return player.getWaterVision();
   }
}
