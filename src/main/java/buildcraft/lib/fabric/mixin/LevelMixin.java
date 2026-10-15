package buildcraft.lib.fabric.mixin;

import buildcraft.lib.block.LocalBlockUpdateNotifier;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fabric API has no event for non-player setBlock (pistons, fluids, plants); require = 0 so a shifted target degrades silently.
// The @Share ref is per setBlock invocation: setBlock re-enters itself on the same thread, so a thread-scoped field would be clobbered.
@Mixin(Level.class)
public abstract class LevelMixin {
   @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), require = 0)
   private void buildcraft$captureOldState4(
      BlockPos pos,
      BlockState newState,
      int flags,
      int recursionLeft,
      CallbackInfoReturnable<Boolean> cir,
      @Share("buildcraft:oldState") LocalRef<BlockState> oldState
   ) {
      if ((Level)(Object)this instanceof ServerLevel level) {
         oldState.set(level.getBlockState(pos));
      }
   }

   @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"), require = 0)
   private void buildcraft$onSetBlockReturn(
      BlockPos pos,
      BlockState newState,
      int flags,
      int recursionLeft,
      CallbackInfoReturnable<Boolean> cir,
      @Share("buildcraft:oldState") LocalRef<BlockState> oldState
   ) {
      BlockState captured = oldState.get();
      // Null means HEAD did not run for this invocation; stay silent rather than fake a nothing-changed event.
      if (cir.getReturnValueZ() && captured != null && (Level)(Object)this instanceof ServerLevel level) {
         LocalBlockUpdateNotifier.onLevelBlockStateChanged(level, pos, captured, newState, flags);
      }
   }

   // Only the 4-arg overload is hooked: the 3-arg setBlock delegates to it, so hooking both fires the notifier twice.
}
