package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerBcFluidMixin {
   @Shadow
   protected boolean wasUnderwater;

   @Inject(method = "updateIsUnderwater", at = @At("HEAD"), cancellable = true)
   private void buildcraft$updateIsUnderwaterExcludingBcFluids(CallbackInfoReturnable<Boolean> cir) {
      Player self = (Player)(Object)this;
      this.wasUnderwater = self.isEyeInFluid(FluidTags.WATER) && !self.isEyeInFluid(BcFluidTags.BC_FLUIDS);
      cir.setReturnValue(this.wasUnderwater);
   }
}
