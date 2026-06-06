package buildcraft.api.core;

import net.minecraft.core.BlockPos;

public interface IAreaProvider {
   BlockPos min();

   BlockPos max();

   void removeFromWorld();
}
