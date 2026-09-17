package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.fluid.BcFluidTags;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
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
   //? if >= 1.21.11 {
   @Shadow
   @Nullable
   private Level level;
   //?} else {
   /*// 1.21.10 Camera.level is typed BlockGetter (widened to Level in 1.21.11); getFluidState exists on both.
   @Shadow
   @Nullable
   private BlockGetter level;
   *///?}

   @Shadow
   private Vec3 position;

   //? if >= 1.21.11 {
   @Shadow
   public BlockPos blockPosition() {
      throw new AssertionError();
   }
   //?} else {
   /*// Camera.getBlockPosition() was renamed to blockPosition() in 1.21.11.
   @Shadow
   public BlockPos getBlockPosition() {
      throw new AssertionError();
   }
   *///?}

   @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
   private void buildcraft$bcFluidFogType(CallbackInfoReturnable<FogType> cir) {
      // Only override when vanilla returned NONE — any other fog type (WATER, LAVA, POWDER_SNOW)
      // already reflects the player's actual environment and must not be replaced.
      if (cir.getReturnValue() != FogType.NONE || this.level == null) {
         return;
      }

      //? if >= 1.21.11 {
      BlockPos pos = this.blockPosition();
      //?} else {
      /*BlockPos pos = this.getBlockPosition();
      *///?}
      FluidState state = this.level.getFluidState(pos);
      if (state.is(BcFluidTags.BC_FLUIDS) && this.position.y < pos.getY() + state.getHeight(this.level, pos)) {
         cir.setReturnValue(FogType.WATER);
      }
   }
}
