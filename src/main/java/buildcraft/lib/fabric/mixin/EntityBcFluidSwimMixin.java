package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.fabric.fluid.BcFluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
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
   private void buildcraft$touchBcFluids(CallbackInfoReturnable<Boolean> cir) {
      Entity self = (Entity)(Object)this;
      if (!self.isEyeInFluid(BcFluidTags.BC_FLUIDS)) {
         return;
      }

      FluidState state = self.level().getFluidState(BlockPos.containing(self.getX(), self.getEyeY(), self.getZ()));
      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(state.getType());
      if (entry == null || !entry.holder().props.swimmable()) {
         return;
      }

      this.wasTouchingWater = true;
   }

   @Inject(method = "updateSwimming", at = @At("RETURN"))
   private void buildcraft$swimInBcFluids(CallbackInfo ci) {
      Entity self = (Entity)(Object)this;
      if (!(self instanceof LivingEntity living) || living.isSwimming()) {
         return;
      }
      if (!living.isEyeInFluid(BcFluidTags.BC_FLUIDS)) {
         return;
      }

      FluidState state = living.level().getFluidState(BlockPos.containing(living.getX(), living.getEyeY(), living.getZ()));
      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(state.getType());
      if (entry != null && entry.holder().props.swimmable()) {
         living.setSwimming(true);
      }
   }
}
