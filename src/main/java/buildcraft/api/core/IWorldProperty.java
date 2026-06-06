package buildcraft.api.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IWorldProperty {
   boolean get(Level var1, BlockPos var2);

   void clear();
}
