package buildcraft.lib.fabric.mixin;

import buildcraft.lib.block.LocalBlockUpdateNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Notifies {@link LocalBlockUpdateNotifier} whenever a block state changes on the server, so BC's
 * pipe networks and gate statements can react to neighbour block changes without polling.
 *
 * Fabric API has no block-set event on {@code Level} (tracking issue:
 * https://github.com/FabricMC/fabric/issues/1500). Keep this mixin until upstream ships one.
 * {@code require = 0} on both injections so the mixin degrades silently if the target shifts.
 *
 * Old state is captured at HEAD (before setBlock overwrites it) via a ThreadLocal so the RETURN
 * injection can deliver the correct before/after pair to LocalBlockUpdateNotifier.
 */
@Mixin(Level.class)
public abstract class LevelMixin {
   private static final ThreadLocal<BlockState> BC_OLD_STATE = new ThreadLocal<>();

   @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), require = 0)
   private void buildcraft$captureOldState4(BlockPos pos, BlockState newState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
      Level level = (Level)(Object)this;
      if (level instanceof ServerLevel) {
         BC_OLD_STATE.set(level.getBlockState(pos));
      }
   }

   @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"), require = 0)
   private void buildcraft$onSetBlockReturn(BlockPos pos, BlockState newState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
      if ((Boolean)cir.getReturnValue()) {
         BlockState oldState = BC_OLD_STATE.get();
         BC_OLD_STATE.remove();
         Level level = (Level)(Object)this;
         if (level instanceof ServerLevel) {
            LocalBlockUpdateNotifier.onLevelBlockStateChanged(level, pos, oldState != null ? oldState : newState, newState, flags);
         }
      } else {
         BC_OLD_STATE.remove();
      }
   }

   // Only the 4-arg overload is hooked: the 3-arg setBlock delegates to it on every supported version, so hooking
   // both fired the notifier twice per call -- the second time with the ThreadLocal already cleared, reporting a
   // bogus oldState == newState transition.
}
