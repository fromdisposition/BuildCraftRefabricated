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
 * BC liquids are tracked under {@link BcFluidTags#BC_LIQUIDS}; gases use {@link BcFluidTags#BC_FLUIDS}.
 * Bridge {@link FluidTags#WATER} for swim/drown physics and {@link BcFluidTags#BC_FLUIDS} for fog/overlay
 * without adding BC fluids to the minecraft water tag.
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
   private void buildcraft$bridgeFluidHeight(TagKey<Fluid> tag, CallbackInfoReturnable<Double> cir) {
      TagKey<Fluid> bcTag = null;
      if (tag == FluidTags.WATER || tag == BcFluidTags.BC_FLUIDS) {
         bcTag = BcFluidTags.BC_LIQUIDS;
      }

      if (bcTag == null) {
         return;
      }

      EntityFluidInteraction self = (EntityFluidInteraction)(Object)this;
      double bcHeight = self.getFluidHeight(bcTag);
      if (bcHeight > cir.getReturnValue()) {
         cir.setReturnValue(bcHeight);
      }
   }

   @Inject(method = "isEyeInFluid", at = @At("RETURN"), cancellable = true)
   private void buildcraft$bridgeEyeInFluid(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
      if (cir.getReturnValue()) {
         return;
      }

      TagKey<Fluid> bcTag = null;
      if (tag == FluidTags.WATER || tag == BcFluidTags.BC_FLUIDS) {
         bcTag = BcFluidTags.BC_LIQUIDS;
      }

      if (bcTag == null) {
         return;
      }

      EntityFluidInteraction self = (EntityFluidInteraction)(Object)this;
      if (self.isEyeInFluid(bcTag)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "isInFluid", at = @At("RETURN"), cancellable = true)
   private void buildcraft$bridgeInFluid(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
      if (cir.getReturnValue()) {
         return;
      }

      TagKey<Fluid> bcTag = null;
      if (tag == FluidTags.WATER || tag == BcFluidTags.BC_FLUIDS) {
         bcTag = BcFluidTags.BC_LIQUIDS;
      }

      if (bcTag == null) {
         return;
      }

      EntityFluidInteraction self = (EntityFluidInteraction)(Object)this;
      if (self.isInFluid(bcTag)) {
         cir.setReturnValue(true);
      }
   }
}
