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

/**
 * Notifies {@link LocalBlockUpdateNotifier} whenever a block state changes on the server, so BC's
 * pipe networks and gate statements can react to neighbour block changes without polling.
 *
 * Fabric API has no generic block-set event on {@code Level}: verified against the actual jars up to
 * fabric-api 0.154.3+26.3 (2026-07-17) — the whole event surface has only ServerBlockEntityEvents/
 * ServerChunkEvents/ServerLevelEvents lifecycle plus player interaction events (AttackBlock/UseBlock/
 * PlayerBlockBreakEvents), none of which fire for pistons, fluids, plants or any non-player setBlock.
 * Re-check the api/event tree in the jar on fabric-api bumps before touching this.
 * {@code require = 0} on both injections so the mixin degrades silently if the target shifts.
 *
 * Old state is captured at HEAD (before setBlock overwrites it) and handed to the RETURN injection
 * through a {@link Share}d ref, which is scoped to ONE invocation of setBlock. That scope is the whole
 * point: setBlock is re-entrant (hence its own {@code recursionLeft} parameter) -- neighbour shape
 * updates, fluid spread and block drops all call it again from inside the outer call, on the same
 * thread. A thread-scoped field would let the nested call overwrite and then clear the outer call's
 * state, leaving the outer RETURN with nothing.
 */
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
      // No captured state means HEAD did not run for this invocation (client level): stay silent rather
      // than inventing an oldState == newState "nothing changed" event that subscribers would believe.
      if (cir.getReturnValueZ() && captured != null && (Level)(Object)this instanceof ServerLevel level) {
         LocalBlockUpdateNotifier.onLevelBlockStateChanged(level, pos, captured, newState, flags);
      }
   }

   // Only the 4-arg overload is hooked: the 3-arg setBlock delegates to it on every supported version, so hooking
   // both fired the notifier twice per call -- the second time with the shared ref of a different invocation.
}
