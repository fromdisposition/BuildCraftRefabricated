package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidTags;
import java.util.Map;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BC liquids live in {@link BcFluidTags#BC_LIQUIDS}, not {@link FluidTags#WATER}.
 * Route tracking to the correct tracker and bridge vanilla WATER queries so drowning,
 * movement slowdown, currents, and swim state match real water without the water tag.
 */
@Mixin(EntityFluidInteraction.class)
public class EntityFluidInteractionBcMixin {
   @Shadow
   @Final
   private Map<TagKey<Fluid>, ?> trackerByFluid;

   @Inject(method = "getTrackerFor", at = @At("HEAD"), cancellable = true)
   private void buildcraft$routeBcLiquidsToLiquidTracker(Fluid fluid, CallbackInfoReturnable<Object> cir) {
      if (fluid.is(BcFluidTags.BC_LIQUIDS)) {
         Object tracker = this.trackerByFluid.get(BcFluidTags.BC_LIQUIDS);
         if (tracker != null) {
            cir.setReturnValue(tracker);
         }
      }
   }

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

   @Inject(method = "isInFluid", at = @At("RETURN"), cancellable = true)
   private void buildcraft$waterBodyIncludesBcLiquids(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
      if (tag != FluidTags.WATER || cir.getReturnValue()) {
         return;
      }

      EntityFluidInteraction self = (EntityFluidInteraction)(Object)this;
      if (self.isInFluid(BcFluidTags.BC_LIQUIDS)) {
         cir.setReturnValue(true);
      }
   }
}
