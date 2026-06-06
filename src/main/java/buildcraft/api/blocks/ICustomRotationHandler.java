package buildcraft.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICustomRotationHandler {
   InteractionResult attemptRotation(Level var1, BlockPos var2, BlockState var3, Direction var4);
}
