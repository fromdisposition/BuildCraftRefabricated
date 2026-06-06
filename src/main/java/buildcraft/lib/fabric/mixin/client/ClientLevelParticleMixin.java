package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.client.block.ClientBlockExtensions;
import buildcraft.fabric.client.block.ClientBlockExtensionsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelParticleMixin {
   @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
   private void buildcraft$customDestroyParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
      ClientBlockExtensions ext = ClientBlockExtensionsRegistry.get(state.getBlock());
      if (ext != null) {
         ClientLevel level = (ClientLevel)(Object)this;
         if (ext.addDestroyEffects(state, level, pos, Minecraft.getInstance().particleEngine)) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
   private void buildcraft$customHitParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
      ClientLevel level = (ClientLevel)(Object)this;
      BlockState state = level.getBlockState(pos);
      ClientBlockExtensions ext = ClientBlockExtensionsRegistry.get(state.getBlock());
      if (ext != null) {
         Vec3 hitLoc = Vec3.atCenterOf(pos).add(direction.getStepX() * 0.5, direction.getStepY() * 0.5, direction.getStepZ() * 0.5);
         BlockHitResult hit = new BlockHitResult(hitLoc, direction, pos, false);
         if (ext.addHitEffects(state, level, hit, Minecraft.getInstance().particleEngine)) {
            ci.cancel();
         }
      }
   }
}
