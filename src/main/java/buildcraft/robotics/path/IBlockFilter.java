package buildcraft.robotics.path;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Predicate over a block position in the world, used by robot block-search AIs. */
public interface IBlockFilter {
   boolean matches(Level world, BlockPos pos);
}
