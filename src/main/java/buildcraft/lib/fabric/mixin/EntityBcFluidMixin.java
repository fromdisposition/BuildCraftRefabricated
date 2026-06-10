package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidUtil;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityBcFluidMixin {
   @Inject(method = "doWaterSplashEffect", at = @At("HEAD"), cancellable = true)
   private void buildcraft$skipWaterSplashInBcFluids(CallbackInfo ci) {
      if (BcFluidUtil.touchesBcFluid((Entity)(Object)this)) {
         ci.cancel();
      }
   }

   @Inject(method = "waterSwimSound", at = @At("HEAD"), cancellable = true)
   private void buildcraft$skipWaterSwimSoundInBcFluids(CallbackInfo ci) {
      if (BcFluidUtil.touchesBcFluid((Entity)(Object)this)) {
         ci.cancel();
      }
   }
}
