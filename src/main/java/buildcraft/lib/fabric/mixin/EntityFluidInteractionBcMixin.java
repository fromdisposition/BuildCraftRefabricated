package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BC liquids are tracked under {@link BcFluidTags#BC_LIQUIDS}, not {@link FluidTags#WATER}.
 * Bridge vanilla water queries so drowning, movement slowdown, currents, and swim state
 * behave like real water without adding BC fluids to the water tag.
 */
@Mixin(EntityFluidInteraction.class)
public class EntityFluidInteractionBcMixin {
   @Inject(method = "getFluidHeight", at = @At("RETURN"), cancellable = true)
   private void buildcraft$waterHeightIncludesBcLiquids(TagKey<Fluid> tag, CallbackInfoReturnable<Double> cir) {
      if (tag != FluidTags.WATER) {
         return;
      }

      EntityFluidInteraction self = (EntityFluidInteraction)(Object)this;
      double bcHeight = self.getFluidHeight(BcFluidTags.BC_LIQUIDS);
      if (bcHeight > cir.getReturnValue()) {
         cir.setReturnValue(bcHeight);
      }
   }

   @Inject(method = "isEyeInFluid", at = @At("RETURN"), cancellable = true)
   private void buildcraft$waterEyeIncludesBcLiquids(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
      if (tag != FluidTags.WATER || cir.getReturnValue()) {
         return;
      }

      EntityFluidInteraction self = (EntityFluidInteraction)(Object)this;
      if (self.isEyeInFluid(BcFluidTags.BC_LIQUIDS)) {
         cir.setReturnValue(true);
      }
   }
}
