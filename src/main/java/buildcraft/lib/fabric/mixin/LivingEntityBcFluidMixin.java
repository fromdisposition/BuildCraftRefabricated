package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla {@link LivingEntity#shouldTravelInFluid} only checks {@link LivingEntity#isInWater()}.
 * Keep a direct BC-liquid fallback so movement uses {@code travelInWater} even if fluid flags lag a tick.
 */
@Mixin(LivingEntity.class)
public class LivingEntityBcFluidMixin {
   @Inject(method = "shouldTravelInFluid", at = @At("RETURN"), cancellable = true)
   private void buildcraft$travelInBcLiquids(FluidState feetState, CallbackInfoReturnable<Boolean> cir) {
      if (cir.getReturnValue()) {
         return;
      }

      LivingEntity self = (LivingEntity)(Object)this;
      if (!self.isAffectedByFluids() || self.canStandOnFluid(feetState)) {
         return;
      }

      if (feetState.getType().is(BcFluidTags.BC_LIQUIDS)
         || self.isEyeInFluid(BcFluidTags.BC_LIQUIDS)
         || self.getFluidHeight(FluidTags.WATER) > 0.0D) {
         cir.setReturnValue(true);
      }
   }
}
