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
 */
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
      if (!success) {
         return;
      }

      Level level = (Level)(Object)this;
      if (!(level instanceof ServerLevel)) {
         return;
      }

      LocalBlockUpdateNotifier.onLevelBlockStateChanged(level, pos, newState, newState, flags);
   }
}
