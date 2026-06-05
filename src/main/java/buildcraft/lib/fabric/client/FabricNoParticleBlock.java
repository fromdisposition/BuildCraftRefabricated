package buildcraft.lib.fabric.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;

public interface FabricNoParticleBlock {
    default void fabricSuppressDestroyParticles(
            Block block, BlockState state, Level level, BlockPos pos, Player player) {

    }
}
