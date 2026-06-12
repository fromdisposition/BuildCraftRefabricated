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
import org.objectweb.asm.Opcodes;

@Mixin(Entity.class)
public class EntityBcFluidSwimMixin {
   @Shadow
   private EntityFluidInteraction fluidInteraction;

   @Shadow
   protected boolean wasTouchingWater;

   @Shadow
   protected boolean wasEyeInWater;

   @Inject(
      method = "baseTick",
      at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/entity/Entity;wasEyeInWater:Z", shift = At.Shift.AFTER)
   )
   private void buildcraft$eyeInBcLiquid(CallbackInfo ci) {
      Entity self = (Entity)(Object)this;
      if (self.isEyeInFluid(BcFluidTags.BC_LIQUIDS)) {
         this.wasEyeInWater = true;
      }
   }

   @Inject(method = "updateFluidInteraction", at = @At("TAIL"))
   private void buildcraft$touchBcLiquids(CallbackInfoReturnable<Boolean> cir) {
      if (this.fluidInteraction.isInFluid(BcFluidTags.BC_LIQUIDS)) {
         this.wasTouchingWater = true;
      }
   }

   @Inject(method = "getFluidHeight", at = @At("RETURN"), cancellable = true)
   private void buildcraft$waterHeightIncludesBcLiquids(TagKey<Fluid> tag, CallbackInfoReturnable<Double> cir) {
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
