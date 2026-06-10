package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityBcFluidMixin {
   @Inject(method = "makeDrownParticles", at = @At("HEAD"), cancellable = true)
   private void buildcraft$skipDrownBubblesInBcFluids(CallbackInfo ci) {
      LivingEntity self = (LivingEntity)(Object)this;
      if (BcFluidUtil.isBcFluidTag(self.level().getFluidState(BlockPos.containing(self.getX(), self.getEyeY(), self.getZ())))) {
         ci.cancel();
      }
   }
}
