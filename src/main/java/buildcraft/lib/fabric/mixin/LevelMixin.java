package buildcraft.lib.fabric.mixin;

import buildcraft.lib.block.LocalBlockUpdateNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
   @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"), require = 0)
   private void buildcraft$onSetBlockReturn(BlockPos pos, BlockState newState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
      this.buildcraft$finishSetBlock(pos, newState, flags, (Boolean)cir.getReturnValue());
   }

   @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("RETURN"), require = 0)
   private void buildcraft$onSetBlockReturnCompat(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
      this.buildcraft$finishSetBlock(pos, newState, flags, (Boolean)cir.getReturnValue());
   }

   private void buildcraft$finishSetBlock(BlockPos pos, BlockState newState, int flags, boolean success) {
      if (success) {
         Level level = (Level)(Object)this;
         LocalBlockUpdateNotifier.onLevelBlockStateChanged(level, pos, newState, newState, flags);
      }
   }
}
