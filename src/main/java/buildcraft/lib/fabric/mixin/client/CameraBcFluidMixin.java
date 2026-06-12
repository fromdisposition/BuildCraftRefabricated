package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.fluid.BcFluidTags;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jspecify.annotations.Nullable;

@Mixin(Camera.class)
public class CameraBcFluidMixin {
   @Shadow
   @Nullable
   private Level level;

   @Shadow
   private Vec3 position;

   @Shadow
   public BlockPos blockPosition() {
      throw new AssertionError();
   }

   @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
   private void buildcraft$bcFluidFogType(CallbackInfoReturnable<FogType> cir) {
      if (cir.getReturnValue() == FogType.WATER || this.level == null) {
         return;
      }

      BlockPos pos = this.blockPosition();
      FluidState state = this.level.getFluidState(pos);
      if (state.is(BcFluidTags.BC_FLUIDS) && this.position.y < pos.getY() + state.getHeight(this.level, pos)) {
         cir.setReturnValue(FogType.WATER);
      }
   }
}
