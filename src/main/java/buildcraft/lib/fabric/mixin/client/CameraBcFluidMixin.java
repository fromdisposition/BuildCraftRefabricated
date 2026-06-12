package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.fluid.BcFluidTags;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraBcFluidMixin {
   @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
   private void buildcraft$bcFluidFogType(CallbackInfoReturnable<FogType> cir) {
      if (cir.getReturnValue() == FogType.WATER) {
         return;
      }

      Entity entity = ((Camera)(Object)this).entity();
      if (entity != null && entity.isEyeInFluid(BcFluidTags.BC_FLUIDS)) {
         cir.setReturnValue(FogType.WATER);
      }
   }
}
