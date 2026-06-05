package buildcraft.lib.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import buildcraft.lib.fabric.client.FabricNoParticleBlock;

@Mixin(Block.class)
public abstract class BlockDestroyParticlesMixin {
    @Inject(method = "spawnDestroyParticles", at = @At("HEAD"), cancellable = true)
    private void buildcraft$suppressDestroyParticles(
            Level level, Player player, BlockPos pos, BlockState state, CallbackInfo ci) {
        if ((Object) this instanceof FabricNoParticleBlock) {
            ci.cancel();
        }
    }
}
