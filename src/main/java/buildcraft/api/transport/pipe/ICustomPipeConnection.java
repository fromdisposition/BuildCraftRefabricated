package buildcraft.api.transport.pipe;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ICustomPipeConnection {

    float getExtension(Level world, BlockPos pos, Direction face, BlockState state);
}
