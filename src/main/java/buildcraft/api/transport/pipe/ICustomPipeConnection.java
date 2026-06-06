package buildcraft.api.transport.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICustomPipeConnection {
   float getExtension(Level var1, BlockPos var2, Direction var3, BlockState var4);
}
