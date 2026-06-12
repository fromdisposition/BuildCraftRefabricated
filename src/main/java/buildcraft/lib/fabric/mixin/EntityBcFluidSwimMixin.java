package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidTags;
import buildcraft.fabric.fluid.BcFluidUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityBcFluidSwimMixin {
   @Shadow
   protected boolean wasTouchingWater;

   @Inject(method = "updateFluidInteraction", at = @At("TAIL"))
   private void buildcraft$touchBcLiquids(CallbackInfoReturnable<Boolean> cir) {
      Entity self = (Entity)(Object)this;
      if (BcFluidUtil.touchesBcLiquid(self)) {
         this.wasTouchingWater = true;
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

      if (BcFluidUtil.shouldCrawlSwimInBcLiquid(living)) {
         living.setSwimming(true);
      } else if (living.isEyeInFluid(BcFluidTags.BC_LIQUIDS) && living.isSwimming()) {
         living.setSwimming(false);
      }
   }
}
