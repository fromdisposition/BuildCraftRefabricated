package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidTags;
import buildcraft.fabric.fluid.BcFluidUtil;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityBcFluidSwimMixin {
   private static final double WATER_CURRENT_MULTIPLIER = 0.014D;

   @Shadow
   private EntityFluidInteraction fluidInteraction;

   @Shadow
   protected boolean wasEyeInWater;

   /**
    * Vanilla sets {@code wasEyeInWater} from {@link FluidTags#WATER} before
    * {@link Entity#updateFluidInteraction()} runs. Refresh after the tracker update so
    * {@link Entity#isUnderWater()} works the same tick we enter BC liquid.
    */
   @Inject(
      method = "baseTick",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Entity;updateSwimming()V",
         shift = At.Shift.BEFORE
      )
   )
   private void buildcraft$syncEyeInWaterAfterFluidUpdate(CallbackInfo ci) {
      if (((Entity)(Object)this).isEyeInFluid(FluidTags.WATER)) {
         this.wasEyeInWater = true;
      }
   }

   @Inject(
      method = "updateFluidInteraction",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/EntityFluidInteraction;applyCurrentTo(Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/entity/Entity;D)V",
         ordinal = 0,
         shift = At.Shift.AFTER
      )
   )
   private void buildcraft$applyBcLiquidCurrent(CallbackInfoReturnable<Boolean> cir) {
      Entity self = (Entity)(Object)this;
      if (this.fluidInteraction.isInFluid(BcFluidTags.BC_LIQUIDS)) {
         this.fluidInteraction.applyCurrentTo(BcFluidTags.BC_LIQUIDS, self, WATER_CURRENT_MULTIPLIER);
      }
   }

   @Inject(method = "getFluidHeight", at = @At("RETURN"), cancellable = true)
   private void buildcraft$entityWaterHeightIncludesBcLiquids(TagKey<Fluid> tag, CallbackInfoReturnable<Double> cir) {
      if (tag != FluidTags.WATER) {
         return;
      }

      double bcHeight = this.fluidInteraction.getFluidHeight(BcFluidTags.BC_LIQUIDS);
      if (bcHeight > cir.getReturnValue()) {
         cir.setReturnValue(bcHeight);
      }
   }

   @Inject(method = "updateSwimming", at = @At("RETURN"))
   private void buildcraft$swimInBcLiquids(CallbackInfo ci) {
      Entity self = (Entity)(Object)this;
      if (!(self instanceof LivingEntity living)) {
         return;
      }

      if (BcFluidUtil.isEyeInGaseousBcFluid(living)) {
         living.setSwimming(false);
         return;
      }

      if (!living.isSwimming() && living.isSprinting() && !living.isPassenger() && living.isUnderWater()) {
         FluidState feet = living.level().getFluidState(living.blockPosition());
         if (feet.getType().is(BcFluidTags.BC_LIQUIDS)) {
            living.setSwimming(true);
         }
      }
   }
}
