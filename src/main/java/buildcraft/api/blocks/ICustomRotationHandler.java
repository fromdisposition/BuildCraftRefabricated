package buildcraft.api.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ICustomRotationHandler {
    InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched);
}
